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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

class TopicListenerImpl<K, V> implements TopicListener<K, V> {
    private static final Logger log = LoggerFactory.getLogger(TopicListenerImpl.class);

    private final String consumerGroupId;
    private final KafkaConsumer<K, V> consumer;
    private final String topic;

    TopicListenerImpl(final Class<? extends Deserializer<K>> keyDeserializer,
                      final Class<? extends Deserializer<V>> valueDeserializer,
                      String consumerGroupIdPrefix,
                      Properties consumerConfig,
                      Function<Properties, KafkaConsumer<K, V>> kafkaConsumerFactory,
                      String topic) {
        this.consumerGroupId = consumerGroupIdPrefix + ".topic-listener-" + Instant.now().toString();
        this.topic = topic;

        if (consumerConfig == null) {
            consumerConfig = new Properties();
        }
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupId);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        this.consumer = kafkaConsumerFactory.apply(consumerConfig);
    }

    @Override
    public String getConsumerGroupId() {
        return this.consumerGroupId;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    public void subscribeAndAwaitPartitionAssignment(Duration assignmentTimeout) throws InterruptedException {
        PartitionAssignmentListener partitionAssignmentListener = new PartitionAssignmentListener(this);
        consumer.subscribe(Collections.singletonList(topic), partitionAssignmentListener);
        log.debug("Waiting for subscription and partition assignment.");
        boolean successful = partitionAssignmentListener.countDownLatch.await(assignmentTimeout.getSeconds(), TimeUnit.SECONDS);
        if (successful) {
            log.debug("Waiting for subscription and partition assignment completed successfully.");
        } else {
            log.debug("Waiting for subscription and partition assignment timed out.");
        }
    }

    @Override
    public Optional<ConsumerRecord<K, V>> findRecord(final Predicate<ConsumerRecord<K, V>> filterCriteria, final int maxRetries, final Duration pollDuration) {
        log.debug("findRecord on topic '{}' started for TopicListener '{}'.", this.topic, this.consumerGroupId);
        int retry = 0;
        Optional<ConsumerRecord<K, V>> foundRecord = Optional.empty();

        while (!foundRecord.isPresent() && retry < maxRetries) {
            retry++;
            ConsumerRecords<K, V> consumerRecords = consumer.poll(pollDuration);
            log.debug("Found {} records during last poll.", consumerRecords.count());
            foundRecord = StreamSupport.stream(consumerRecords.spliterator(), false)
                    .peek(record -> log.debug("Processing record with key '{}' at offset '{}' on TopicListener '{}'", record.key() ,record.offset(), this.consumerGroupId))
                    .filter(filterCriteria)
                    .findFirst();

            consumer.commitSync();
        }
        log.debug("findRecord on topic '{}' completed for TopicListener '{}'.", this.topic, this.consumerGroupId);
        return foundRecord;
    }

    @Override
    public void close() {
        consumer.close();
        log.debug("TopicListener '{}' closed.", this.consumerGroupId);
    }

    class PartitionAssignmentListener implements ConsumerRebalanceListener {
        private boolean isPartitionsAssigned = false;
        private final TopicListenerImpl topicListener;
        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        PartitionAssignmentListener(TopicListenerImpl topicListener) {
            this.topicListener = topicListener;
        }

        public boolean isPartitionsAssigned() {
            return isPartitionsAssigned;
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            partitions.forEach(topicPartition -> log.debug("Partition partitionId '{}' on topic '{}' revoked for TopicListener '{}'.", topicPartition.partition(), topicPartition.topic(), this.topicListener.getConsumerGroupId()));
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            partitions.forEach(topicPartition -> log.debug("Partition partitionId '{}' at offset {} on topic '{}' assigned for TopicListener '{}'.", topicPartition.partition(), this.topicListener.consumer.position(topicPartition), topicPartition.topic(), this.topicListener.getConsumerGroupId()));
            if (!this.isPartitionsAssigned) {
                this.isPartitionsAssigned = true;
                this.countDownLatch.countDown();
            }
        }
    }
}
