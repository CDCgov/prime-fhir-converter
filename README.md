General disclaimer This repository was created for use by CDC programs to collaborate on public health related projects in support of the CDC mission. GitHub is not hosted by the CDC, but is a third party website used by CDC and its partners to share information and collaborate on software. CDC use of GitHub does not imply an endorsement of any one particular service, product, or enterprise.

# Overview

The PRIME FHIR Converter is a Java-based library, written in Kotlin, and created as part of the CDC’s Pandemic-Ready
Interoperability Modernization Effort (PRIME). This library contains various utilities to manipulate FHIR data, most
notably a configurable FHIR to HL7 v2 conversion utility. This library is currently used by the 
[CDC’s ReportStream](https://github.com/CDCgov/prime-data-hub) project to support the delivery of Electronic 
Laboratory Reporting (ELR) messages to public health agencies (PHAs) that can only ingest HL7 v2 and not FHIR.

We believe this library can increase FHIR adoption within health IT ecosystems by supporting a modular 
modernization process where organizations can use a FHIR based system where desired and convert the output 
to HL7 v2 for use in legacy systems.

# Usage
## Adding to Your Project
This Java-based library has not yet been published to a binary repository. To use it in your application, you can download
the lastest JAR file from the [releases](https://github.com/CDCgov/prime-fhir-converter/releases) or include
the library as part of your Gradle build scripts as follows:

In your `settings.gradle.kts` or `settings.gradle` add this repository as a source (note this feature requires 
Gradle 4.10 or higher)
```groovy
sourceControl {
    gitRepository(java.net.URI("https://github.com/CDCgov/prime-fhir-converter.git")) {
        producesModule("gov.cdc.prime:prime-fhir-converter")
    }
}
```

Add the dependency to your `build.gradle.kts` 
```kotlin
implementation("gov.cdc.prime:prime-fhir-converter:0.1")
```
or your `build.gradle` file
```groovy
implementation 'gov.cdc.prime:prime-fhir-converter:0.1'
```

## FHIR to HL7 v2 Conversion
The FHIR to HL7 v2 conversion utility takes in a message bundle that follows the
[HL7 v2 to FHIR Mapping Implementation Guide](https://build.fhir.org/ig/HL7/v2-to-fhir/index.html)
and converts the data to an HL7 v2 message per the a provided
[configuration schema](docs/fhir-hl7v2-converter/configuration.md). A configuration schema is a very flexible list 
of rules on how build an HL7 v2 message from a FHIR bundle which specifies the type and version of HL7 v2 message
to generate as well as how to extract and translate the FHIR data into HL7 v2 fields. 
[A base configuration](src/main/resources/hl7_mapping/ORU_R01) that can
be extended is provided in the library which follows the 
[HL7 v2 to FHIR Mapping Implementation Guide](https://build.fhir.org/ig/HL7/v2-to-fhir/index.html) for 
converting a FHIR bundle to an HL7 v2 ORU R01 observation message. 

### Java Usage

```java
// Read a bundle using the HAPI FHIR library
IParser bundleParser = FhirContext.forR4().newJsonParser();
String bundleText = Files.readString(Path.of("sample_fhir.json"));
Bundle bundle = bundleParser.parseResource(Bundle.class, bundleText);

// Convert it to HL7 v2 - Note we do not specify the YML extension as part of the schema name
FhirToHl7Converter converter = new FhirToHl7Converter("src/main/resources/hl7_mapping/ORU_R01/ORU_R01-base", false, null);      
Message hl7Message = converter.convert(bundle);

// Print out the HL7 v2 message using the HAPI HL7 v2 library
System.out.println(hl7Message.encode());
```

### Kotlin Usage

```kotlin
// Read a bundle using the HAPI FHIR library
val bundleParser = FhirContext.forR4().newJsonParser()
val bundleText = File("sample_fhir.json").readText()
val bundle = bundleParser.parseResource(Bundle::class.java, bundleText)

// Convert it to HL7 v2 - Note we do not specify the YML extension as part of the schema name
val converter = FhirToHl7Converter("src/main/resources/hl7_mapping/ORU_R01/ORU_R01-base")
val hl7Message = converter.convert(bundle)

// Print out the HL7 v2 message using the HAPI HL7 v2 library
println(hl7Message.encode())
```

## HL7 v2 to FHIR Conversion
An HL7 v2 to FHIR converter is not included as part of this library, but the CDC ReportStream project uses the 
[Linux4Health Hl7 v2 to FHIR converter library](https://github.com/LinuxForHealth/hl7v2-fhir-converter) to convert
HL7 v2 to FHIR. You can find
a [sample implementation](https://github.com/CDCgov/prime-reportstream/blob/master/prime-router/src/main/kotlin/fhirengine/translation/HL7toFhirTranslator.kt) and 
[conversion configuration](https://github.com/CDCgov/prime-reportstream/tree/master/prime-router/metadata/fhir_mapping)
for ORU R01 in the [ReportStream repository](https://github.com/CDCgov/prime-reportstream). 

## Extended FHIR Path Evaluation Utilities
This library contains extensions to the FHIR Path functions that are used to facilitate the conversion process from 
FHIR to HL7 v2, but can be used on their own for other purposes. See
[FHIR Path Custom Functions](docs/fhir-path-custom-functions.md)

## Future Functionality in v1.0
More features are coming in the near future, including support in the FHIR to HL7 v2 converter for any message type
or version supported by the HAPI HL7 v2 library, as well as a FHIR transform utility to manipulate the data in 
a FHIR bundle to enrich or fix its data. Set a [Watch](https://docs.github.com/en/rest/activity/watching) on this
repository to stay up-to-date with changes to this library.

# Building
To build the library, you need to have installed a Java JDK 11 or higher and run the command
```bash
./gradlew build
```
The resulting JAR will be in the `build/libs` folder

## Related documents

* [Open Practices](open_practices.md)
* [Rules of Behavior](rules_of_behavior.md)
* [Thanks and Acknowledgements](thanks.md)
* [Disclaimer](DISCLAIMER.md)
* [Contribution Notice](CONTRIBUTING.md)
* [Code of Conduct](code-of-conduct.md)
 
## Public Domain Standard Notice
This repository constitutes a work of the United States Government and is not
subject to domestic copyright protection under 17 USC § 105. This repository is in
the public domain within the United States, and copyright and related rights in
the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
All contributions to this repository will be released under the CC0 dedication. By
submitting a pull request you are agreeing to comply with this waiver of
copyright interest.

## License Standard Notice
The repository utilizes code licensed under the terms of the Apache Software
License and therefore is licensed under ASL v2 or later.

This source code in this repository is free: you can redistribute it and/or modify it under
the terms of the Apache Software License version 2, or (at your option) any
later version.

This source code in this repository is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the Apache Software License for more details.

You should have received a copy of the Apache Software License along with this
program. If not, see http://www.apache.org/licenses/LICENSE-2.0.html

The source code forked from other open source projects will inherit its license.

## Privacy Standard Notice
This repository contains only non-sensitive, publicly available data and
information. All material and community participation is covered by the
[Disclaimer](https://github.com/CDCgov/template/blob/master/DISCLAIMER.md)
and [Code of Conduct](https://github.com/CDCgov/template/blob/master/code-of-conduct.md).
For more information about CDC's privacy policy, please visit [http://www.cdc.gov/other/privacy.html](https://www.cdc.gov/other/privacy.html).

## Contributing Standard Notice
Anyone is encouraged to contribute to the repository by [forking](https://help.github.com/articles/fork-a-repo)
and submitting a pull request. (If you are new to GitHub, you might start with a
[basic tutorial](https://help.github.com/articles/set-up-git).) By contributing
to this project, you grant a world-wide, royalty-free, perpetual, irrevocable,
non-exclusive, transferable license to all users under the terms of the
[Apache Software License v2](http://www.apache.org/licenses/LICENSE-2.0.html) or
later.

All comments, messages, pull requests, and other submissions received through
CDC including this GitHub page may be subject to applicable federal law, including but not limited to the Federal Records Act, and may be archived. Learn more at [http://www.cdc.gov/other/privacy.html](http://www.cdc.gov/other/privacy.html).

## Records Management Standard Notice
This repository is not a source of government records, but is a copy to increase
collaboration and collaborative potential. All government records will be
published through the [CDC web site](http://www.cdc.gov).

## Additional Standard Notices
Please refer to [CDC's Template Repository](https://github.com/CDCgov/template)
for more information about [contributing to this repository](https://github.com/CDCgov/template/blob/master/CONTRIBUTING.md),
[public domain notices and disclaimers](https://github.com/CDCgov/template/blob/master/DISCLAIMER.md),
and [code of conduct](https://github.com/CDCgov/template/blob/master/code-of-conduct.md).
