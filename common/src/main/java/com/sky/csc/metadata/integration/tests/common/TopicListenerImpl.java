package com.sky.csc.metadata.integration.tests.common;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

class TopicListenerImpl<K, V> implements TopicListener<K, V> {
    private static final Logger log = LoggerFactory.getLogger(TopicListenerImpl.class);

    private final String id;
    private final KafkaConsumer<K, V> consumer;
    private final String topic;

    TopicListenerImpl(final Class<? extends Deserializer<K>> keyDeserializer,
                      final Class<? extends Deserializer<V>> valueDeserializer,
                      Properties consumerConfig,
                      Function<Properties, KafkaConsumer<K, V>> kafkaConsumerFactory,
                      String topic) {
        this.id = Instant.now().toString();
        this.topic = topic;

        if (consumerConfig == null) {
            consumerConfig = new Properties();
        }
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "test-tl-group-" + this.id);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        this.consumer = kafkaConsumerFactory.apply(consumerConfig);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    public void subscribeAndAwaitPartitionAssignment(Duration assignmentTimeout) throws TimeoutException {
        PartitionAssignmentListener partitionAssignmentListener = new PartitionAssignmentListener(this);
        consumer.subscribe(Collections.singletonList(topic), partitionAssignmentListener);
        // give the main thread to the consumer to request and been assigned partitions
        log.debug("Starting poll to complete subscription and partition assignment");
        try {
            ConsumerRecords<K, V> consumerRecords = consumer.poll(assignmentTimeout);
            log.warn("Awaiting partition assignment poll returned {} records", consumerRecords.count());
            throw new TimeoutException("TopicListener partition assignment wait timeout duration exceeded.");
        } catch (WakeupException e) {
            if (!partitionAssignmentListener.isPartitionsAssigned()) throw e;
        }
        log.debug("Subscription and partition assignment finished.");
        consumer.assignment().forEach(topicPartition -> {
            long offset = consumer.position(topicPartition);
            log.debug("Starting offset topic '{}' partition '{}' is: {}", topicPartition.topic(), topicPartition.partition(), offset);
        });
    }

    @Override
    public Optional<ConsumerRecord<K, V>> findRecord(final Predicate<ConsumerRecord<K, V>> filterCriteria, final int maxRetries, final Duration pollDuration) {
        log.debug("findRecord on topic '{}' started for TopicListener '{}'.", this.topic, this.id);
        int retry = 0;
        Optional<ConsumerRecord<K, V>> foundRecord = Optional.empty();

        while (!foundRecord.isPresent() && retry < maxRetries) {
            retry++;
            ConsumerRecords<K, V> consumerRecords = consumer.poll(pollDuration);
            log.debug("Found {} records during last poll.", consumerRecords.count());
            foundRecord = StreamSupport.stream(consumerRecords.spliterator(), false)
                    .peek(record -> log.debug("Processing record with key '{}' at offset '{}' on TopicListener '{}'", record.key() ,record.offset(), this.id))
                    .filter(filterCriteria)
                    .findFirst();

            consumer.commitSync();
        }
        log.debug("findRecord on topic '{}' completed for TopicListener '{}'.", this.topic, this.id);
        return foundRecord;
    }

    @Override
    public void close() {
        consumer.close();
        log.debug("TopicListener '{}' closed.", this.id);
    }

    class PartitionAssignmentListener implements ConsumerRebalanceListener {
        private boolean isPartitionsAssigned = false;
        private final TopicListenerImpl topicListener;

        PartitionAssignmentListener(TopicListenerImpl topicListener) {
            this.topicListener = topicListener;
        }

        public boolean isPartitionsAssigned() {
            return isPartitionsAssigned;
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            partitions.forEach(topicPartition -> log.debug("Partition partitionId '{}' on topic '{}' revoked for TopicListener '{}'.", topicPartition.partition(), topicPartition.topic(), this.topicListener.getId()));
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            partitions.forEach(topicPartition -> log.debug("Partition partitionId '{}' on topic '{}' assigned for TopicListener '{}'.", topicPartition.partition(), topicPartition.topic(), this.topicListener.getId()));
            this.isPartitionsAssigned = true;
            this.topicListener.consumer.wakeup();
        }
    }
}
