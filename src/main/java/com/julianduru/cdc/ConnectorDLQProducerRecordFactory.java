package com.julianduru.cdc;

import com.moniepoint.cdc.config.CdcDlqPrefixHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * created by Julian Duru on 24/05/2023
 */
@Component
@RequiredArgsConstructor
public class ConnectorDLQProducerRecordFactory implements CdcDlqProducerRecordFactory {

    private static final ConcurrentHashMap<String, CdcMessage> messageMap = new ConcurrentHashMap<>();

    @Value("${queue.config.consumers.default-group-id}")
    private String groupId;

    private final CdcDlqPrefixHandler dlqPrefixHandler;


    @Override
    public void prepare(ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, Exception exception) {
        Throwable exceptionCause = exception.getCause();

        if (exceptionCause instanceof CdcProcessingException) {
            CdcProcessingException messageProcessingException = (CdcProcessingException) exceptionCause;
            CdcMessage message = messageProcessingException.getCdcMessage();
            messageMap.put(message.getReference(), message);
        }
    }


    @Override
    public ProducerRecord<Object, Object> createProducerRecord(ConsumerRecord<?, ?> record, TopicPartition topicPartition, Headers headers, byte[] key, byte[] value) throws Exception {
        String reference = null;

        if (record == null || record.value() == null) {
            throw new RuntimeException("Record or Record Value is null");
        }

        if (record.topic().startsWith(dlqPrefixHandler.getDLQTopicPrefix())) {
            CdcMessage message = JSONUtil.fromJsonString(record.value().toString(), CdcMessage.class);
            reference = message.getReference();
        }
        else {
            Payload payload = JSONUtil.fromJsonString(record.value().toString(), Payload.class);
            reference = payload.hash();
        }

        CdcMessage cdcMessage = messageMap.remove(reference);

        if (cdcMessage == null) {
            throw new RuntimeException("Payload with Reference: " + reference + " not found in messageMap");
        }

        // TODO: test and confirm this,
        //  also we may not want the retried message written to the same partition
        //  and consumed by the same consumer
        return new ProducerRecord<>(
            getTopicName(topicPartition),
            topicPartition.partition() < 0 ? null : topicPartition.partition(),
            UUID.randomUUID().toString(),
            JSONUtil.asJsonString(cdcMessage),
            headers
        );
    }


    protected String getTopicName(TopicPartition topicPartition) {
        String topic = topicPartition.topic();
        if (topic.startsWith(CdcDlqPrefixHandler.DLQ_TOPIC_PREFIX_ROOT)) {
            return topic;
        }

        return String.format(
            "%s.%s", dlqPrefixHandler.getDLQTopicPrefix(), topic
        );
    }



}
