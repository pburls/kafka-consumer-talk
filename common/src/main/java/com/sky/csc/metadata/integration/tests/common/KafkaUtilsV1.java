package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KafkaUtilsV1 {
    private static final Logger log = LoggerFactory.getLogger(KafkaUtilsV1.class);

    public static Future<Optional<ConsumerRecord<String, String>>> subscribeAndStartSearchForRecord(final KafkaConsumer<String, String> consumer,
                                                                                                    final String topic,
                                                                                                    final String key,
                                                                                                    final int maxRetries,
                                                                                                    final Duration pollDuration) {
        consumer.subscribe(Collections.singletonList(topic));
        log.debug("Current assigned partitions: " + consumer.assignment().stream().map(partition -> "" + partition.partition() + "@" + consumer.position(partition)).collect(Collectors.joining(",")));
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Asynchronous subscribeAndStartSearchForRecord for key '" + key + "' on topic '" + topic + "' started on Thread: "+ Thread.currentThread().getId());
            int retry = 0;
            Optional<ConsumerRecord<String, String>> foundRecord = Optional.empty();

            while (!foundRecord.isPresent() && retry < maxRetries) {
                retry++;
                ConsumerRecords<String, String> consumerRecords = consumer.poll(pollDuration);
                log.debug("Found " + consumerRecords.count() + " records during last poll.");
                log.debug("Current assigned partitions: " + consumer.assignment().stream().map(partition -> "" + partition.partition() + "@" + consumer.position(partition)).collect(Collectors.joining(",")));
                foundRecord = StreamSupport.stream(consumerRecords.spliterator(), false)
                        .peek(record -> log.debug("Processing record with key '" + record.key() + "' at offset: " + record.offset()))
                        .filter(record -> record.key().equals(key))
                        .findFirst();

                consumer.commitSync();
            }
            consumer.close();
            log.debug("Asynchronous subscribeAndStartSearchForRecord for key '" + key + "' on topic '" + topic + "' completed on Thread: "+ Thread.currentThread().getId());
            return foundRecord;
        });
    }
}
