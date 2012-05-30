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

import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Andreas Höhmann
 */
public class VaadletsBuilderXMLTest {

  private static InputStreamReader reader(final String xmlFile) throws IOException {
    return new InputStreamReader(new ClassPathResource(xmlFile, VaadletsBuilderXMLTest.class).getInputStream());
  }

  @Test(dataProvider = "valid-xml-files")
  public void testBuild(final String xmlFile) throws IOException {
    new VaadletsBuilder().build(reader(xmlFile));
  }

  @Test(dataProvider = "valid-xml-files")
  public void testUnmarschal(final String xmlFile) throws IOException {
    final Vaadlets v = JAXBUtils.unmarshal(reader(xmlFile), false);
  }

  @Test(dataProvider = "valid-xml-files")
  public void testValidate(final String xmlFile) throws IOException {
    final Vaadlets v = JAXBUtils.unmarshal(reader(xmlFile), true);
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
