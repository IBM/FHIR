package com.ibm.fhir.server.test.cpg;

import java.util.Collection;
import java.util.stream.Collectors;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

import com.ibm.fhir.model.resource.CapabilityStatement;
import com.ibm.fhir.path.FHIRPathNode;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.ibm.fhir.server.test.FHIRServerTestBase;

public class BaseCPGOperationTest extends FHIRServerTestBase {
    
    @BeforeClass
    public void checkForOperationSupport() throws Exception {
        CapabilityStatement conf = retrieveConformanceStatement();
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        EvaluationContext evaluationContext = new EvaluationContext(conf);
        Collection<FHIRPathNode> tmpResults = evaluator.evaluate(evaluationContext, "rest.resource.where(type = 'Library').operation.name");
        Collection<String> listOfOperations = tmpResults.stream().map(x -> x.getValue().asStringValue().string()).collect(Collectors.toList());
        if( ! listOfOperations.contains("evaluate") ) {
            throw new SkipException("CPG Operations are not enabled");
        }
    }
}
