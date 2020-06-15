package com.sky.csc.metadata.ddi

import com.sky.csc.categories.DDI
import com.sky.csc.categories.Done
import com.sky.csc.categories.WIP
import com.sky.csc.generators.PmpComposites
import com.sky.csc.integrations.CsaPersistedTopics
import com.sky.csc.integrations.MerlinMock
import com.sky.csc.integrations.PmpDdiOutboundTranslator
import com.sky.csc.metadata.ddi.model.Person
import org.junit.experimental.categories.Category
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.validation.Validation

@Category(DDI)
class PersonSpec extends Specification {
    static final log = LoggerFactory.getLogger(PersonSpec.class);
    static final validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Category(WIP)
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
        Person ddiPersonFragment = CsaPersistedTopics.getDdiFragmentForKey(personTopicListener, DdiFragmentType.Person, entityUUID)
        def violations = validator.validate(ddiPersonFragment)
        !violations
        //assert all the values on the ddiPersonFragment are equal to the pmpPartyComposite's values

        and: "a Merlin Person object should be created"
        def merlinPersonObject = MerlinMock.getMerlinObject(DdiFragmentType.Person, entityUUID)
        merlinPersonObject
        //assert all the values on the merlinPersonObject are equal to the pmpPartyComposite's values
    }

    @Category(Done)
    def "Example Done DDI PERSON fragment test"() {
        given: "nothing"
        def a = 1
        def b = 1

        when: "something happens"
        def result = a + b

        then: "pass"
        result == 2
    }

    def "Example un-categorised DDI PERSON fragment test"() {
        given: "nothing"
        def a = 1
        def b = 1

        when: "something happens"
        def result = a + b

        then: "pass"
        result == 2
    }
}
