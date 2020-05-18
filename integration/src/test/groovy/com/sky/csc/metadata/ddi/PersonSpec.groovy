package com.sky.csc.metadata.ddi

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.AbstractCompositeEntity
import com.sky.pmp.domain.CompositeParty
import spock.lang.Specification

class PersonSpec extends Specification {
    def "PMP Party composite to DDI PERSON fragment to Merlin Person object"() {
        given: "A PMP Party composite"
        def pmpPartyComposite = generatePMPPartyComposite()
        def uuid = pmpPartyComposite.getKeyBag().getSourceSpecificReference().getValue()

        when: "the party composite is added to the PMP DDI Outbound Translator input queue"
        sendPmpDdiOutboundTranslatorInput(pmpPartyComposite)

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

    def sendPmpDdiOutboundTranslatorInput(AbstractCompositeEntity inputComposite) {
        // find a way to add this to the pmp.outbound.ddi.translator.input.endpoint queue
    }

    def generatePMPPartyComposite() {
        def pmpPartyCompositeJson = this.getClass().getResource('/pmp-composites/party.json').text
        pmpPartyCompositeJson = replaceUuidToken(pmpPartyCompositeJson)
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    def String replaceUuidToken(String pmpCompositeJson) {
        def tokenToReplace = "<to-be-replaced-with-new-uuid>"
        def uuid = UUID.randomUUID().toString()
        return pmpCompositeJson.replaceAll(tokenToReplace, uuid)
    }
}
