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
/**
 * Copyright 2012 Andreas Höhmann (mymita.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.mymita.vaadlets;

import static java.lang.String.format;
import static org.apache.commons.lang3.ClassUtils.getPackageCanonicalName;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.dom.DOMInputImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public final class JAXBUtils {

  private static class ClasspathResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, final String publicId,
        final String systemId, final String baseURI) {
      final ClassPathResource resource = SCHEMAS.get(namespaceURI);
      if (resource != null) {
        try {
          final LSInput input = new DOMInputImpl();
          input.setByteStream(resource.getInputStream());
          return input;
        } catch (final IOException e) {
          throw new RuntimeException(format("Can't load schema '%s' from classpath", namespaceURI), e);
        }
      } else {
        return null;
      }
    }
  }

  private static final String CONTEXTPATH = Joiner.on(":").join(
      new String[] {
        getPackageCanonicalName(com.mymita.vaadlets.ObjectFactory.class),
        getPackageCanonicalName(com.mymita.vaadlets.core.ObjectFactory.class),
        getPackageCanonicalName(com.mymita.vaadlets.layout.ObjectFactory.class),
        getPackageCanonicalName(com.mymita.vaadlets.ui.ObjectFactory.class),
        getPackageCanonicalName(com.mymita.vaadlets.input.ObjectFactory.class),
        getPackageCanonicalName(com.mymita.vaadlets.addon.ObjectFactory.class),
      });

  private static final Map<String, ClassPathResource> SCHEMAS = ImmutableMap
      .<String, ClassPathResource> builder()
      .put("http://www.mymita.com/vaadlets/1.0.0", new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-1.0.0.xsd"))
      .put("http://www.mymita.com/vaadlets/core/1.0.0",
          new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-core-1.0.0.xsd"))
      .put("http://www.mymita.com/vaadlets/layout/1.0.0",
          new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-layout-1.0.0.xsd"))
      .put("http://www.mymita.com/vaadlets/ui/1.0.0",
          new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-ui-1.0.0.xsd"))
      .put("http://www.mymita.com/vaadlets/input/1.0.0",
          new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-input-1.0.0.xsd"))
      .put("http://www.mymita.com/vaadlets/addon/1.0.0",
          new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-addon-1.0.0.xsd")).build();

  public static final Vaadlets unmarshal(final Reader aReader) {
    return unmarshal(aReader, true);
  }

  public static final Vaadlets unmarshal(final Reader aReader, final boolean validation) {
    return validation ? unmarshal(aReader, SCHEMAS.get("http://www.mymita.com/vaadlets/1.0.0")) : unmarshal(aReader,
        null);
  }

  private static final Vaadlets unmarshal(final Reader aReader, final Resource theSchemaResource) {
    try {
      final JAXBContext jc = JAXBContext.newInstance(CONTEXTPATH);
      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      if (theSchemaResource != null) {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(new ClasspathResourceResolver());
        final Schema schema = schemaFactory.newSchema(new StreamSource(theSchemaResource.getInputStream()));
        unmarshaller.setSchema(schema);
      }
      return unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(aReader), Vaadlets.class)
          .getValue();
    } catch (final JAXBException | SAXException | XMLStreamException | FactoryConfigurationError | IOException e) {
      throw new RuntimeException("Can't unmarschal", e);
    }
  }
}
