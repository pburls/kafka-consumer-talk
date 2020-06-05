package com.sky.csc.generators

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.AbstractCompositeEntity
import com.sky.pmp.domain.CompositeParty
import com.sky.pmp.domain.Source

class PmpComposites {
    static final objectMapper = new ObjectMapper()

    static getEntityUuidFromComposite(AbstractCompositeEntity compositeEntity) {
        return compositeEntity.getKeyBag().getAlternativeReferences().find({reference -> reference.source == Source.PD}).getValue()
    }

    static CompositeParty generatePartyComposite() {
        def pmpPartyCompositeJson = getPmpCompositeTemplateJson("party.json")
        pmpPartyCompositeJson = replaceUuidTokens(pmpPartyCompositeJson)
        return objectMapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    private static getPmpCompositeTemplateJson(String templateFileName) {
        def resourceFilePath = "/pmp-composites/${templateFileName}"
        def pmpResourceFileUrl = this.getClass().getResource(resourceFilePath)
        if (!pmpResourceFileUrl) {
            throw new RuntimeException("Unable to find resource file with path '${resourceFilePath}'")
        }

        return pmpResourceFileUrl.text
    }

    private static String replaceUuidTokens(String pmpCompositeJson) {
        def tokenRegex = "(<new-uuid-(.)>)"
        def uuidMap = [:]
        return pmpCompositeJson.replaceAll(tokenRegex, { globalCapture, tokenCapture, uuidIdCapture ->
            uuidMap.computeIfAbsent(uuidIdCapture, { it -> UUID.randomUUID().toString() } )
        })
    }
}
