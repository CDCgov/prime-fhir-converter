package gov.cdc.prime.fhirconverter.translation.hl7.utils.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.test.Test

class SchemaReferenceResolverHelperTests {

    @Test
    fun `test getSchemaServiceProviders`() {
        val providers = SchemaReferenceResolverHelper.getSchemaServiceProviders()
        assertNotNull(providers)
        assertEquals(2, providers.size)
        assertNotNull(providers.get("file"))
        assertNotNull(providers.get("classpath"))
    }
}