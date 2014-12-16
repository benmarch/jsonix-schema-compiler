package org.hisrc.jsonix.configuration;

import java.text.MessageFormat;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
import org.hisrc.jsonix.analysis.ModelInfoGraphAnalyzer;
import org.hisrc.jsonix.definition.Mapping;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MModelInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPackageInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.util.PackageInfoQNameAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = MappingConfiguration.LOCAL_ELEMENT_NAME)
@XmlType(propOrder = {})
public class MappingConfiguration {

	private final Logger logger = LoggerFactory
			.getLogger(MappingConfiguration.class);

	public static final String LOCAL_ELEMENT_NAME = "mapping";

	private String id;
	private String name;
	private String _package;
	private String defaultElementNamespaceURI;
	private String defaultAttributeNamespaceURI;
	private IncludesConfiguration includesConfiguration;
	private ExcludesConfiguration excludesConfiguration;

	public static final QName MAPPING_NAME = new QName(
			ModulesConfiguration.NAMESPACE_URI,
			MappingConfiguration.LOCAL_ELEMENT_NAME,
			ModulesConfiguration.DEFAULT_PREFIX);

	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "package")
	public String getPackage() {
		return _package;
	}

	public void setPackage(String _package) {
		this._package = _package;
	}

	@XmlAttribute(name = "defaultElementNamespaceURI")
	public String getDefaultElementNamespaceURI() {
		return defaultElementNamespaceURI;
	}

	public void setDefaultElementNamespaceURI(String defaultElementNamespaceURI) {
		this.defaultElementNamespaceURI = defaultElementNamespaceURI;
	}

	@XmlAttribute(name = "defaultAttributeNamespaceURI")
	public String getDefaultAttributeNamespaceURI() {
		return defaultAttributeNamespaceURI;
	}

	public void setDefaultAttributeNamespaceURI(
			String defaultAttributeNamespaceURI) {
		this.defaultAttributeNamespaceURI = defaultAttributeNamespaceURI;
	}

	@XmlElement(name = IncludesConfiguration.LOCAL_ELEMENT_NAME)
	public IncludesConfiguration getIncludesConfiguration() {
		return includesConfiguration;
	}

	public void setIncludesConfiguration(
			IncludesConfiguration includesConfiguration) {
		this.includesConfiguration = includesConfiguration;
	}

	@XmlElement(name = ExcludesConfiguration.LOCAL_ELEMENT_NAME)
	public ExcludesConfiguration getExcludesConfiguration() {
		return excludesConfiguration;
	}

	public void setExcludesConfiguration(
			ExcludesConfiguration excludesConfiguration) {
		this.excludesConfiguration = excludesConfiguration;
	}

	public <T, C extends T> Mapping<T, C> build(
			ModelInfoGraphAnalyzer<T, C> analyzer, MModelInfo<T, C> modelInfo,
			MPackageInfo packageInfo, Map<String, Mapping<T, C>> mappings) {
		Validate.notNull(modelInfo);
		Validate.notNull(packageInfo);
		Validate.notNull(mappings);

		final String packageName = getPackage();

		final String mappingName = getName();

		logger.debug(MessageFormat.format(
				"Package [{0}] will be mapped by the mapping [{1}].",
				packageName, mappingName));

		final PackageInfoQNameAnalyzer<T, C> qnameAnalyzer = new PackageInfoQNameAnalyzer<T, C>(
				modelInfo);

		final String draftMostUsedElementNamespaceURI = qnameAnalyzer
				.getMostUsedElementNamespaceURI(packageInfo);
		final String mostUsedElementNamespaceURI = draftMostUsedElementNamespaceURI == null ? ""
				: draftMostUsedElementNamespaceURI;

		final String defaultElementNamespaceURI;
		if (this.defaultElementNamespaceURI != null) {
			defaultElementNamespaceURI = this.defaultElementNamespaceURI;
		} else {
			logger.debug(MessageFormat
					.format("Mapping [{0}] will use \"{1}\" as it is the most used element namespace URI in the package [{2}].",
							mappingName, mostUsedElementNamespaceURI,
							packageName));
			defaultElementNamespaceURI = mostUsedElementNamespaceURI;

		}

		final String draftMostUsedAttributeNamespaceURI = qnameAnalyzer
				.getMostUsedAttributeNamespaceURI(packageInfo);
		final String mostUsedAttributeNamespaceURI = draftMostUsedAttributeNamespaceURI == null ? ""
				: draftMostUsedAttributeNamespaceURI;

		final String defaultAttributeNamespaceURI;
		if (this.defaultAttributeNamespaceURI != null) {
			defaultAttributeNamespaceURI = this.defaultAttributeNamespaceURI;
		} else {
			logger.debug(MessageFormat
					.format("Mapping [{0}] will use \"{1}\" as it is the most used attribute namespace URI in the package [{2}].",
							mappingName, mostUsedAttributeNamespaceURI,
							packageName));
			defaultAttributeNamespaceURI = mostUsedAttributeNamespaceURI;

		}

		final Mapping<T, C> mapping = new Mapping<T, C>(analyzer, packageInfo,
				mappingName, defaultElementNamespaceURI,
				defaultAttributeNamespaceURI);

		if (getExcludesConfiguration() != null) {
			final ExcludesConfiguration excludesConfiguration = getExcludesConfiguration();
			for (TypeInfoConfiguration typeInfoConfiguration : excludesConfiguration
					.getTypeInfoConfigurations()) {
				final MTypeInfo<T, C> typeInfo = typeInfoConfiguration
						.findTypeInfo(analyzer, packageInfo);
				if (typeInfo != null) {
					mapping.excludeTypeInfo(typeInfo);
				}
			}
			for (ElementInfoConfiguration elementInfoConfiguration : excludesConfiguration
					.getElementInfoConfigurations()) {
				final MElementInfo<T, C> elementInfo = elementInfoConfiguration
						.findElementInfo(analyzer, packageInfo);
				if (elementInfo != null) {
					mapping.excludeElementInfo(elementInfo);
				}
			}
			for (PropertyInfoConfiguration propertyInfoConfiguration : excludesConfiguration
					.getPropertyInfoConfigurations()) {
				final MPropertyInfo<T, C> propertyInfo = propertyInfoConfiguration
						.findPropertyInfo(analyzer, packageInfo);
				if (propertyInfo != null) {
					mapping.excludePropertyInfo(propertyInfo);
				}
			}
		}

		if (getIncludesConfiguration() == null) {
			logger.trace(MessageFormat
					.format("Includes configuration for the mapping [{0}] is not provided, including the whole package.",
							mappingName));
			mapping.includePackage(packageInfo);
		} else {
			final IncludesConfiguration includesConfiguration = getIncludesConfiguration();
			for (TypeInfoConfiguration typeInfoConfiguration : includesConfiguration
					.getTypeInfoConfigurations()) {
				final MTypeInfo<T, C> typeInfo = typeInfoConfiguration
						.findTypeInfo(analyzer, packageInfo);
				if (typeInfo != null) {
					mapping.includeTypeInfo(typeInfo);
				}
			}
			for (ElementInfoConfiguration elementInfoConfiguration : includesConfiguration
					.getElementInfoConfigurations()) {
				final MElementInfo<T, C> elementInfo = elementInfoConfiguration
						.findElementInfo(analyzer, packageInfo);
				if (elementInfo != null) {
					mapping.includeElementInfo(elementInfo);
				}
			}
			for (PropertyInfoConfiguration propertyInfoConfiguration : includesConfiguration
					.getPropertyInfoConfigurations()) {
				final MPropertyInfo<T, C> propertyInfo = propertyInfoConfiguration
						.findPropertyInfo(analyzer, packageInfo);
				if (propertyInfo != null) {
					mapping.includePropertyInfo(propertyInfo);
				}
			}
			for (DependenciesOfMappingConfiguration dependenciesOfMappingConfiguration : includesConfiguration
					.getDependenciesOfMappingConfiguration()) {
				final String id = dependenciesOfMappingConfiguration.getId();
				final Mapping<T, C> dependingMapping = mappings.get(id);
				if (dependingMapping == null) {
					throw new MissingMappingWithIdException(id);
				}
				mapping.includeDependenciesOfMapping(dependingMapping);
			}
		}

		return mapping;

	}

	@Override
	public String toString() {
		return MessageFormat.format("[{0}:{1}]", getId(), getName());
	}
}