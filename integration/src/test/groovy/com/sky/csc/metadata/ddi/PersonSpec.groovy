package com.sky.csc.metadata.ddi


import com.sky.csc.generators.PmpComposites
import com.sky.csc.integrations.PmpDdiOutboundTranslatorInput
import spock.lang.Specification

class PersonSpec extends Specification {
    def "PMP Party composite to DDI PERSON fragment to Merlin Person object"() {
        given: "A PMP Party composite"
        def pmpPartyComposite = PmpComposites.generatePartyComposite()
        def uuid = pmpPartyComposite.getKeyBag().getSourceSpecificReference().getValue()

        when: "the party composite is added to the PMP DDI Outbound Translator input queue"
        PmpDdiOutboundTranslatorInput.sendInputComposite(pmpPartyComposite)

        then: "a DDI Person fragment should be created"
        def ddiPersonFragment = getDdiFragmentForKey("PERSON", uuid)
        ddiPersonFragment
        //assert all the values on the ddiPersonFragment are equal to the pmpPartyComposite's values

        and: "a Merling Person object should be created"
        def merlinPersonObject = getMerlingObject("Person", uuid)
        merlinPersonObject
        //assert all the values on the merlinPersonObject are equal to the pmpPartyComposite's values
    }

    def Object getMerlingObject(String objectType, String uuid) {
        // find a way to get any created merlin objects from the Merlin stub/mock
    }

    def Object getDdiFragmentForKey(String fragmentType, String uuid) {
        // find a way to wait for the fragment to appear on the kafka topic
    }
}
