package com.julianduru.cdc.processing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

/**
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockObject<T> {

    private Hashable hashable;

    private MessageRecord<T> messageRecord;

    private Consumer<MessageRecord<T>> consumer;

}
