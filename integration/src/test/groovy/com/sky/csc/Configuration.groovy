package com.sky.csc

class Configuration {
    class PmpDdiOutboundTranslatorInputConfig {
        static final brokerUrl = ""
        static final brokerUsername = System.getenv("pmp.activemq.broker.username")
        static final brokerPassword = System.getenv("pmp.activemq.broker.password")
        static final queueName = "Consumer.OutboundDdi.VirtualTopic.pmp.output"
    }
}
