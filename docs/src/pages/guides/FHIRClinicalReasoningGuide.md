---
layout: post
title: IBM FHIR Server Clinical Reasoning Guide
description: IBM FHIR Server Clinical Reasoning Guide
date:   2021-07-15
permalink: /FHIRClinicalReasoningGuide/
---

# Overview

(Experimental) IBM FHIR Server added initial support for the [Clinical Quality Language (CQL)](https://cql.hl7.org/) and FHIR operations defined in the [Clinical Practice Guidelines (CPG) ](https://build.fhir.org/ig/HL7/cqf-recommendations/) and [Quality Measure (cqf-measures)](http://hl7.org/fhir/us/cqfmeasures/stu2/) Implementation Guides (IG) that leverage CQL. These IGs build upon the resources and operations defined in the [FHIR Clinical Reasoning Module](http://www.hl7.org/FHIR/clinicalreasoning-module.html). 

The IBM FHIR Server's module builds upon the work of other open source projects including[cqframework/clinical_quality_language](https://github.com/cqframework/clinical_quality_language/), [DBCG/cql_engine](https://github.com/DBCG/cql_engine/), and [DBCG/cql-evaluator](https://github.com/DBCG/cql-evaluator/) projects and much of the work is to expose these functions with the necessary hooks to perform CQL to ELM translation and ELM evaluation natively inside the IBM FHIR server or externally using an IBM FHIR Server client and model objects. Additionally, support is provided for evaluating and reporting on clinical quality measures and care gaps.

This is an initial step. The quality measure and reporting space is being actively developed and changed and further development will be needed to keep up with standards. For example, both the [May2021 ballot](https://hl7.org/fhir/us/cqfmeasures/2021May/index.html) of the cqf- measures IG and the [Davinci Data Exchange for Quality Measures](http://hl7.org/fhir/us/davinci-deqm/datax.html) IG change some of the operations that were implemented here.

There was some prior work done on `ActivityDefinition/$apply` and `PlanDefinition/$apply` that are relevant to the clinical quality reporting space that were not integrated with this work yet and should be considered for next steps.

## CPG Operations

The CPG IG defines operations that work directly with CQL either as ad-hoc queries via the `/$cql` operation or via Library resources stored in the FHIR Registry (`/Library/$evaluate` and `/Library/[id]/$evaluate`). These operations are exposed by the *optional* module operation/fhir-operation-cpg. In order to make these operations available to the server, you must build the fhir-operation-cpg module and copy the target/fhir-operation-cpg-X.X.X-shaded.jar into the userlib directory of your FHIR server installation.

* /$cql
* /Library/$evaluate 
* /Library/[id]/$evaluate

# CQF Operations

The cqf-measures IG builds upon the resources and operations defined in the [FHIR Clincial Reasoning Module](http://www.hl7.org/FHIR/clinicalreasoning-module.html) to define a workflow of operations that support electronic Clinical Quality Measures (eCQMs) and reporting. These operations are exposed by the *optional* module operation/fhir-operation-cqf. In order to make these operations available to the server, you must build the fhir-operation-cqf module and copy the target/fhir-operation-cqf-X.X.X-shaded.jar into the userlib directory of your FHIR server installation.

* /Library/$data-requirements
* /Measure/[id]/$data-requirements
* /Measure/$collect-data
* /Measure/[id]/$collect-data
* /Measure/$evaluate-measure
* /Measure/[id]/$evaluate-measure
* /Measure/$care-gaps
* /Measure/$submit-data
* /Measure/[id]/$submit-data

## System APIs / Modules

|Module|Description|
|------|-----------|
|fhir-cql|Foundation classes for implementing the CQL Engine backend in IBM FHIR Server|
|fhir-cql-rest|REST Client-based implementation of CQL Engine backend|
|fhir-cql-server|Internal API-based implementation of CQL Engine backend|
|fhir-quality-measure|FHIR Quality Measure evaluation logic|
|operation/fhir-operation-cpg|*Optional* module that implements CQL operations|
|operation/fhir-operation-cqf|*Optional* module that implements CQF operations|

