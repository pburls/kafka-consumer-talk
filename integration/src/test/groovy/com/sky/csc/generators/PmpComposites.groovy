package com.sky.csc.generators

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.CompositeParty

class PmpComposites {
    static CompositeParty generatePartyComposite() {
        def pmpPartyCompositeJson = this.getClass().getResource('/pmp-composites/party.json').text
        pmpPartyCompositeJson = replaceUuidTokens(pmpPartyCompositeJson)
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    static String replaceUuidTokens(String pmpCompositeJson) {
        def tokenRegex = "(<new-uuid-(.)>)"
        def uuidMap = [:]
        return pmpCompositeJson.replaceAll(tokenRegex, { globalCapture, tokenCapture, uuidIdCapture ->
            uuidMap.computeIfAbsent(uuidIdCapture, { it -> UUID.randomUUID().toString() } )
        })
    }
}
