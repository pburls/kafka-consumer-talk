package com.sky.csc.integrations

import com.sky.csc.Configuration
import com.sky.csc.metadata.ddi.DdiFragmentType
import com.sky.csc.metadata.integration.tests.common.KafkaUtils
import com.sky.kaas.factory.ClientFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import java.time.Duration

class CsaPersistedTopics {
    static final Logger log = LoggerFactory.getLogger(CsaPersistedTopics.class);

    static final kafkaConsumerFactory = { Properties consumerConfig ->
        log.debug("Creating Kafka Consumer")
        return new ClientFactory<String, String>().createKafkaConsumer(consumerConfig)
    };

    static addKafkaClientSecurityConfig(Properties clientProps) {
        def truststoreResourceURL = CsaPersistedTopics.class.getResource(Configuration.CsaPersistedTopicsConsumerConfig.ClientSecurity.truststoreResourceLocation)
        def truststoreLocation = Paths.get(truststoreResourceURL.toURI()).toFile();
        def keystoreResourceURL = CsaPersistedTopics.class.getResource(Configuration.CsaPersistedTopicsConsumerConfig.ClientSecurity.keystoreResourceLocation)
        def keystoreLocation = Paths.get(keystoreResourceURL.toURI()).toFile();

        clientProps.put("security.protocol", Configuration.CsaPersistedTopicsConsumerConfig.ClientSecurity.securityProtocol)
        clientProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation.toString())
        clientProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, Configuration.CsaPersistedTopicsConsumerConfig.ClientSecurity.password)
        clientProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation.toString())
        clientProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, Configuration.CsaPersistedTopicsConsumerConfig.ClientSecurity.password)
    }

    static Optional<ConsumerRecord<String, String>> findKeyOnKafkaTopic(String keyToFind, String topicName, Duration subscriptionTimeout, int maxPolls, Duration pollDurationTimeout) {
        def consumerGroupIdPrefix = Configuration.CsaPersistedTopicsConsumerConfig.groupIdPrefix

        def consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.CsaPersistedTopicsConsumerConfig.bootstrapServers)
        addKafkaClientSecurityConfig(consumerProps)

        def topicListener = KafkaUtils.createTopicListener(StringDeserializer.class, StringDeserializer.class, consumerGroupIdPrefix, consumerProps, kafkaConsumerFactory, topicName, subscriptionTimeout);
        return topicListener.findRecord({ record -> (record.key() == keyToFind) }, maxPolls, pollDurationTimeout);
    }

    static Object getDdiFragmentForKey(DdiFragmentType fragmentType, String uuid) {
        if (!Configuration.ddiFragmentTypeToTopicMap.containsKey(fragmentType.name())) {
            throw new IllegalArgumentException("Topic mapping for fragmentType '${fragmentType}' has not yet been configured")
        }
        def topicName = Configuration.ddiFragmentTypeToTopicMap[fragmentType.name()]
        def keyToFind = "uk:DDI:${fragmentType}:${uuid}"
        log.debug("Searching for key '${keyToFind}' on topic '${topicName}'.")

        // wait for the fragment to appear on the kafka topic
        def findResult = findKeyOnKafkaTopic(
                keyToFind,
                topicName,
                Configuration.CsaPersistedTopicsConsumerConfig.topicSubscriptionTimeout,
                Configuration.CsaPersistedTopicsConsumerConfig.findMaxPollAttempts,
                Configuration.CsaPersistedTopicsConsumerConfig.findPollTimeout
        )

        if(findResult.isPresent()) {
            return findResult.get()
        } else {
            log.error("Failed to find key '${keyToFind}' on topic '${topicName}' after ${Configuration.CsaPersistedTopicsConsumerConfig.findMaxPollAttempts}x${Configuration.CsaPersistedTopicsConsumerConfig.findPollTimeout} poll attempts.")
            return null
        }
    }
}
