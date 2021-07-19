# fhir-operation-cqf

This is an optional module that provides a basic implementation of some key features in the Clinical Quality Framework ([2019](http://hl7.org/fhir/us/cqfmeasures/2019May/) / [2021](http://hl7.org/fhir/us/cqfmeasures/2021May/)). This includes gathering of measure data requirements, measure evaluation, care gaps evaluation, and data submission. Operation definitions are pulled from the core FHIR R4 clinical reasoning module. There are still open items related to apply operations for ActivityDefinition and PlanDefinition apply operations. 

## TODO
* [ ] Add support for CQL in $apply for ActivityDefinition and PlanDefinition (see #2613)
* [ ] Add support for $data-requirements the ActivityDefinition and PlanDefinition (see #2616)
* [ ] Upgrade to operation definitions in May2021 cqfmeasures ballot (see 2614)
