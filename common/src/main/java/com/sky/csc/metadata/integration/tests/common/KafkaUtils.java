package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class KafkaUtils {
    public static <K, V> TopicListener<K, V> createTopicSubscription(final Class<? extends Deserializer<K>> keyDeserializer,
                                                                     final Class<? extends Deserializer<V>> valueDeserializer,
                                                                     Properties consumerConfig,
                                                                     Function<Properties, KafkaConsumer<K, V>> kafkaConsumerFactory,
                                                                     String topic,
                                                                     Duration subscriptionTimeout) throws InterruptedException, TimeoutException, ExecutionException {
        TopicListener listener = new TopicListener(keyDeserializer, valueDeserializer, consumerConfig, kafkaConsumerFactory, topic);
        listener.subscribeAndAwaitPartitionAssignment(subscriptionTimeout);
        return listener;
    }
}
