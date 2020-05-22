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
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class KafkaUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(KafkaUtilsTest.class);
    static final String TEST_TOPIC_NAME = "some-topic";

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    public static KafkaConsumer<String, String> consumerFactory(Properties consumerConfig) {
        return sharedKafkaTestResource.getKafkaTestUtils().getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, consumerConfig);
    }

    @Test
    public void testFindRecordByKeyFindsRecordProducedToTopicAfterSubscriptionIsComplete() throws Exception {
        // Given a Kafka broker and test topic to put the message onto
        final KafkaTestUtils kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
        kafkaTestUtils.createTopic(TEST_TOPIC_NAME, 1, (short) 1);

        // and a test record to find
        final String messageToFind = "testMessage";
        final String keyToFind = "testKey";
        final ProducerRecord<String, String> testRecord = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, messageToFind);

        // and a Producer connected to the Kafka broker
        Properties producerProps = new Properties();
        final KafkaProducer<String, String> producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class, producerProps);

        // and a Consumer connected to the Kafka broker and subscribed to the test topic
        Properties consumerProps = new Properties();
        log.debug("Subscribing to topic '{}'", TEST_TOPIC_NAME);
        final TopicListener<String, String> topicListener = KafkaUtils.createTopicSubscription(StringDeserializer.class, StringDeserializer.class, consumerProps, KafkaUtilsTest::consumerFactory, TEST_TOPIC_NAME, Duration.ofSeconds(10));

        // When the test record is produced to the test topic
        log.debug("Producing test record");
        producer.send(testRecord);
        producer.flush();

        // Then a record is found for the key
        log.debug("Invoking findRecordByKey for key '{}' on topic '{}'", keyToFind, TEST_TOPIC_NAME);
        final Optional<ConsumerRecord<String, String>> foundRecord = topicListener.findRecordByKey(keyToFind, 10, Duration.ofSeconds(1));
        log.debug("Received findRecordByKey results");
        Assert.assertTrue(foundRecord.isPresent());

        // Then the record found value is equal to the test record value
        Assert.assertEquals(messageToFind, foundRecord.get().value());
    }

    @Test
    public void testFindRecordByKeyIgnoresRecordsProducedBeforeSubscription() throws Exception {
        // Given a Kafka broker and test topic to put the messages onto
        final KafkaTestUtils kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
        kafkaTestUtils.createTopic(TEST_TOPIC_NAME, 1, (short) 1);

        // and a test record to find
        final String messageToFind = "testMessage";
        final String keyToFind = "testKey";
        final ProducerRecord<String, String> testRecord = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, messageToFind);

        // and a couple old records with same key produced before the find
        final ProducerRecord<String, String> oldRecord1 = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, "oldMessage1");
        final ProducerRecord<String, String> oldRecord2 = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, "oldMessage2");

        // and a Producer connected to the Kafka broker
        Properties producerProps = new Properties();
        final KafkaProducer<String, String> producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class, producerProps);

        // and the old records are produced to the test topic
        log.debug("Producing old test record");
        producer.send(oldRecord1);
        producer.send(oldRecord2);
        producer.flush();

        // and a Consumer connected to the Kafka broker and subscribed to the test topic
        Properties consumerProps = new Properties();
        log.debug("Subscribing to topic '{}'", TEST_TOPIC_NAME);
        final TopicListener<String, String> topicListener = KafkaUtils.createTopicSubscription(StringDeserializer.class, StringDeserializer.class, consumerProps, KafkaUtilsTest::consumerFactory, TEST_TOPIC_NAME, Duration.ofSeconds(10));

        // When the test record is produced to the test topic
        log.debug("Producing test record");
        producer.send(testRecord);
        producer.flush();

        // Then a record is found for the key
        log.debug("Invoking findRecordByKey for key '{}' on topic '{}'", keyToFind, TEST_TOPIC_NAME);
        final Optional<ConsumerRecord<String, String>> foundRecord = topicListener.findRecordByKey(keyToFind, 10, Duration.ofSeconds(1));
        log.debug("Received findRecordByKey results");
        Assert.assertTrue(foundRecord.isPresent());

        // and the record found value is equal to the test record value produced after the subscription
        Assert.assertEquals(messageToFind, foundRecord.get().value());
    }
}