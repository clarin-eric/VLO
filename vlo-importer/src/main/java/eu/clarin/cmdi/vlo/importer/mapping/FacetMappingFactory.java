package eu.clarin.cmdi.vlo.importer.mapping;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.ximpleware.NavException;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.AcceptableContext;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.RejectableContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Creates facet-mappings (xpaths) from a configuration. As they say "this is
 * where the magic happens". Also does some caching.
 */
public class FacetMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);

    private final ConcurrentHashMap<String, FacetsMapping> mapping = new ConcurrentHashMap<>();

    /**
     * Our one instance of the FMF.
     */
    private final VloConfig vloConfig;

    private final FieldNameServiceImpl fieldNameService;

    protected final FacetConceptMapping conceptMapping;

    protected final FacetsConfiguration facetsConfiguration;

    private final FacetsMapping baseMapping;

    public FacetMappingFactory(VloConfig vloConfig, VLOMarshaller marshaller) {
        this(vloConfig, marshaller, new ValueMappingFactoryDOMImpl());
    }

    public FacetMappingFactory(VloConfig vloConfig, VLOMarshaller marshaller, ValueMappingFactory valueMappingFactory) {
        this.vloConfig = vloConfig;
        this.fieldNameService = new FieldNameServiceImpl(vloConfig);

        this.conceptMapping = marshaller.getFacetConceptMapping(vloConfig.getFacetConceptsFile());
        if (conceptMapping == null) {
            throw new RuntimeException("Cannot proceed without facet concept mapping");
        }

        this.facetsConfiguration = marshaller.getFacetsConfiguration(vloConfig.getFacetsConfigFile());
        if (facetsConfiguration == null) {
            throw new RuntimeException("Cannot proceed without facets configuration");
        }

        this.baseMapping = createBaseMapping(conceptMapping, facetsConfiguration);

        try {
            valueMappingFactory.createValueMapping(vloConfig.getValueMappingsFile(), conceptMapping, baseMapping);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.error("Value Mappings not initialized!", ex);
        }
    }

    public FacetConceptMapping getConceptMapping() {
        return this.conceptMapping;
    }

    /**
     *
     * @param facetConceptsFile path to facet concepts file, leave null or empty
     * to use default
     * @param xsd
     * @param useLocalXSDCache
     * @return
     */
    public FacetsMapping getFacetMapping(String xsd, Boolean useLocalXSDCache) {
        return getOrCreateMapping(xsd, useLocalXSDCache);
    }

    /**
     * Get facet concept mapping.
     *
     * Get facet mapping used to map meta data based on a facet concepts file
     * and url to cmdi meta data profile.
     *
     * @param facetConcepts name of the facet concepts file
     * @param xsd url of xml schema of cmdi profile
     * @param useLocalXSDCache use local XML schema files instead of accessing
     * the component registry
     *
     * @return facet concept mapping
     */
    protected FacetsMapping getOrCreateMapping(String xsd, Boolean useLocalXSDCache) {
        return mapping.computeIfAbsent(xsd, (key) -> {
            return createMapping(new ConceptLinkPathMapperImpl(this.vloConfig, xsd, useLocalXSDCache));
        });
    }

    private static FacetsMapping createBaseMapping(FacetConceptMapping conceptMapping, FacetsConfiguration facetsConfiguration) {
        LOG.debug("Creating base mapping");

        final Map<String, Facet> facetConfigMap
                = Maps.newHashMap(facetsConfiguration.getFacet()
                        .stream()
                        .collect(Collectors.toMap(Facet::getName, Functions.identity()))
                );

        // Gets the configuration. VLOMarshaller only reads in the facetconceptmapping.xml file and returns the facetMapping (though the reading in is implicit).
//        FacetConceptMapping conceptMapping = marshaller.getFacetConceptMapping(facetConcepts);
        FacetsMapping facetMapping = new FacetsMapping(facetConfigMap);

        // Below we put the stuff we found into the configuration class.
        for (FacetConcept facetConcept : conceptMapping.getFacetConcepts()) {
            LOG.trace("-- Facet concept {}", facetConcept);
            final FacetDefinition definition = facetMapping.getFacetDefinition(facetConcept.getName());
            final Facet facetConfig = facetConfigMap.get(facetConcept.getName());

            if (facetConfig != null) {
                definition.setPropertiesFromConfig(facetConfig);
            }

            definition.setFallbackPatterns(facetConcept.getPatterns());

            //set derived facets
            for (String derivedFacetName : facetConcept.getDerivedFacets()) {

                definition.addDerivedFacet(facetMapping.getFacetDefinition(derivedFacetName));
            }

        }

        return facetMapping;
    }

    /**
     * Create facet concept mapping.
     *
     * Create facet mapping used to map meta data based on a facet concept
     * mapping file and url to cmdi meta data profile.
     *
     * @param facetConcepts name of the facet concepts file
     * @param xsd url of xml schema of cmdi profile
     * @param useLocalXSDCache use local XML schema files instead of accessing
     * the component registry
     *
     * @return the facet mapping used to map meta data to facets
     */
    protected FacetsMapping createMapping(ConceptLinkPathMapper clpMapper) {
        LOG.debug("Creating mapping for {} using {} (useLocalXSDCache: {})", clpMapper.getXsd(), this.vloConfig.getFacetConceptsFile(), clpMapper.useLocalXSDCache());

        // Gets the configuration. VLOMarshaller only reads in the facetconceptmapping.xml file and returns the facetMapping (though the reading in is implicit).
//        FacetConceptMapping conceptMapping = marshaller.getFacetConceptMapping(facetConcepts);
        FacetsMapping facetMapping = null;
        try {
            facetMapping = (FacetsMapping) baseMapping.clone();
            //The magic
            Map<String, List<Pattern>> conceptLinkPathMapping = clpMapper.createConceptLinkPathMapping();
            Map<Pattern, String> pathConceptLinkMapping = null;
            // Below we put the stuff we found into the configuration class.
            for (FacetConcept facetConcept : this.conceptMapping.getFacetConcepts()) {
                LOG.trace("-- Facet concept {}", facetConcept);
                FacetDefinition config = facetMapping.getFacetDefinition(facetConcept.getName());
                List<Pattern> xpaths = new ArrayList<>();
                handleId(xpaths, facetConcept);
                for (String concept : facetConcept.getConcepts()) {
                    LOG.trace("-- -- Concept {}", concept);
                    List<Pattern> paths = conceptLinkPathMapping.get(concept);
                    if (paths != null) {
                        if (facetConcept.hasContext()) {
                            LOG.trace("-- -- -- Concept has context");
                            for (Pattern path : paths) {
                                LOG.trace("-- -- -- Concept path {}", path);
                                // lazily instantiate the reverse mapping, i.e., from path to concept
                                if (pathConceptLinkMapping == null) {
                                    pathConceptLinkMapping = new HashMap<>();
                                    for (String c : conceptLinkPathMapping.keySet()) {
                                        for (Pattern p : conceptLinkPathMapping.get(c)) {
                                            pathConceptLinkMapping.put(p, c);
                                        }
                                    }
                                }
                                String context = getContext(path, pathConceptLinkMapping);
                                boolean handled = false;
                                // check against acceptable context
                                if (facetConcept.hasAcceptableContext()) {
                                    AcceptableContext acceptableContext = facetConcept.getAcceptableContext();
                                    if (context == null && acceptableContext.includeEmpty()) {
                                        // no context is accepted
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](empty) is accepted", facetConcept.getName(), path, context);
                                        xpaths.add(path);
                                        handled = true;
                                    } else if (acceptableContext.getConcepts().contains(context)) {
                                        // a specific context is accepted
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}] is accepted", facetConcept.getName(), path, context);
                                        xpaths.add(path);
                                        handled = true;
                                    }
                                }
                                // check against rejectable context
                                if (!handled && facetConcept.hasRejectableContext()) {
                                    RejectableContext rejectableContext = facetConcept.getRejectableContext();
                                    if (context == null && rejectableContext.includeEmpty()) {
                                        // no context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](empty) is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    } else if (rejectableContext.getConcepts().contains(context)) {
                                        // a specific context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}] is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    } else if (rejectableContext.includeAny()) {
                                        // any context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](any) is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    }
                                }
                                if (!handled && context != null && facetConcept.hasAcceptableContext() && facetConcept.getAcceptableContext().includeAny()) {
                                    // any, not rejected context, is accepted
                                    LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](any) is accepted", facetConcept.getName(), path, context);
                                    xpaths.add(path);
                                }
                            }
                        } else {
                            LOG.trace("-- -- -- Paths: {} (concept has no context)", paths);
                            xpaths.addAll(paths);
                        }
                    }
                }

                // pattern-based blacklisting: remove all XPath expressions that contain a blacklisted substring;
                // this is basically a hack to enhance the quality of the visualised information in the VLO;
                // should be replaced by a more intelligent approach in the future
                for (Pattern blacklistPattern : facetConcept.getBlacklistPatterns()) {
                    Iterator<Pattern> xpathIterator = xpaths.iterator();
                    while (xpathIterator.hasNext()) {
                        Pattern xpath = xpathIterator.next();
                        if (xpath.getPattern().contains(blacklistPattern.getPattern())) {
                            LOG.debug("Rejecting {} because of blacklisted substring {}", xpath, blacklistPattern);
                            xpathIterator.remove();
                        }
                    }
                }

                LinkedHashSet<Pattern> linkedHashSet = new LinkedHashSet<>(xpaths);
                if (xpaths.size() != linkedHashSet.size()) {
                    LOG.error("Duplicate XPaths for facet {} in: {}.", facetConcept.getName(), xpaths);
                }
                config.setPatterns(new ArrayList<>(linkedHashSet));

            }
        } catch (NavException | URISyntaxException e) {
            LOG.error("Error creating facetMapping from xsd: {}", clpMapper.getXsd(), e);
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            LOG.error("couldn't clone the base facet map", e);
        }
        LOG.debug("Mapping for {}: {}", clpMapper.getXsd(), facetMapping);
        return facetMapping;
    }

    /**
     * Look if there is a contextual (container) data category associated with
     * an ancestor by walking back.
     */
    private String getContext(Pattern path, Map<Pattern, String> pathConceptLinkMapping) {
        String context = null;
        String cpath = path.getPattern();
        while (context == null && !cpath.equals("/text()")) {
            if (cpath.contains("@")) {
                // go to the parent element of the attribute
                cpath = cpath.replaceAll("/@.*", "/text()");
            } else {
                // go to the parent element of the element
                cpath = cpath.replaceAll("/[^/]*/text\\(\\)", "/text()");
            }
            context = pathConceptLinkMapping.get(new Pattern(cpath));
        }
        return context;
    }

    /**
     * The id facet is special case and patterns must be added first. The
     * standard pattern to get the id out of the header is the most reliable and
     * it should fall back on concept matching if nothing matches. (Note this is
     * the exact opposite of other facets where the concept match is probably
     * better then the 'hardcoded' pattern).
     */
    private void handleId(List<Pattern> xpaths, FacetConcept facetConcept) {
        if (fieldNameService.getFieldName(FieldKey.ID).equals(facetConcept.getName())) {
            xpaths.addAll(facetConcept.getPatterns());
        }
    }

    public void printMapping(File file) throws IOException {
        final FileWriter fileWriter = new FileWriter(file);
        printMapping(() -> fileWriter);
    }

    public void printMapping(Supplier<Writer> writerFactory) throws IOException {
        final Set<String> xsdNames = mapping.keySet();
        try (Writer writer = writerFactory.get()) {
            writer.append("This file is generated on " + DateFormat.getDateTimeInstance().format(new Date())
                    + " and only used to document the mapping.\n");
            writer.append("This file contains xsd name and a list of conceptName with xpath mappings that are generated.\n");
            writer.append("---------------------\n");
            writer.flush();
            for (String xsd : xsdNames) {
                FacetsMapping facetMapping = mapping.get(xsd);
                writer.append(xsd + "\n");
                for (FacetDefinition config : facetMapping.getFacetDefinitions()) {
                    writer.append("FacetName:" + config.getName() + "\n");
                    writer.append("Mappings:\n");
                    for (Pattern pattern : config.getPatterns()) {
                        writer.append("    " + pattern + "\n");
                    }
                }
                writer.append("---------------------\n");
                writer.flush();
            }
        }
    }

    public FacetsMapping getBaseMapping() {
        return baseMapping;
    }
}
