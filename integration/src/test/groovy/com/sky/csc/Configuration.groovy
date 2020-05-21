package com.sky.csc

class Configuration {
    class PmpDdiOutboundTranslatorInputConfig {
        static final brokerUrl = ""
        static final brokerUsername = System.getenv("pmp.activemq.broker.username")
        static final brokerPassword = System.getenv("pmp.activemq.broker.password")
        static final queueName = "Consumer.OutboundDdi.VirtualTopic.pmp.output"
    }

    class CsaPersistedTopicsConsumerConfig {
        static final bootstrapServers = "bootstrap.servers=kfk-shared-broker-1.dev.awscsc.skyott.com:9093,kfk-shared-broker-2.dev.awscsc.skyott.com:9093,kfk-shared-broker-3.dev.awscsc.skyott.com:9093"
        static final groupId = "sky.csc.int.csc-metadata-tests.e2e-int-tests"
    }
}
