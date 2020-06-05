package com.sky.csc.metadata.ddi


import com.sky.csc.generators.PmpComposites
import com.sky.csc.integrations.CsaPersistedTopics
import com.sky.csc.integrations.MerlinMock
import com.sky.csc.integrations.PmpDdiOutboundTranslator
import com.sky.pmp.domain.Source
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class PersonSpec extends Specification {
    static final Logger log = LoggerFactory.getLogger(PersonSpec.class);

    def "PMP Party composite to DDI PERSON fragment to Merlin Person object"() {
        given: "A PMP Party composite"
        def pmpPartyComposite = PmpComposites.generatePartyComposite()
        def entityUUID = PmpComposites.getEntityUuidFromComposite(pmpPartyComposite)
        log.debug("PMP Party composite with PD Reference '${entityUUID}'")

        and: "a TopicListener for the CSA DDI PERSON persisted topic"
        def personTopicListener = CsaPersistedTopics.createTopicListener(DdiFragmentType.Person)

        when: "the party composite is added to the PMP DDI Outbound Translator input queue"
        PmpDdiOutboundTranslator.sendInputComposite(pmpPartyComposite)

        then: "a DDI Person fragment should be created"
        def ddiPersonFragment = CsaPersistedTopics.getDdiFragmentForKey(personTopicListener, DdiFragmentType.Person, entityUUID)
        ddiPersonFragment
        //assert all the values on the ddiPersonFragment are equal to the pmpPartyComposite's values

        and: "a Merlin Person object should be created"
        def merlinPersonObject = MerlinMock.getMerlinObject(DdiFragmentType.Person, entityUUID)
        merlinPersonObject
        //assert all the values on the merlinPersonObject are equal to the pmpPartyComposite's values
    }
}
