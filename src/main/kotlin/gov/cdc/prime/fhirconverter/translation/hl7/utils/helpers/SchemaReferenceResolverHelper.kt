package gov.cdc.prime.fhirconverter.translation.hl7.utils.helpers

import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.ClasspathSchemaServiceProvider
import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.FileSchemaServiceProvider
import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.SchemaServiceProvider

object SchemaReferenceResolverHelper {

    fun getSchemaServiceProviders():
        Map<String, SchemaServiceProvider> {
        val serviceProviders: MutableMap<String, SchemaServiceProvider> = mutableMapOf()
        serviceProviders["file"] = FileSchemaServiceProvider()
        serviceProviders["classpath"] = ClasspathSchemaServiceProvider()
        return serviceProviders
    }
}