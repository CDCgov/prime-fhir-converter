# Mapping schema file structure

Translation between FHIR and HL7v2 requires schema files that define how data is translated from one format to another. As we have built out these mappings, we developed a file structure that aims to be self-documenting and logical so that the mappings are more maintainable and reusable.

Mappings were developed based on the [FHIR R4 mapping inventory](https://docs.google.com/spreadsheets/d/1PaFYPSSq4oplTvw_4OgOn6h2Bs_CMvCAU9CqC4tPBgk/edit#gid=0). This documentation is under active development so there may be differences between the current revision of the inventory and the mapping files.

## FHIR to HL7v2 mapping file organization

In order for the directory structure to be self-documenting, it should follow a defined hierarchy. The following structure would make both the source of data and the target mapping immediately apparent:

- hl7_mapping/_**[HL7v2 message type]**_/ ← target message types, such as ADT_A01, ORU_R01, etc.
- hl7_mapping/resources/_**[FHIR path to resource]**_/_**[HL7v2 segment or datatype]**_.yml
- hl7_mapping/datatypes/_**[FHIR path to datatype]**_/_**[HL7v2 segment or datatype]**_.yml

Each message type has a directory named after itself, with subdirectories for message specific resources or datatypes schemas.

- hl7_mapping/ORU_R01/ORU-R01-base.yml
- hl7_mapping/ORU_R01/base.patient-result ← only if special handling is needed for this message type

For resources and datatypes, the basic structure is to define the FHIR path being mapped, followed by the HL7v2 segment that is being mapped using data from the given FHIR path. For example:

- hl7_mapping/resources/MessageHeader/SFT.yml ← elements of MessageHeader that map to SFT
- hl7_mapping/resources/MessageHeader/MSH.yml ← elements of MessageHeader that map to MSH
- hl7_mapping/resources/Organization/HD.yml ← elements of Organization that map to HD
- hl7_mapping/resources/Organization/XON.yml ← elements of Organization that map to XON
- hl7_mapping/resources/Organization/XONExtension.yml ← elements of Organization that map to XON that may not have mapped values in FHIR, used to store literal string representations of specific HL7v2 fields

Schemas that reference resource elements should be stored in a directory that represents the element:

- resources/MessageHeader/source/HD.yml ← map MessageHeader.source
- resources/MessageHeader/destination/HD.yml ← map MessageHeader.destination

Datatypes follow the same pattern:

- datatypes/Coding/MSG.yml ← Coding maps to MSG
- datatypes/Meta/PT.yml ← Meta maps to PT

### About Extensions

Several mapping files contain support for custom extensions that are intended to preserve the original HL7v2 string literal values when there is no lossless equivalent in the FHIR mapping inventory. Many of these extensions follow the structure of `url` being the HL7v2 location (e.g. `OBX.2`) and `value` being the string literal value to be placed at that location. If the extension is present in the input FHIR bundle, this value should override any other mappings that may exist for that location.