package eu.clarin.cmdi.vlo.importer.processor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.Vocabulary;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {

    public static final String ENGLISH_LANGUAGE = "code:eng";
    public static final String DEFAULT_LANGUAGE = "code:und";

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final Boolean useLocalXSDCache;
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    private final SelfLinkExtractor selfLinkExtractor = new SelfLinkExtractorImpl();
    private final Vocabulary CCR;
    private final FacetMappingFactory facetMappingFactory;
    private final FieldNameServiceImpl fieldNameService;
    private final VloConfig config;
    private final VLOMarshaller marshaller;

    public CMDIParserVTDXML(Map<String, AbstractPostNormalizer> postProcessors, VloConfig config, FacetMappingFactory facetMappingFactory, VLOMarshaller marshaller, Boolean useLocalXSDCache) {
        this.config = config;
        this.marshaller = marshaller;
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.facetMappingFactory = facetMappingFactory;
        this.CCR = new Vocabulary(config.getConceptRegistryUrl());
        this.fieldNameService = new FieldNameServiceImpl(config);
    }

    @Override
    public CMDIData process(File file, ResourceStructureGraph resourceStructureGraph) throws VTDException, IOException, URISyntaxException {
        final CMDIData cmdiData = new CMDIData(this.fieldNameService);
        final VTDGen vg = new VTDGen();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            vg.setDoc(IOUtils.toByteArray(fileInputStream));
            vg.parse(true);
        }

        final VTDNav nav = vg.getNav();
        final FacetMapping facetMapping = getFacetMapping(nav.cloneNav());

        if (facetMapping.getFacetConfigurations().isEmpty()) {
            LOG.error("Problems mapping facets for file: {}", file.getAbsolutePath());
        }

        nav.toElement(VTDNav.ROOT);
        processResources(cmdiData, nav, resourceStructureGraph);
        FacetProcessor facetProcessor = new FacetProcessor(postProcessors, config, facetMappingFactory, marshaller, useLocalXSDCache);
        facetProcessor.processFacets(cmdiData, nav, facetMapping);
        return cmdiData;
    }

    /**
     * Extracts valid XML patterns for all facet definitions
     *
     * @param nav VTD Navigator
     * @return the facet mapping used to map meta data to facets
     * @throws VTDException
     */
    private FacetMapping getFacetMapping(VTDNav nav) throws VTDException {
        final String profileId = SchemaParsingUtil.extractXsd(nav);
        if (profileId == null) {
            throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
        }

        return facetMappingFactory.getFacetMapping(profileId, useLocalXSDCache);
    }

    @Override
    public String extractMdSelfLink(File file) throws IOException {
        return selfLinkExtractor.extractMdSelfLink(file);
    }

    /**
     * Extract ResourceProxies from ResourceProxyList
     *
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @throws VTDException
     */
    private void processResources(CMDIData cmdiData, VTDNav nav, ResourceStructureGraph resourceStructureGraph) throws VTDException {
        AutoPilot mdSelfLink = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(mdSelfLink, null);
        mdSelfLink.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink");
        String mdSelfLinkString = mdSelfLink.evalXPathToString();
        if (resourceStructureGraph != null) {
            resourceStructureGraph.addResource(mdSelfLinkString);
        }

        AutoPilot resourceProxy = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceProxy, null);
        resourceProxy.selectXPath("/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy");

        AutoPilot resourceRef = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceRef, null);
        resourceRef.selectXPath("cmd:ResourceRef");

        AutoPilot resourceType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceType, null);
        resourceType.selectXPath("cmd:ResourceType");

        AutoPilot resourceMimeType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceMimeType, null);
        resourceMimeType.selectXPath("cmd:ResourceType/@mimetype");

        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();

            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                cmdiData.addResource(ref, type, mimeType);
            }

            // resource hierarchy information?
            if (resourceStructureGraph != null && type.toLowerCase().equals("metadata")) {
                resourceStructureGraph.addEdge(ref, mdSelfLinkString);
            }
        }
    }

}
