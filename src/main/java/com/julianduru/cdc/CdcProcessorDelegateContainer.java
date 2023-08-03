package com.julianduru.cdc;

import com.julianduru.cdc.data.OperationStatus;
import com.julianduru.cdc.data.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * created by Julian Duru on 24/02/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcProcessorDelegateContainer {

    private final Set<CdcProcessorDelegate> processorDelegates = new HashSet<>();


    public void registerHandler(CdcProcessorDelegate handler) throws Exception {
        processorDelegates.add(handler);
    }


    public OperationStatus query(String reference, Payload payload) {
        CdcProcessorDelegate delegate = getDelegateForPayload(payload).get();
        return delegate.query(reference, payload);
    }


    public OperationStatus process(String reference, Payload payload) {
        CdcProcessorDelegate delegate = getDelegateForPayload(payload).get();
        return delegate.process(reference, payload);
    }


    public boolean supports(Payload payload) {
        return getDelegateForPayload(payload, false).isPresent();
    }


    private Optional<CdcProcessorDelegate> getDelegateForPayload(Payload payload) {
        return getDelegateForPayload(payload, true);
    }


    private Optional<CdcProcessorDelegate> getDelegateForPayload(Payload payload, boolean throwException) {
        List<CdcProcessorDelegate> delegates = processorDelegates.stream()
            .filter(
                delegate -> delegate.supports(payload)
            )
            .toList();

        if (delegates.isEmpty()) {
            if (throwException) {
                throw new IllegalArgumentException(
                    String.format(
                        "No delegate found for sourceId: %s, payload: %s", payload.sourceId(), payload
                    )
                );
            } else {
                return Optional.empty();
            }
        }

        if (delegates.size() != 1) {
            if (throwException) {
                throw new IllegalArgumentException(
                    String.format(
                        "Cannot proceed. Multiple processors found for sourceId: %s, payload: %s", payload.sourceId(), payload
                    )
                );
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(delegates.get(0));
    }


}
