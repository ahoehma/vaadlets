/*******************************************************************************
 * Copyright 2012 Andreas Höhmann (mymita.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.mymita.vaadlets;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Andreas Höhmann
 * @since 0.0.1
 */
public class VaadletsBuilderTest implements Serializable {

  private static final long serialVersionUID = -2707101566758503829L;

  private static InputStreamReader reader(final String xmlFile) throws IOException {
    return new InputStreamReader(new ClassPathResource(xmlFile, VaadletsBuilderTest.class).getInputStream());
  }

  @Test(dataProvider = "valid-xml-files")
  public void testBuild(final String xmlFile) throws IOException {
    assertNotNull(VaadletsBuilder.build(reader(xmlFile)).getRoot());
  }

  @Test(dataProvider = "valid-xml-files")
  public void testUnmarschal(final String xmlFile) throws IOException {
    assertNotNull(JAXBUtils.unmarshal(reader(xmlFile), false));
  }

  @Test(dataProvider = "valid-xml-files")
  public void testValidate(final String xmlFile) throws IOException {
    assertNotNull(JAXBUtils.unmarshal(reader(xmlFile), true));
  }

  @DataProvider(name = "valid-xml-files")
  private Object[][] validXMLFiles() {
    return new Object[][] {
      {
        "vaadlets-1.xml"
      }, {
        "vaadlets-2.xml"
      }, {
        "vaadlets-3.xml"
      }, {
        "vaadlets-4.xml"
      }, {
        "vaadlets-5.xml"
      },
    };
  }
}