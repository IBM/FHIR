/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.search.parameters;

import static com.ibm.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Markdown;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.model.type.code.SearchParamType;

public class ParametersMapTest {
  SearchParameter sp_a1 = SearchParameter.builder()
          .url(Uri.of("http://ibm.com/fhir/test/sp_a1"))
          .status(PublicationStatus.ACTIVE)
          .name(string("a"))
          .description(Markdown.of("First param with code 'a'"))
          .base(ResourceType.RESOURCE)
          .type(SearchParamType.STRING)
          .code(Code.of("a"))
          .expression(string("extension.value as String"))
          .build();
  SearchParameter sp_a2 = SearchParameter.builder()
          .url(Uri.of("http://ibm.com/fhir/test/sp_a2"))
          .status(PublicationStatus.ACTIVE)
          .name(string("a"))
          .description(Markdown.of("Second param with code 'a'"))
          .base(ResourceType.RESOURCE)
          .type(SearchParamType.STRING)
          .code(Code.of("a"))
          .expression(string("extension.value as String"))
          .build();
  SearchParameter sp_b = SearchParameter.builder()
          .url(Uri.of("http://ibm.com/fhir/test/sp_b"))
          .status(PublicationStatus.ACTIVE)
          .name(string("b"))
          .base(ResourceType.RESOURCE)
          .type(SearchParamType.STRING)
          .description(Markdown.of("Param with code 'b'"))
          .code(Code.of("b"))
          .expression(string("extension.value as String"))
          .build();

  @Test
  public void testParametersMap() {
      ParametersMap pm = new ParametersMap();
      pm.insert("a", sp_a1);
      pm.insert("a", sp_a2);
      pm.insert("b", sp_b);

      assertTrue(pm.lookupByCode("a").contains(sp_a1));
      assertTrue(pm.lookupByCode("a").contains(sp_a2));
      assertTrue(pm.lookupByCode("b").contains(sp_b));

      assertEquals(pm.lookupByUrl("http://ibm.com/fhir/test/sp_a1"), sp_a1);
      assertEquals(pm.lookupByUrl("http://ibm.com/fhir/test/sp_a2"), sp_a2);
      assertEquals(pm.lookupByUrl("http://ibm.com/fhir/test/sp_b"), sp_b);

      assertTrue(pm.codeEntries().size() == 2);
      assertTrue(pm.urlEntries().size() == 3);
  }

  /**
   * Adding the same parameter under two different codes should be supported
   */
  @Test
  public void testParametersMap_duplicateUrl() {
      ParametersMap pm = new ParametersMap();
      pm.insert("b", sp_b);
      pm.insert("b2", sp_b);

      assertTrue(pm.lookupByCode("b").contains(sp_b));
      assertTrue(pm.lookupByCode("b2").contains(sp_b));

      assertEquals(pm.lookupByUrl("http://ibm.com/fhir/test/sp_b"), sp_b);

      assertTrue(pm.codeEntries().size() == 2);
      assertTrue(pm.urlEntries().size() == 1);
  }

  /**
  * Adding the same parameter twice should be the same as adding it once
  */
  @Test
  public void testParametersMap_duplicate() {
      ParametersMap pm = new ParametersMap();
      pm.insert("b", sp_b);
      pm.insert("b", sp_b);

      assertTrue(pm.lookupByCode("b").contains(sp_b));

      assertEquals(pm.lookupByUrl("http://ibm.com/fhir/test/sp_b"), sp_b);

      assertTrue(pm.codeEntries().size() == 1);
      assertTrue(pm.urlEntries().size() == 1);
  }
}
