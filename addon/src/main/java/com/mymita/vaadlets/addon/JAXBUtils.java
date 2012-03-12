package com.mymita.vaadlets.addon;

import static java.lang.String.format;
import static org.apache.commons.lang3.ClassUtils.getPackageCanonicalName;

import java.io.IOException;
import java.io.InputStream;
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
import com.mymita.vaadlets.Vaadlets;

public final class JAXBUtils {

	private static class ClasspathResourceResolver implements
			LSResourceResolver {

		public LSInput resolveResource(String type, String namespaceURI,
				String publicId, String systemId, String baseURI) {
			ClassPathResource resource = SCHEMAS.get(namespaceURI);
			if (resource != null) {
				try {
					LSInput input = new DOMInputImpl();
					input.setByteStream(resource.getInputStream());
					return input;
				} catch (IOException e) {
					throw new RuntimeException(format(
							"Can't load schema '%s' from classpath", namespaceURI), e);
				}
			} else
				return null;
		}
	}

	private static final String CONTEXTPATH = Joiner
			.on(":")
			.join(new String[] {
					getPackageCanonicalName(com.mymita.vaadlets.ObjectFactory.class),
					getPackageCanonicalName(com.mymita.vaadlets.core.ObjectFactory.class),
					getPackageCanonicalName(com.mymita.vaadlets.layout.ObjectFactory.class),
					getPackageCanonicalName(com.mymita.vaadlets.ui.ObjectFactory.class) });

	private static final Map<String, ClassPathResource> SCHEMAS = ImmutableMap
			.<String, ClassPathResource> builder()
			.put("http://www.mymita.com/vaadlets/1.0.0",
					new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-1.0.0.xsd"))
			.put("http://www.mymita.com/vaadlets/core/1.0.0",
					new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-core-1.0.0.xsd"))
			.put("http://www.mymita.com/vaadlets/layout/1.0.0",
					new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-layout-1.0.0.xsd"))
			.put("http://www.mymita.com/vaadlets/ui/1.0.0",
					new ClassPathResource("com/mymita/vaadlets/xsd/vaadlets-ui-1.0.0.xsd")).build();

	public static final Vaadlets unmarshal(final InputStream inputStream) {
		return unmarshal(inputStream, true);
	}

	public static final Vaadlets unmarshal(final InputStream inputStream,
			boolean validation) {
		return validation ? unmarshal(inputStream,
				SCHEMAS.get("http://www.mymita.com/vaadlets/1.0.0"))
				: unmarshal(inputStream, null);
	}

	private static final Vaadlets unmarshal(final InputStream inputStream,
			final Resource theSchemaResource) {
		try {
			final JAXBContext jc = JAXBContext.newInstance(CONTEXTPATH);
			final Unmarshaller unmarshaller = jc.createUnmarshaller();
			if (theSchemaResource != null) {
				final SchemaFactory schemaFactory = SchemaFactory
						.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				schemaFactory
						.setResourceResolver(new ClasspathResourceResolver());
				final Schema schema = schemaFactory.newSchema(new StreamSource(
						theSchemaResource.getInputStream()));
				unmarshaller.setSchema(schema);
			}
			return unmarshaller.unmarshal(
					XMLInputFactory.newInstance().createXMLStreamReader(
							inputStream), Vaadlets.class).getValue();
		} catch (final JAXBException | SAXException | XMLStreamException
				| FactoryConfigurationError | IOException e) {
			throw new RuntimeException("Can't unmarschal", e);
		}
	}
}