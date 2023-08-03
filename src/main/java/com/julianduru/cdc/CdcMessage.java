package com.julianduru.cdc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.ZonedDateTime;

/**
 * created by julian on 23/02/2023
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CdcMessage {


    private String groupId;


    private String reference;


    private String topic;


    private String header;


    private String payload;


    @JsonIgnore
    private ZonedDateTime treatedDate;


    private OperationStatus processingStatus;


    private int attemptCount;


    public static CdcMessage fromRecord(ConsumerRecord<?, ?> record, Payload payload, String groupId) throws JsonProcessingException {
        return CdcMessage.builder()
            .groupId(groupId)
            .topic(record.topic())
            .reference(payload.hash())
            .payload(JSONUtil.asJsonString(payload))
            .build();
    }


    public CdcMessage increaseAttemptCount() {
        attemptCount++;
        return this;
    }


}


