package com.sky.csc.metadata.ddi

import com.sky.csc.categories.DDI
import com.sky.csc.categories.Done
import com.sky.csc.generators.PmpComposites
import com.sky.csc.integrations.CsaPersistedTopics
import com.sky.csc.integrations.MerlinMock
import com.sky.csc.integrations.PmpDdiOutboundTranslator
import com.sky.csc.metadata.ddi.model.Person
import org.junit.experimental.categories.Category
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.validation.Validation

@Category(DDI)
class PersonSpec extends Specification {
    static final log = LoggerFactory.getLogger(PersonSpec.class);
    static final validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Category(Done)
    def "PMP Party composite to DDI PERSON fragment to Merlin Person object"() {
        given: "A PMP Party composite"
        def pmpPartyComposite = PmpComposites.generatePartyComposite()
        def entityUUID = PmpComposites.getEntityUuidFromComposite(pmpPartyComposite)
        log.debug("Created PMP Party composite with PD Reference '${entityUUID}'")

        and: "a TopicListener for the CSA DDI Person persisted topic"
        def personTopicListener = CsaPersistedTopics.createTopicListener(DdiFragmentType.Person)

        when: "the party composite is added to the PMP DDI Outbound Translator input queue"
        PmpDdiOutboundTranslator.sendInputComposite(pmpPartyComposite)

        then: "a DDI PERSON fragment record should be found on the topic"
        def ddiPersonFragment = CsaPersistedTopics.getDdiFragmentForKey(personTopicListener, DdiFragmentType.Person, entityUUID)
        ddiPersonFragment

        and: "the fragment object should adhere to the DDI PERSON schema"
        def violations = validator.validate(ddiPersonFragment as Person)
        !violations

        and: "a Merlin Person object should be found in the Merlin Mock"
        def merlinPersonObject = MerlinMock.getMerlinObject(DdiFragmentType.Person, entityUUID)
        merlinPersonObject
    }
}
