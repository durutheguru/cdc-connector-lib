package com.julianduru.cdc;

import com.julianduru.cdc.data.CdcMessage;
import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import com.julianduru.cdc.exception.CdcProcessingException;
import com.julianduru.cdc.util.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * created by Julian Dumebi Duru on 25/05/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcDlqConsumer implements Consumer {


    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;


    private final CdcProcessor cdcProcessor;


    @Override
    public void consume(ConsumerRecord<String, String> record) throws Exception {
        log.info("Message received: " + record);

        CdcMessage message = JSONUtil.fromJsonString(record.value(), CdcMessage.class);
        Payload payload = JSONUtil.fromJsonString(message.getPayload(), Payload.class);
        if (!cdcProcessor.supports(payload)) {
            log.warn("Unsupported payload. Ignoring. {}", payload);
            return;
        }

        OperationStatus response = cdcProcessor.process(payload);
        message.setProcessingStatus(response);
        message.increaseAttemptCount();

        if (!response.isFinal()) {
            log.debug("Message Processing with reference: {} was not completed, will be re-queued", payload.hash());
            throw new CdcProcessingException(
                "Message with reference: " + payload.hash() + " is not in a final state, will be re-queued.",
                message
            );
        }
    }


}
