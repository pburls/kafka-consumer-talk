package com.sky.csc.generators

import com.fasterxml.jackson.databind.ObjectMapper
import com.sky.pmp.domain.CompositeParty

class PmpComposites {
    static CompositeParty generatePartyComposite() {
        def pmpPartyCompositeJson = this.getClass().getResource('/pmp-composites/party.json').text
        pmpPartyCompositeJson = replaceUuidToken(pmpPartyCompositeJson)
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(pmpPartyCompositeJson, CompositeParty.class)
    }

    static String replaceUuidToken(String pmpCompositeJson) {
        def tokenToReplace = "<to-be-replaced-with-new-uuid>"
        def uuid = UUID.randomUUID().toString()
        return pmpCompositeJson.replaceAll(tokenToReplace, uuid)
    }
}
