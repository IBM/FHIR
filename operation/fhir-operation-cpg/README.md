# fhir-operation-cpg

This is an optional module that provides a basic implementation of CQL-related operations found at present only in the [Clinical Practice Guidelines Implementation Guide](http://build.fhir.org/ig/HL7/cqf-recommendations/index.html) and upcoming FHIR R5 specifications. This includes the following:

* /$cql
* /Library/$evaluate
* /Library/[id]/$evaluate

These operations are assumed to interact with the local FHIR server for data, content, and terminology. If needed, terminology can be configured to be remote as described in the Terminology Guide. 

## TODO

* [ ] Implement prefetchData (see #2618)
* [ ] Implement remote endpoints for data, content, and terminology (see #2618)
