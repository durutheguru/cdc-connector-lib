package com.julianduru.cdc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * created by Julian Duru on 29/04/2023
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcConsumer implements Consumer {


    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;


    private final CdcProcessor cdcProcessor;



    @Override
    public void consume(ConsumerRecord<String, String> record) throws Exception {
        log.info("Message received: " + record);

        if (record == null || record.value() == null) { // should not happen, indicates an abnormality
            log.warn("Record or Record Value is null. {}", record != null ? record.key() : "");
            return;
        }

        Payload payload = JSONUtil.fromJsonString(record.value(), Payload.class);
        if (!cdcProcessor.supports(payload)) {
            log.warn("Unsupported payload. Ignoring. {}", payload);
            return;
        }

        OperationStatus response = cdcProcessor.process(payload);
        if (!response.isFinal()) {
            CdcMessage message = CdcMessage.fromRecord(record, payload, groupId);
            message.setProcessingStatus(response);

            log.debug("Message Processing with reference: {} was not completed, will be re-queued", payload.hash());
            throw new CdcProcessingException(
                "Message with reference: " + payload.hash() + " is not in a final state, will be re-queued.",
                message
            );
        }
    }


}

