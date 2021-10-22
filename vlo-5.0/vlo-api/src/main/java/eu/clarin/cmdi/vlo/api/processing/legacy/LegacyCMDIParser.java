package eu.clarin.cmdi.vlo.api.processing.legacy;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParsingException;
import eu.clarin.cmdi.vlo.importer.processor.FacetProcessor;
import eu.clarin.cmdi.vlo.importer.processor.FacetProcessorVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.FacetValuesMapFilter;
import eu.clarin.cmdi.vlo.importer.processor.ProfileNameExtractor;
import eu.clarin.cmdi.vlo.importer.processor.ResourceProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ResourceProcessorVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.SchemaParsingUtil;
import eu.clarin.cmdi.vlo.importer.processor.ValueWriter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyCMDIParser<T> implements LegacyCMDIDataProcessor<T>  {

    private final static Logger LOG = LoggerFactory.getLogger(LegacyCMDIParser.class);

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final Boolean useLocalXSDCache;

    private final CMDIDataFactory<T> cmdiDataFactory;
    private final FacetMappingFactory facetMappingFactory;
    private final VloConfig config;
    private final VLOMarshaller marshaller;
    private final FieldNameService fieldNameService;
    private final ValueWriter valueWriter;
    private final ProfileNameExtractor profileNameExtractor;

    public LegacyCMDIParser(Map<String, AbstractPostNormalizer> postProcessors, List<FacetValuesMapFilter> postMappingFilters, VloConfig config, FacetMappingFactory facetMappingFactory, VLOMarshaller marshaller, CMDIDataFactory<T> cmdiDataFactory, FieldNameService fieldNameService, Boolean useLocalXSDCache) {
        this.config = config;
        this.marshaller = marshaller;
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.facetMappingFactory = facetMappingFactory;
        this.cmdiDataFactory = cmdiDataFactory;
        this.fieldNameService = fieldNameService;
        this.valueWriter = new ValueWriter(config, postProcessors, postMappingFilters);
        this.profileNameExtractor = new ProfileNameExtractor(config);
    }

    public CMDIData<T> process(VloRecordMappingRequest request, ResourceStructureGraph resourceStructureGraph) throws CMDIParsingException, VTDException, IOException, URISyntaxException {
        final CMDIData<T> cmdiData = cmdiDataFactory.newCMDIDataInstance();
        final VTDGen vg = new VTDGen();
        vg.setDoc(request.getXmlContent());
        vg.parse(true);

        final VTDNav nav = vg.getNav();
        final String profileId = SchemaParsingUtil.extractXsd(nav);
        final FacetsMapping facetMapping = getFacetMapping(nav.cloneNav(), profileId);

        // CMDI profile information
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE_ID), profileId, true);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE), profileNameExtractor.process(profileId), true);

        if (facetMapping.getFacetDefinitions().isEmpty()) {
            LOG.error("Problems mapping facets for file: {}", request);
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
    private FacetsMapping getFacetMapping(VTDNav nav, String profileId) throws VTDException {
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
