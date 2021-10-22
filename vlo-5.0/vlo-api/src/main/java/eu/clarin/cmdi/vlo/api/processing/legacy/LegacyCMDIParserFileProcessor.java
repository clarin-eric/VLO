/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.api.processing.legacy;

import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.api.processing.MetadataFileProcessor;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.exception.InputProcessingException;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.processor.FacetValuesMapFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Profile("legacy")
@Slf4j
public class LegacyCMDIParserFileProcessor implements MetadataFileProcessor {

    private final LanguageCodeUtils languageCodeUtils;
    private final ResourceStructureGraph resourceStructureGraph = new ResourceStructureGraph();
    private final FieldNameServiceImpl fieldNameService;
    private final VloRecordCMDIDataFactory cmdiDataFactory;
    private final FacetMappingFactory facetMappingFactory;
    private final VLOMarshaller marshaller;
    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final List<FacetValuesMapFilter> postMappingFilters;
    private final LegacyCMDIDataProcessor<VloRecord> parser;

    public LegacyCMDIParserFileProcessor(@Value("${vlo.api.legacy.config-location}") String configLocation) throws IOException {

        //TODO
        final URL configUrl = Path.of(configLocation).toUri().toURL();
        final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl);
        final VloConfig config = configFactory.newConfig();

        fieldNameService = new FieldNameServiceImpl(config);
        languageCodeUtils = new LanguageCodeUtils(config);

        //TODO
        postProcessors = MetadataImporter.registerPostProcessors(config, fieldNameService, languageCodeUtils);
        //TODO
        postMappingFilters = MetadataImporter.registerPostMappingFilters(fieldNameService);
        //TODO
        marshaller = new VLOMarshaller();

        facetMappingFactory = new FacetMappingFactory(config, marshaller);
        cmdiDataFactory = new VloRecordCMDIDataFactory(fieldNameService);
        parser = new LegacyCMDIParser<>(postProcessors, postMappingFilters, config, facetMappingFactory, marshaller, cmdiDataFactory, fieldNameService, false);

    }

    @Override
    public VloRecord processMappingRequest(VloRecordMappingRequest request) throws InputProcessingException {
        try {
            CMDIData<VloRecord> cmdiData = parser.process(request, resourceStructureGraph);
            return integrate(request, cmdiData);
        } catch (Exception ex) {
            throw new InputProcessingException("Error while processing input request", ex);
        }

    }

    private VloRecord integrate(VloRecordMappingRequest request, CMDIData<VloRecord> data) {
        final VloRecord record = data.getDocument();
        record.setId(data.getId());
        record.setSourcePath(request.getFile());
        record.setDataRoot(request.getDataRoot());
        record.setProfileId(getFirstValue(data, FieldKey.ID).orElse(null));
        record.setSelflink(getFirstValue(data, FieldKey.SELF_LINK).orElse(null));
        record.setResources(
                Streams.concat(
                        data.getDataResources().stream(),
                        data.getLandingPageResources().stream(),
                        data.getMetadataResources().stream(),
                        data.getSearchPageResources().stream(),
                        data.getSearchResources().stream())
                        .map(r -> VloRecord.Resource.builder()
                        .mediaType(r.getMimeType())
                        .type(r.getType())
                        .ref(r.getResourceName())
                        .build())
                        .collect(Collectors.toList()));
        return record;
    }

    private Optional<String> getFirstValue(CMDIData<VloRecord> data, FieldKey fieldKey) {
        return Optional.ofNullable(data.getFieldValues(fieldNameService.getFieldName(fieldKey)))
                .flatMap(c -> c.stream().findFirst().map(Object::toString));
    }

    public static class VloRecordCMDIDataFactory implements CMDIDataFactory<VloRecord> {

        private final FieldNameService fieldNameService;

        public VloRecordCMDIDataFactory(FieldNameService fieldNameService) {
            this.fieldNameService = fieldNameService;
        }

        @Override
        public CMDIData<VloRecord> newCMDIDataInstance() {
            return new CMDIDataVloRecordImpl(fieldNameService);
        }

    }

}
