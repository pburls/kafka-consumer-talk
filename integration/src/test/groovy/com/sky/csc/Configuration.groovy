package com.sky.csc

import com.sky.csc.metadata.ddi.model.Person

import java.time.Duration

class Configuration {
    class PmpDdiOutboundTranslatorInputConfig {
        static final brokerUrl = "tcp://vm009075.bskyb.com:41616"
        static final brokerUsername = SecretsHelper.decrypt("/8XA650BMy+7CYYJYhyMgg==")
        static final brokerPassword = SecretsHelper.decrypt("/8XA650BMy+7CYYJYhyMgg==")
        static final queueName = "Consumer.OutboundDdi.e2e.VirtualTopic.pmp.output"
    }

    class CsaPersistedTopicsConsumerConfig {
        static final bootstrapServers = "bootstrap.servers=kfk-shared-broker-1.dev.awscsc.skyott.com:9093,kfk-shared-broker-2.dev.awscsc.skyott.com:9093,kfk-shared-broker-3.dev.awscsc.skyott.com:9093"
        static final groupIdPrefix = "sky.csc.int.csc-metadata-tests.e2e-int-tests"
        static final topicSubscriptionTimeout = Duration.ofSeconds(10)
        static final findMaxPollAttempts = 10
        static final findPollTimeout = Duration.ofSeconds(1)

        class ClientSecurity {
            static final securityProtocol = "SSL"
            static final truststoreResourceLocation = "/kafka-ssl/csc-metadata-tests.kafka.truststore.jks"
            static final keystoreResourceLocation = "/kafka-ssl/csc-metadata-tests.kafka.keystore.jks"
            static final password = SecretsHelper.decrypt("xHUpCxeNePOlNT5NKw3Wmk4gMOZWOJgnVTL5rJW9DPA=")
        }
    }

    static final ddiFragmentTypeConfigMap = [
            Person: [
                    topicName: "sky.csc.int.ddi.person",
                    modelClass: Person.class
            ]
    ]

    class MerlinMockConfig {
        static final hostUrl = "http://astrolabe-merlin-mock-int.dev.cosmic.sky"
        static final requestsEndpoint = "/requests"
    }

    static getEnvironmentVariable(String name) {
        if (!System.getenv().containsKey(name)) {
            throw new RuntimeException("Configuration Environment Variable '${name}' is not set.")
        }
        return System.getenv(name)
    }
}
