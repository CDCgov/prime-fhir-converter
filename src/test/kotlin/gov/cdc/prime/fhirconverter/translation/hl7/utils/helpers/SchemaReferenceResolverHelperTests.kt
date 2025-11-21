package gov.cdc.prime.fhirconverter.translation.hl7.utils.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.test.Test

class SchemaReferenceResolverHelperTests {

    // todo this file errors when the tests are run, saying there are no tests in it??
    // todo axe this file for this test isn't really needed? bulk of fxnality cut out of impl file
    @Test
    fun `test getSchemaServiceProviders`() {
        val providers = SchemaReferenceResolverHelper.getSchemaServiceProviders()
        assertNotNull(providers)
        assertEquals(2, providers.size)
        assertNotNull(providers.get("file"))
        assertNotNull(providers.get("classpath"))
//        assertNotNull(providers.get("azure"))
    }
}