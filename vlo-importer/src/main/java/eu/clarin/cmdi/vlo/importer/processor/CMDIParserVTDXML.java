package eu.clarin.cmdi.vlo.importer.processor;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML<T> implements CMDIDataProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final Boolean useLocalXSDCache;

    private final CMDIDataFactory<T> cmdiDataFactory;
    private final FacetMappingFactory facetMappingFactory;
    private final VloConfig config;
    private final VLOMarshaller marshaller;
    private final FieldNameService fieldNameService;
    private final ValueWriter valueWriter;

    public CMDIParserVTDXML(Map<String, AbstractPostNormalizer> postProcessors, List<FacetValuesMapFilter> postMappingFilters, VloConfig config, FacetMappingFactory facetMappingFactory, VLOMarshaller marshaller, CMDIDataFactory<T> cmdiDataFactory, FieldNameService fieldNameService, Boolean useLocalXSDCache) {
        this.config = config;
        this.marshaller = marshaller;
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.facetMappingFactory = facetMappingFactory;
        this.cmdiDataFactory = cmdiDataFactory;
        this.fieldNameService = fieldNameService;
        this.valueWriter = new ValueWriter(config, postProcessors, postMappingFilters);
    }

    @Override
    public CMDIData<T> process(File file, ResourceStructureGraph resourceStructureGraph) throws CMDIParsingException, VTDException, IOException, URISyntaxException {
        final CMDIData<T> cmdiData = cmdiDataFactory.newCMDIDataInstance();
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

        newResourceProcessor(nav)
                .processResources(cmdiData, resourceStructureGraph);

        newFacetProcessor(nav)
                .processFacets(cmdiData, facetMapping);

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

    /**
     * Instantiates a new resource processor. Called for each processed file.
     *
     * @param nav
     * @return a new resource processor
     */
    protected ResourceProcessor newResourceProcessor(VTDNav nav) {
        return new ResourceProcessorVTDXML(nav);
    }

    /**
     * Instantiates a new facet processor. Called for each processed file.
     *
     * @param nav
     * @return a new facet processor
     */
    protected FacetProcessor newFacetProcessor(VTDNav nav) {
        return new FacetProcessorVTDXML(postProcessors, config, marshaller, valueWriter, fieldNameService, nav);
    }
}
