package com.sky.csc.metadata.ddi


import com.sky.csc.generators.PmpComposites
import com.sky.csc.integrations.CsaPersistedTopics
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
        def pmpUUID = pmpPartyComposite.getKeyBag().sourceSpecificReferenceValue()
        def pdUUID = pmpPartyComposite.getKeyBag().getAlternativeReferences()
                .find({reference -> reference.source == Source.PD})
                .getValue()
        log.debug("PMP Party composite with PMP Reference '${pmpUUID}' and PD Reference '${pdUUID}'")

        when: "the party composite is added to the PMP DDI Outbound Translator input queue"
        PmpDdiOutboundTranslator.sendInputComposite(pmpPartyComposite)

        then: "a DDI Person fragment should be created"
        def ddiPersonFragment = CsaPersistedTopics.getDdiFragmentForKey(DdiFragmentType.Person, pmpUUID)
        ddiPersonFragment
        //assert all the values on the ddiPersonFragment are equal to the pmpPartyComposite's values

        and: "a Merling Person object should be created"
        def merlinPersonObject //= getMerlingObject("Person", pmpUUID)
        merlinPersonObject
        //assert all the values on the merlinPersonObject are equal to the pmpPartyComposite's values
    }

    def Object getMerlingObject(String objectType, String uuid) {
        // find a way to get any created merlin objects from the Merlin stub/mock
        return null;
    }
}
