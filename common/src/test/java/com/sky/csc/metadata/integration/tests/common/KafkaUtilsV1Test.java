package com.sky.csc.metadata.integration.tests.common;

import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class KafkaUtilsV1Test {
    private static final Logger log = LoggerFactory.getLogger(KafkaUtilsV1Test.class);
    static final String GROUP_ID_PREFIX = "kafka-utils-test";
    static final String TEST_TOPIC_NAME = "some-topic";

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Test
    public void testSubscribeAndStartSearchForRecord() throws Exception {
        // Given a Kafka broker and topic
        final KafkaTestUtils kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
        kafkaTestUtils.createTopic(TEST_TOPIC_NAME, 1, (short) 1);

        // and a Consumer connected to the Kafka broker
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        final KafkaConsumer<String, String> kafkaConsumer = kafkaTestUtils.getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, props);

        // and a Producer connected to the Kafka broker
        props = new Properties();
        final KafkaProducer<String, String> producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class, props);

        // and a test message with key
        final String messageToFind = "testMessage";
        final String keyToFind = "testKey";
        final ProducerRecord<String, String> testRecord = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, messageToFind);

        // and subscribeAndStartSearchForRecord is called with the key to find on the test topic
        log.debug("Invoking subscribeAndStartSearchForRecord for key '" + keyToFind + "' on topic '" + TEST_TOPIC_NAME + "' from Thread: "+ Thread.currentThread().getId());
        final Future<Optional<ConsumerRecord<String, String>>> resultFuture = KafkaUtilsV1.subscribeAndStartSearchForRecord(kafkaConsumer, TEST_TOPIC_NAME, keyToFind, 5, Duration.ofSeconds(1));

        // When the test record is produced to the test topic
        log.debug("Producing test record from Thread: "+ Thread.currentThread().getId());
        producer.send(testRecord);
        producer.flush();

        // Then a record is found for the key
        log.debug("Waiting for subscribeAndStartSearchForRecord results on Thread: "+ Thread.currentThread().getId());
        Optional<ConsumerRecord<String, String>> foundRecord = resultFuture.get();
        log.debug("Received subscribeAndStartSearchForRecord results on Thread: "+ Thread.currentThread().getId());
        Assert.assertTrue(foundRecord.isPresent());

        // Then the record found value is equal to the test record value
        Assert.assertEquals(messageToFind, foundRecord.get().value());
    }
}