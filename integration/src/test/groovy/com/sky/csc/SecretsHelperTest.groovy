package com.sky.csc

import com.sky.csc.categories.Done
import org.junit.experimental.categories.Category
import org.slf4j.LoggerFactory
import spock.lang.Specification

class SecretsHelperTest extends Specification {
    static final log = LoggerFactory.getLogger(SecretsHelperTest.class);

    @Category(Done)
    def "encrypt a secret and decrypt it again"() {
        given: "a secret to encrypt"
        def plainTextSecret = "super sensitive password"

        when: "encrypting the secret with the secrets helper"
        def encrypted = SecretsHelper.encrypt(plainTextSecret)

        then: "an AES encrypted string is returned"
        encrypted
        log.debug("encrypted secret '{}'", encrypted)

        when: "decrypting the encrypted secret with the secrets helper"
        def decrypted = SecretsHelper.decrypt(encrypted)

        then: "the decrypted string matches the original plain text secret"
        decrypted == plainTextSecret
    }
}
