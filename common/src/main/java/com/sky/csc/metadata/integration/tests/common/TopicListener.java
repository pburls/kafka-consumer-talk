package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;

public interface TopicListener<K, V> {
    String getConsumerGroupId();

    String getTopic();

    Optional<ConsumerRecord<K, V>> findRecord(Predicate<ConsumerRecord<K, V>> filterCriteria, int maxRetries, Duration pollDuration);

    void close();
}
