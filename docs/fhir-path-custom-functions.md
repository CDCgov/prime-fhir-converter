This FHIR converter library includes custom FHIR Path functions that can be used in expressions evaluated by the
`FHIRPathUtils` class. Alternatively, you can pass in the custom FHIR Path functions directly to your FHIR Path
Engine.

For the most up-to-date list of custom functions please refer to the code in the
[CustomFHIRFunctions.kt](../src/main/kotlin/gov/cdc/prime/fhirconverter/translation/hl7/utils/CustomFHIRFunctions.kt)
file.

# Usage
## Kotlin
```kotlin
val bundle = Bundle() // Read or populate your bundle

val context = CustomContext(bundle, bundle, mutableMapOf("diagnostic" to "Bundle.entry.resource.ofType(DiagnosticReport)"))
val result1 = FhirPathUtils.evaluate(context, bundle, bundle, "%diagnostic")
if (result1.isNotEmpty()) {
    // Set the first Diagnostic Report as our focus for our next evaluation
    val result2: List<Base> = FhirPathUtils.evaluate(context, result1[0], bundle, "%resource.status")
}
```

## Java
```java
Bundle bundle = new Bundle(); // Read or populate your bundle

Map<String, String> constants = new HashMap<>(Map.of("diagnostic", "Bundle.entry.resource.ofType(DiagnosticReport)"));
CustomContext context = new CustomContext(bundle, bundle, constants);
List<Base> result1 = FhirPathUtils.INSTANCE.evaluate(context, bundle, bundle, "%diagnostic");
if (!result1.isEmpty()) {
    // Set the first Diagnostic Report as our focus for our next evaluation
    List<Base> result2 = FhirPathUtils.INSTANCE.evaluate(context, result1.get(0), bundle, "%resource.status");
}
```
