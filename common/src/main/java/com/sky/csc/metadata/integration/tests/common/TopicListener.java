package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Optional;

public interface TopicListener<K, V> {
    String getId();

    String getTopic();

    Optional<ConsumerRecord<K, V>> findRecordByKey(K key, int maxRetries, Duration pollDuration);

    void close();
}
