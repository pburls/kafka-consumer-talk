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

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class KafkaUtilsTest {
    static final String TEST_TOPIC_NAME = "some-topic";

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFindRecordByKey() throws Exception {
        // Given a Kafka broker and topic
        final KafkaTestUtils kafkaTestUtils = sharedKafkaTestResource.getKafkaTestUtils();
        kafkaTestUtils.createTopic(TEST_TOPIC_NAME, 1, (short) 1);

        // and a Consumer connected to the Kafka broker
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final KafkaConsumer<String, String> kafkaConsumer = kafkaTestUtils.getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, props);

        // and a Producer connected to the Kafka broker
        props = new Properties();
        final KafkaProducer<String, String> producer = kafkaTestUtils.getKafkaProducer(StringSerializer.class, StringSerializer.class, props);

        // and a test record
        final String messageToFind = "testMessage";
        final String keyToFind = "testKey";
        final ProducerRecord<String, String> testRecord = new ProducerRecord<>(TEST_TOPIC_NAME, keyToFind, messageToFind);

        // When findRecordByKey is called with the key to find on the test topic
        System.out.println("Invoking findRecordByKey for key '" + keyToFind + "' on topic '" + TEST_TOPIC_NAME + "' from Thread: "+ Thread.currentThread().getId());
        final CompletableFuture<Optional<ConsumerRecord<String, String>>> resultFuture = KafkaUtils.findRecordByKey(kafkaConsumer, TEST_TOPIC_NAME, keyToFind, 5, Duration.ofSeconds(1));

        // and when the test record is produced to the test topic
        System.out.println("Producing test record from Thread: "+ Thread.currentThread().getId());
        producer.send(testRecord);
        producer.flush();

        // Then a record is found for the key
        System.out.println("Waiting for findRecordByKey results on Thread: "+ Thread.currentThread().getId());
        Optional<ConsumerRecord<String, String>> foundRecord = resultFuture.get();
        System.out.println("Received findRecordByKey results on Thread: "+ Thread.currentThread().getId());
        Assert.assertTrue(foundRecord.isPresent());

        // Then the record found value is equal to the test record value
        Assert.assertEquals(messageToFind, foundRecord.get().value());
    }
}