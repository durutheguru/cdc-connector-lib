package com.julianduru.cdc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * created by Julian Duru on 01/05/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcProcessor {


    private final CdcProcessorDelegateContainer cdcProcessorDelegateContainer;



    public OperationStatus process(Payload payload) {
        String reference = payload.hash();
        OperationStatus response = cdcProcessorDelegateContainer.query(reference, payload);
        log.debug("Querying Message Reference: {}. Response: {}", reference, response);

        if (response.getStatus().isRetryable()) {
            response = cdcProcessorDelegateContainer.process(reference, payload);
        }

        return response;
    }


    public boolean supports(Payload payload) {
        return cdcProcessorDelegateContainer.supports(payload);
    }



}




