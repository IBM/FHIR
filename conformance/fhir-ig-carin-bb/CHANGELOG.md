30 NOV 2020 - Initial Load
 - remove invalid <br/> tags from between <li> tags in CapabilityStatement-c4bb.json
 - move invalid <sup> tag into subsequent <p> tag in CapabilityStatement-c4bb.json 
 - removed invalid ImplementationGuide parameters `copyrightyear`, `releaselabel`, and more
 
02 JUN 2021 - service-date fixup
-  backport CARIN BB 1.1.0 service-date fix to our packaged 1.0.1 version #2456 
 - changed the FHIRPath expression from 
   "ExplanationOfBenefit.billablePeriod | ExplanationOfBenefit.item.serviced as Date |  ExplanationOfBenefit.item.serviced as Period" to 
   "ExplanationOfBenefit.billablePeriod | ExplanationOfBenefit.item.serviced"
 - see https://jira.hl7.org/browse/FHIR-30443 for more detail
 
 03 JUN 2021 - updated `adjudication-has-amount-type-slice` constraints per: [https://jira.hl7.org/browse/FHIR-32337](https://jira.hl7.org/browse/FHIR-32337)
 
```json
{
    "key": "adjudication-has-amount-type-slice",
    "severity": "error",
    "human": "If Adjudication is present, it must have at least one adjudicationamounttype slice",
    "expression": "adjudication.exists() implies (adjudication.where(category.memberOf('http://hl7.org/fhir/us/carin-bb/ValueSet/C4BBAdjudication')).exists())",
    "source": "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Inpatient-Institutional"
}
```

Changed the constraint location from: `ExplanationOfBenefit.item` and `ExplanationOfBenefit.item.adjudication` to `ExplanationOfBenefit` and `ExplanationOfBenefit.item` respectively in the `snapshot` and `differential` for the inpatient and outpatient Institutional EOBs.
