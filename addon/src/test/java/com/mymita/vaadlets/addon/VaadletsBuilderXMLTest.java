package com.mymita.vaadlets.addon;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mymita.vaadlets.Vaadlets;

/**
 * @author Andreas Höhmann
 */
public class VaadletsBuilderXMLTest {

  @Test(
    dataProvider = "valid-xml-files")
  public void testBuild(final String xmlFile) throws IOException {
    new VaadletsBuilder().build(new ClassPathResource(xmlFile, VaadletsBuilderXMLTest.class).getInputStream());
  }

  @Test(
    dataProvider = "valid-xml-files")
  public void testUnmarschal(final String xmlFile) throws IOException {
    final Vaadlets v = JAXBUtils.unmarshal(
        new ClassPathResource(xmlFile, VaadletsBuilderXMLTest.class).getInputStream(), false);
  }

  @Test(
    dataProvider = "valid-xml-files")
  public void testValidate(final String xmlFile) throws IOException {
    final Vaadlets v = JAXBUtils.unmarshal(
        new ClassPathResource(xmlFile, VaadletsBuilderXMLTest.class).getInputStream(), true);
  }

  @DataProvider(
    name = "valid-xml-files")
  private Object[][] validXMLFiles() {
    return new Object[][]{
        {
          "vaadlets-1.xml"
        },
        {
          "vaadlets-2.xml"
        },
    };
  }
}