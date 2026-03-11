# PRIME FHIR Converter Development Guide

This guide outlines the practical steps and overall process for converting FHIR resources to HL7v2 messages. It transforms FHIR resources into HL7v2 messages using the configuration schema to generate HL7v2 segments and fields from FHIR resource elements .

Implementation steps include:

* Create an issue so your work can be tracked, and you can get feedback and input. (See [CONTRIBUTING](https://github.com/CDCgov/prime-fhir-converter/blob/master/CONTRIBUTING.md))
* What are you doing?
  - 1 Adding the FHIR resources and mapping to the HL7 segments and fields? Then:
    * Review the official HL7 documentation for the target FHIR Resource.
    * Modifying the configuration schema mapping.
    * Modify an existing test when add new feature
  - 2 Adding a New Configuration schema to map a new Message Type? Then:
    * Add the Configuration schema to support the new HL7v2 message map.
    * Test the new configuration schema mapping for the HL7v2 message segment field assertions.
  - 3 Example of Unit Test to test the New Configuration Schema to Support a New HL7v2 Message Type? Then
    * Example of a Unit Test Case.
    * Create a new message template
    * Suggestions for the new developer.
    * Implement tests for both the messages to resources and segment fields to resource data to validate mapping results.
  * Make a PR and request review (See [CONTRIBUTING](https://github.com/CDCgov/prime-fhir-converter/blob/master/CONTRIBUTING.md))
  * Suggestions for new developers

## 1. Adding FHIR resources and mapping to an existing HL7 segments and fields
### Review the Official FHIR (HL7 Documentation)

The Official HL7 FHIR documentation, provided by the HL7 organization, provides complete documentation for HL7 FHIR resources and the corresponding HL7v2 mappings. The best place to start is by reviewing the [HL7 FHIR Resource index](https://www.hl7.org/fhir/resourcelist.html).

Each resource page includes field definitions (e.g., Resource [Medication - Content](https://www.hl7.org/fhir/medication.html)) and HL7v2 mappings (e.g., Resource [Medication - Mapping](https://www.hl7.org/fhir/medication-mappings.html)). Review the documentation to determine which FHIR resource fields to include. The FHIR specification supports many resources in various fields, and only a subset is likely necessary for a specific use case.

Additionally, it is helpful to review the structures, groups, and segments of HL7 v2.6 messages to inspect the conversion results. A useful resource is [Health Level Seven® Standard Version 2.6 - An Application Protocol for Electronic Data Exchange in Healthcare Environments](https://www.vico.org/HL7_V2_6/HL7%20Messaging%20Version%202.6/V2_6_Index.html) at vico.org.

### Modifying the configuration schema mapping
Identify the FHIR resource elements that map to the segments and fields of the HL7v2 message. A good example is [src/main/resources/hl7_mapping/ORU_R01/base/patient.yml](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/main/resources/hl7_mapping/ORU_R01/base/patient.yml), which maps the FHIR patient resource to the HL7 PID segment. The conversion is managed by a configuration schema that specifies which HL7v2 messages to generate and the values assigned to each HL7v2 field. For more details, please see the [CONFIGURATION](https://github.com/CDCgov/prime-fhir-converter/blob/master/docs/fhir-hl7v2-converter/configuration.md) page.  For base resources, please see the [RESOURCES](https://github.com/CDCgov/prime-fhir-converter/tree/master/src/main/resources/hl7_mapping/ORU_R01/base) page for ORU_R01 message type setting.

### Modify an existing test when add new feature

Modify an existing test in [src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7](https://github.com/CDCgov/prime-fhir-converter/tree/master/src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7) to test the new FHIR resource element. As an example, see  [FhirToHl7ConverterTests.kt](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7/FhirToHl7ConverterTests.kt), which tests FHIR resource element conversion to HL7 field.  The developer can add more unit tests to the file when a new FHIR resource element needs to be tested.
## 2. Adding a New Configuration Schema to generate a new Message Type from given FHIR resource.

### Add the Configuration Schema to support the new HL7v2 message mapping.
To add a new configuration schema, a developer creates a file named after the message type (e.g., ADT_A01.yml) and reuses content from an existing configuration file.  The existing configuration is in [src/main/resources/hl7_mapping/ORU_R01](https://github.com/CDCgov/prime-fhir-converter/tree/master/src/main/resources/hl7_mapping/ORU_R01). A good example is [src/main/resources/hl7_mapping/ORU_R01/ORU_R01-base.yml](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/main/resources/hl7_mapping/ORU_R01/ORU_R01-base.yml), which processes the ORU_R01 message.  [CONFIGURATION](https://github.com/CDCgov/prime-fhir-converter/blob/master/docs/fhir-hl7v2-converter/configuration.md) explains how to configure the schema to support a new message type to map a FHIR resource to an HL7v2 field segment.  A developer should reuse an existing schema and only create new templates if needed.

### Test the new configuration schema mapping for the HL7v2 message segment field assertions
Add a directory to the test resources schema directory, src/main/test/resources/schema, to test the new configuration schema. Next, a developer needs to add unit tests to verify that the new HL7v2 segment field is generated. As an example, see [FhirToHl7ConverterTests.kt](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7/FhirToHl7ConverterTests.kt) copy and create a new unit test if needed.

Unit Tests are vital to help check new function, and also ensure that an added function does not break when other later function is added.

## 3. Example of Unit Test to test the New Configuration Schema to Support a New HL7v2 Message Type

### Example of a Unit Test Case
The example below demonstrates a Unit test case for setting the HL7 value using ConverterSchemaElement.  The example below can be found in [FhirToHl7ConverterTests.kt](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7/FhirToHl7ConverterTests.kt).

```jsunicoderegexp
package gov.cdc.prime.fhirconverter.translation.hl7
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import ca.uhn.fhir.context.FhirContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.util.Terser
import gov.cdc.prime.fhirconverter.translation.hl7.schema.ConfigSchemaReader.fromFile
import gov.cdc.prime.fhirconverter.translation.hl7.schema.converter.ConverterSchemaElement
import gov.cdc.prime.fhirconverter.translation.hl7.schema.converter.HL7ConverterSchema
import gov.cdc.prime.fhirconverter.translation.hl7.utils.CustomContext
import gov.cdc.prime.fhirconverter.translation.hl7.utils.helpers.SchemaReferenceResolverHelper
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.ServiceRequest
import org.junit.jupiter.api.Nested
import java.io.File
import kotlin.test.Test

class FhirToHl7ConverterTests {

@Test
    fun `test set HL7 value`() {
        val fieldValue = "somevalue"
        val mockTerser = mockk<Terser>()
        val mockSchema = mockk<HL7ConverterSchema>() // Just a dummy schema to pass around
        var element = ConverterSchemaElement("name", required = true, hl7Spec = listOf("MSH-10"))
        var converter =
            FhirToHl7Converter(mockSchema, terser = mockTerser, warnings = mutableListOf(), errors = mutableListOf())
        val customContext = CustomContext(Bundle(), Bundle())

        // Required element
        assertFailure { converter.setHl7Value(element, "", customContext) }
            .hasClass(RequiredElementException::class.java)

        // Test the value is set for all specified HL7 fields
        element = ConverterSchemaElement("name", hl7Spec = listOf("MSH-10", "MSH-11", "MSH-12"))
        justRun { mockTerser.set(any(), any()) }
        converter.setHl7Value(element, fieldValue, customContext)
        verifySequence {
            mockTerser.set(element.hl7Spec[0], fieldValue)
            mockTerser.set(element.hl7Spec[1], fieldValue)
            mockTerser.set(element.hl7Spec[2], fieldValue)
        }

        // Not strict errors
        every { mockTerser.set(any(), any()) } throws HL7Exception("some text")
        assertThat(converter.setHl7Value(element, fieldValue, customContext))
        clearAllMocks()

        every { mockTerser.set(any(), any()) } throws IllegalArgumentException("some text")
        assertThat(converter.setHl7Value(element, fieldValue, customContext))

        clearAllMocks()

        // Strict errors
        converter = FhirToHl7Converter(
            mockSchema,
            true,
            mockTerser,
            warnings = mutableListOf(),
            errors = mutableListOf()
        )
        every { mockTerser.set(element.hl7Spec[0], any()) } throws HL7Exception("some text")
        assertFailure { converter.setHl7Value(element, fieldValue, customContext) }
            .hasClass(HL7ConversionException::class.java)

        every { mockTerser.set(element.hl7Spec[0], any()) } throws IllegalArgumentException("some text")
        assertFailure { converter.setHl7Value(element, fieldValue, customContext) }
            .hasClass(SchemaException::class.java)

        every { mockTerser.set(element.hl7Spec[0], any()) } throws Exception("some text")
        assertFailure { converter.setHl7Value(element, fieldValue, customContext) }
            .hasClass(HL7ConversionException::class.java)
    }
}
```
### Suggestions for the new developer
The learning curve can feel steep for a new developer. Suggestions for "ramping up":
1.	Clone the [PRIME-FHIR-CONVERTER](https://github.com/CDCgov/prime-fhir-converter) project.
2.	Set up your environment for Kotlin development. You can use VS Code or IntelliJ IDE.
3.	Run the command `gradle clean build`. This will confirm that your libraries are set up correctly and that your development environment is working.
4.	Select a unit test and run it. This will confirm your unit test setup is working. For example, you could try: [FhirToHl7ConverterTests.kt](https://github.com/CDCgov/prime-fhir-converter/blob/master/src/test/kotlin/gov/cdc/prime/fhirconverter/translation/hl7/FhirToHl7ConverterTests.kt).
5.	Set the IDE Project Setting to OpenJDK 11.
6.	View the HL7v2 message created from the conversion in the test. The easiest way to do this is to put a breakpoint immediately after the command val message = FhirToHl7Converter(…).process(bundle), which returns an HL7v2 message.
7.	Use the debugger to step through the asserts of the test. View the associated schema elements with the generated HL7 message segment.
8.	Find the message in src/main/resources/hl7_mapping/ORU_R01. e.g. ORU_R01-base.yml
9.	Observe how the Segment.Fields such as MSH-9.1 and MSH-9.2 display the message type correctly.
10.	Make a small change to one of the tests. Change the input FHIR resource element and observe how the output responds Repeat.

-----
