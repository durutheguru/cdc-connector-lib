package com.julianduru.cdc;

import com.moniepoint.cdc.annotation.ChangeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * created by Julian Duru on 24/02/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcBeanProcessor implements BeanPostProcessor {


    private final CdcProcessorDelegateContainer consumerContainer;


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (hasChangeConsumer(bean)) {
                log.debug("Encountered Bean with actions: {}", beanName);
                registerConsumer(bean);
            }

            return bean;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private boolean hasChangeConsumer(Object bean) {
        ChangeConsumer consumer = bean.getClass().getAnnotation(ChangeConsumer.class);
        return consumer != null;
    }


    /**
     *
     * @param bean
     */
    private void registerConsumer(Object bean) throws Exception {
        ChangeConsumer consumer = bean.getClass().getAnnotation(ChangeConsumer.class);
        Class<?> beanClass = bean.getClass();

        try {
            Method queryMethod = beanClass.getMethod(ChangeConsumer.QUERY_METHOD_NAME, String.class, Payload.class);
            Method processMethod = beanClass.getMethod(ChangeConsumer.PROCESS_METHOD_NAME, String.class, Payload.class);

            validateMethodReturnType(queryMethod, processMethod);
            doRegistration(consumer, bean, queryMethod, processMethod);
        }
        catch (NoSuchMethodException ex) {
            throw new IllegalStateException(
                String.format(
                    "Consumer {%s} must have a query and process method. Supported signatures: %n" +
                    "- OperationStatus query(String reference, Payload payload) %n" +
                    "- OperationStatus process(String reference, Payload payload)%n%n",
                    beanClass.getName()
                )
            );
        }
    }


    private void validateMethodReturnType(Method...methods) {
        for (Method method: methods) {
            Class<?> methodReturnType = method.getReturnType();
            if (methodReturnType != OperationStatus.class) {
                throw new IllegalStateException(
                    String.format("Consumer {%s} method must return OperationStatus", method.getName())
                );
            }
        }
    }


    private void doRegistration(ChangeConsumer consumer, Object bean, Method queryMethod, Method processMethod) throws Exception {
        consumerContainer.registerHandler(
            new CdcProcessorDelegate() {


                @Override
                public String sourceId() {
                    return consumer.sourceId();
                }


                @Override
                public ChangeType type() {
                    return consumer.changeType();
                }


                @Override
                public OperationStatus query(String reference, Payload payload) {
                    try {
                        return (OperationStatus) queryMethod.invoke(bean, reference, payload);
                    }
                    catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        return OperationStatus.inProgress(t.getMessage());
                    }
                }


                @Override
                public OperationStatus process(String reference, Payload payload) {
                    try {
                        return (OperationStatus) processMethod.invoke(bean, reference, payload);
                    }
                    catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        return OperationStatus.inProgress(t.getMessage());
                    }
                }


                @Override
                public boolean supports(Payload payload) {
                    try {
                        //TODO: cache 'supports' method to avoid always using reflection and depending on NoSuchMethodException..
                        Method method = bean.getClass().getMethod(ChangeConsumer.SUPPORTED_PAYLOAD_METHOD_NAME, Payload.class);
                        return (Boolean) method.invoke(bean, payload);
                    }
                    catch (NoSuchMethodException e) {
                        log.debug("No supports payload method declared on consumer: {}. Applying default", bean.getClass().getName());
                        return CdcProcessorDelegate.DEFAULT_SUPPORTS_PAYLOAD_PREDICATE.test(this, payload);
                    }
                    catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        return false;
                    }
                }


            }
        );
    }


}
