package gov.cdc.prime.fhirconverter.translation.hl7.utils.helpers

import gov.cdc.prime.fhirconverter.translation.hl7.schema.ConfigSchemaReader
import gov.cdc.prime.fhirconverter.translation.hl7.schema.converter.HL7ConverterSchema
import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.ClasspathSchemaServiceProvider
import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.FileSchemaServiceProvider
import gov.cdc.prime.fhirconverter.translation.hl7.schema.providers.SchemaServiceProvider

object SchemaReferenceResolverHelper {

    fun retrieveHl7SchemaReference(schema: String): HL7ConverterSchema =
        ConfigSchemaReader.fromFile(
            schema,
            HL7ConverterSchema::class.java,
            getSchemaServiceProviders()
        )

    fun getSchemaServiceProviders():
        Map<String, SchemaServiceProvider> {
        val serviceProviders: MutableMap<String, SchemaServiceProvider> = mutableMapOf()
        serviceProviders["file"] = FileSchemaServiceProvider()
        serviceProviders["classpath"] = ClasspathSchemaServiceProvider()
        return serviceProviders
    }
}