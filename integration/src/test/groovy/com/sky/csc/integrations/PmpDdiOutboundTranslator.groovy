package com.sky.csc.integrations

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.ImmutableMap
import com.sky.csc.Configuration
import com.sky.pmp.domain.AbstractCompositeEntity
import com.sky.pmp.domain.OperationType
import com.sky.pmp.testutils.activemq.ActiveMqConfig
import com.sky.pmp.testutils.activemq.ActiveMqTestRunner
import com.sky.pmp.testutils.activemq.Headers
import com.sky.pmp.testutils.activemq.TestMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.sky.pmp.testutils.activemq.ActiveMqConfig.DestinationType.QUEUE

class PmpDdiOutboundTranslator {
    static final Logger log = LoggerFactory.getLogger(PmpDdiOutboundTranslator.class)
    static final objectMapper = new ObjectMapper()

    static sendInputComposite(AbstractCompositeEntity inputComposite) {
        def result = sendMessage(inputComposite)
        log.debug("Sent ${inputComposite.getClass().toString()} input to PMP DDI Outbound Translator")
    }

    static sendMessage(AbstractCompositeEntity inputComposite) throws Exception {
        def headers = ImmutableMap.of(
                Headers.ENTITY_TYPE, inputComposite.whichType().name(),
                Headers.OPERATION_TYPE, OperationType.REPLACE.name(),
                "PmpUuid", inputComposite.pmpUuid(),
                Headers.PMP_MESSAGE_ID, "pmp-core:some-component:1:1",
                Headers.CORE_DELTA_SEQUENCE_NUMBER, 1);

        def messageBody = objectMapper.writeValueAsString(inputComposite)

        def testMessage = new TestMessage(headers, messageBody)

        def config = new ActiveMqConfig(
                Configuration.PmpDdiOutboundTranslatorInputConfig.brokerUrl,
                Configuration.PmpDdiOutboundTranslatorInputConfig.brokerUsername,
                Configuration.PmpDdiOutboundTranslatorInputConfig.brokerPassword
        )

        def activeMqTestRunner = new ActiveMqTestRunner(config)
        activeMqTestRunner.addTestInput(QUEUE, Configuration.PmpDdiOutboundTranslatorInputConfig.queueName, testMessage);
        return activeMqTestRunner.execute();
    }
}
