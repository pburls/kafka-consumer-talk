package com.sky.csc.generators

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.CompositeParty

class PmpComposites {
    static final objectMapper = new ObjectMapper()

    static CompositeParty generatePartyComposite() {
        def pmpPartyCompositeJson = this.getClass().getResource('/pmp-composites/party.json').text
        pmpPartyCompositeJson = replaceUuidTokens(pmpPartyCompositeJson)
        return objectMapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    static String replaceUuidTokens(String pmpCompositeJson) {
        def tokenRegex = "(<new-uuid-(.)>)"
        def uuidMap = [:]
        return pmpCompositeJson.replaceAll(tokenRegex, { globalCapture, tokenCapture, uuidIdCapture ->
            uuidMap.computeIfAbsent(uuidIdCapture, { it -> UUID.randomUUID().toString() } )
        })
    }
}
