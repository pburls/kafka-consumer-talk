package com.sky.csc.generators

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.CompositeParty

class PmpComposites {
    static final objectMapper = new ObjectMapper()

    static CompositeParty generatePartyComposite() {
        def pmpPartyCompositeJson = getPmpCompositeTemplateJson("party.json")
        pmpPartyCompositeJson = replaceUuidTokens(pmpPartyCompositeJson)
        return objectMapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    static getPmpCompositeTemplateJson(String templateFileName) {
        def resourceFilePath = "/pmp-composites/${templateFileName}"
        def pmpResourceFileUrl = this.getClass().getResource(resourceFilePath)
        if (!pmpResourceFileUrl) {
            throw new RuntimeException("Unable to find resource with path '${resourceFilePath}'")
        }

        return pmpResourceFileUrl.text
    }

    static String replaceUuidTokens(String pmpCompositeJson) {
        def tokenRegex = "(<new-uuid-(.)>)"
        def uuidMap = [:]
        return pmpCompositeJson.replaceAll(tokenRegex, { globalCapture, tokenCapture, uuidIdCapture ->
            uuidMap.computeIfAbsent(uuidIdCapture, { it -> UUID.randomUUID().toString() } )
        })
    }
}
