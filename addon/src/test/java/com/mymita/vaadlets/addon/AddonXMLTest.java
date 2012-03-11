package com.mymita.vaadlets.addon;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mymita.vaadlets.Vaadlets;

/**
 * @author Andreas Höhmann
 */
public class AddonXMLTest {

  @Test(
    dataProvider = "valid-xml-files")
  public void testUnmarschal(final String xmlFile) throws IOException {
    final Vaadlets v = JAXBUtils.unmarschal(new ClassPathResource(xmlFile, AddonXMLTest.class).getInputStream(), false);
  }
  
  @Test(
		  dataProvider = "valid-xml-files")
  public void testValidate(final String xmlFile) throws IOException {
	  final Vaadlets v = JAXBUtils.unmarschal(new ClassPathResource(xmlFile, AddonXMLTest.class).getInputStream(),
			  true);
  }
  
  @Test(
		  dataProvider = "valid-xml-files")
  public void testBuild(final String xmlFile) throws IOException {
	  new Addon().build(new ClassPathResource(xmlFile, AddonXMLTest.class).getInputStream());
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