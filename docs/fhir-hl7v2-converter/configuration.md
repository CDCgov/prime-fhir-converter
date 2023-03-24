The FHIR to HL7 v2 conversion is driven by a configuration that specifies what HL7 v2 is generated
and what values are assigned to the Hl7 v2 fields. The use of FHIR Path expressions and constants make schemas
flexible and reusable.

# Schemas
There are top level and child schemas. Top level schemas define an HL7 message type and HL7 message version as 
supported by the Java HAPI HL7 v2 library. This initial version of the FHIR to HL7 v2 conversion library only 
supports ORU R01 (version 1.0 of this library will support any type and version supported by the HAPI HL7 v2 library). A schema
also defines a list constants and one or more elements.

| Property Name | Required?                 | Description                                                                                                 | Default |
|---------------|---------------------------|-------------------------------------------------------------------------------------------------------------|---------|
| hl7Type       | Required (top level only) | The HL7 v2 message type to generate                                                                         | None    |
| hl7version    | Required (top level only) | The hl7 v2 message version to generate                                                                      | None    |
| constants     | Optional                  | A map of schema level constants to be used by any element or child schema                                   | None    |
| extends       | Optional                  | Specify the relative path and schema name (the filename without extension) to inherit from a parent schema. | None    |

```yaml
hl7Type: ORU_R01
hl7Version: 2.5.1
constants:
  # Prefix for RS custom extension URLs
  rsext: '"https://reportstream.cdc.gov/fhir/StructureDefinition/"'
elements:
  - name: software-segment
    condition: 'Bundle.entry.resource.ofType(MessageHeader).exists()'
    resource: 'Bundle.entry.resource.ofType(MessageHeader)'
    schema: base/software
```

# Elements
There are two type of elements:
* Value elements that set one or more HL7 v2 fields
* Schema elements that use reusable child schemas that can handle repetitions

## Common Element Properties
| Property Name | Required? | Description                                                                                                                                                                                                      | Default                                |
|---------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| name          | Required  | The name of the element. Element names must be unique within a schema including child schemas. This uniqueness allows for the overwriting of elements in extended schemas.                                       | None                                   |
| constants     | Optional  | A map of element level constants to be used by the element or its child schema                                                                                                                                   | None                                   |
| resource      | Optional  | A FHIR Path expression that defines the focus FHIR resource for subsequent FHIR Path evaluations within the element and child schema.                                                                            | Bundle root or parent defined resource |
| condition     | Optional  | A FHIR Path expression that must evaluate to a Boolean to determine if the element should be processed. If the expression does not evaluate to a boolean then an exception is logged and it evaluates to `false` | `true`                                 |
| required      | Optional  | Set to `true` if the element's condition must evaluate to true                                                                                                                                                   | `false`                                |
| debug         | Optional  | Set to `true` to output debug information on the evaluation of the element                                                                                                                                       | `false`                                |

## Value Elements Properties
Value elements are used to populate one or more HL7 v2 fields with a FHIR primitive value.

| Property Name | Required? | Description                                                                                                                                                                                                                                                                                                                                       | Default |
|---------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| value         | Required  | A list of FHIR Path expressions that must evaluate to a FHIR primitive value. Each value is evaluated in order and the first non-empty/non-null value will be used to populate the HL7 v2 field. Note that string literals are allowed in FHIR Path and should be surrounded by quotes.                                                           | None    |
| valueSet      | Optional  | A map of key/value pairs used to convert the result from the `value` property. The result from the `value` property is compared to the key of the `valueSet` and if a key is matches then the value from the key/value pair is assigned to the HL7 v2 field. If a key/value pair was set and there was no match then the HL7 v2 field is not set. | None    |
| hl7spec       | Required  | A list of [HAPI HL7 v2 location specs](https://hapifhir.github.io/hapi-hl7v2/base/apidocs/ca/uhn/hl7v2/util/Terser.html) to assign the resulting value from the `value` property to. All listed HL7 v2 location specs get assigned the same value.                                                                                                | None    |

```yaml
  - name: use
    condition: '%resource.exists()'
    value:
      - '%resource.use'
      - '"official"'
    hl7Spec: [ '%{hl7NameField}-7' ]
    valueSet:
      official: L
      usual: D
      maiden: M
      nickname: N
      anonymous: S
```

## Schema Element Properties
Schema elements are used for:
* using a child schema to process a FHIR resource. This allows you to reuse schemas to process specific field types 
(e.g. convert a FHIR Coding resource to an HL7 v2 CE field)
* iterating over a FHIR Collection (an array) to process each iteration separately

Important notes on child schemas:
* They inherit the resource defined by the element (if any) or parent element
* They inherit the constants defined by the element, parent elements, and/or parent schema(s)

| Property Name | Required? | Description                                                                                                                                                                                                       | Default |
|---------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| schema        | Required  | The relative path and name (the filename without extension) of a child schema to be evaluated using the set focus resource.                                                                                       | None    |  
| resourceIndex | Optional  | The name of a constant that contains the index number of the FHIR resource being processed from the collection. Used to iterate over a FHIR resource collection denoted by the resource property and starts at 0. | 0       |

```yaml
  - name: filler-order
    resource: '%resource.identifier.where(type.coding.code = "FILL")'
    condition: '%resource.exists()'
    constants:
      entityIdFieldPath: '%{fieldPath}(%{entityIdIndex})-3'
    schema: datatype/ei-entity-identifier
    resourceIndex: entityIdIndex
```

## Order of Evaluation
Within a schema, elements are evaluated in order. This allows for setting up elements with conditionals to overwrite values
as needed. Note that child schema elements are evaluated in order as well at the time the parent element is evaluated.
The element properties are evaluated in the following order:
1. constants - constants are loaded and can be used in any property that supports it. Note that constants are treated as
strings until they are used in the FHIR Path expression of an element
2. resource - the focus resource is set and can be used in the subsequent properties. If the resource evaluates to empty
then the element is not processed.
3. condition - if the condition evaluates to false then the element is not processed
4. required - if required is true and condition is false then an exception is thrown
5. value and hl7Spec - if the element is to set a value
6. schema and resourceIndex - if the element is a child schema

# About Constants
Constants provide great flexibility to generate reusable schemas and handle repeating types and fields, but with great 
flexibility can also come great headaches and care must be taken on how constants are used.

1. Care must be taken not to use FHIR Path syntax in an HL7 v2 location spec. It is recommended to use specific constants
for FHIR Path and others for Hl7 v2 location specs to reduce the risk of a syntax error.
2. Simple integer constants, like a resource index, can be used in both FHIR Path expressions and HL7 Specs
(e.g. `myIndex: 1`)
3. Constants are treated as strings until they are used on a specific element. At that time, if a constant is used
in a FHIR Path expression then the contents of the string are evaluated as a FHIR Path expression for the result to 
be inserted as replacement for the constant. This allows you to define constants with FHIR Path expressions
that can be reused, but that the result will be dependent on what constants and focus resource is set on the element using it.
4. Constants used as literals in FHIR Path expressions need to be surrounded by quotes (e.g. `myConstant: '"my literal string"'`)

Constants are used as follows:
* **For FHIR Path expressions** please refer to the
[FHIR Path specification section on environment variables](http://hl7.org/fhirpath/N1/#environment-variables).
Constants must have proper FHIR Path expression syntax including for string literals. This library also supports
having constants as a prefix by using the syntax `` %`<constant_name>-<prefix> ``
```yaml
  constants:
    diagnostic: 'Bundle.entry.resource.ofType(DiagnosticReport)[%orderIndex]'
    rsext: '"https://reportstream.cdc.gov/fhir/StructureDefinition/"'
  resource: '%resource.extension(%`rsext-parent-observation-value-descriptor`)'
```

* **For HL7 location spec**, all constants are treated as string replacements. The special syntax of
`%{<constant_name>}` is used 
```yaml
  constants:
     fieldPath: '/PATIENT_RESULT/ORDER_OBSERVATION(%{orderIndex})/OBR'
  hl7Spec: [ '%{fieldPath}(%{entityIdIndex})-3' ]
```

# Extending Schemas
Top level schemas can extend any existing top level schema in order to reduce duplicate elements and facilitate
customization for different purposes. You can chain as many top level schemas as you wish. Note that reusing an element
name will result in overwriting the existing element, whether the existing element is at the top level of the extended schema or one of its child schemas. 

```yaml
hl7Type: ORU_R01
hl7Version: 2.5.1
extends: ../ORU_R01/ORU_R01-base
elements:
  # Disable an element. Note we only need the name and what is overwritten.
  - name: filler-order
    condition: 'false'
```

Extended schemas can:
* Define a different type and version of Hl7 v2 message than the top level schema
* Override one or more elements based on the element name
  * You override element can reside at the top level element list regardless where they reside in the inherited schema
  * Since you are overriding elements, you only need the element name and any properties you are overriding
* Add new elements in the same manner as any schema
* The order of the elements matter. The top level schema elements are loaded first.

# Useful References
* [FHIR Path specification](http://hl7.org/fhirpath/N1/)
* [HAPI HL7 v2 location specification syntax](https://hapifhir.github.io/hapi-hl7v2/base/apidocs/ca/uhn/hl7v2/util/Terser.html)