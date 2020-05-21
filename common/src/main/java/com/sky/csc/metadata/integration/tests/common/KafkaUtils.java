package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

public class KafkaUtils {
    static CompletableFuture<Optional<ConsumerRecord<String, String>>> findRecordByKey(final KafkaConsumer<String, String> consumer,
                                                               final String topic,
                                                               final String key,
                                                               final int maxRetries,
                                                               final Duration pollDuration) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Asynchronous findRecordByKey for key '" + key + "' on topic '" + topic + "' started on Thread: "+ Thread.currentThread().getId());
            int retry = 0;
            Optional<ConsumerRecord<String, String>> foundRecord = Optional.empty();
            consumer.subscribe(Collections.singletonList(topic));

            while (!foundRecord.isPresent() && retry < maxRetries) {
                retry++;
                ConsumerRecords<String, String> consumerRecords = consumer.poll(pollDuration);
                System.out.println("Found " + consumerRecords.count() + " records during last poll.");
                foundRecord = StreamSupport.stream(consumerRecords.spliterator(), false)
                        .peek(record -> System.out.println("Processing record with key '" + record.key() + "' at offset: " + record.offset()))
                        .filter(record -> record.key().equals(key))
                        .findFirst();

                consumer.commitSync();
            }
            consumer.close();
            System.out.println("Asynchronous findRecordByKey for key '" + key + "' on topic '" + topic + "' completed on Thread: "+ Thread.currentThread().getId());
            return foundRecord;
        });
    }
}
