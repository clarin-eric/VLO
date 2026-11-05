/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.importer.api;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataBaseImpl;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.ImporterTestcase;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.mapping.ConceptLinkPathMapper;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test for {@link CmdiRecordAnalyzer}: a change that breaks embedders
 * fails here.
 */
public class CmdiRecordAnalyzerContractTest extends ImporterTestcase {

    private static final String PROFILE_ID = "clarin.eu:cr1:p_1274880881885";

    private static final Map<String, String> EXTRA_FACETS
            = Map.of("test_mdProfile", "/cmd:CMD/cmd:Header/cmd:MdProfile/text()");

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        config.setFacetConceptsFile(getTestFacetConceptFilePath());
        config.setFacetsConfigFile(
                new File(Objects.requireNonNull(getClass().getResource("/facetConfigTest.xml")).toURI()).getAbsolutePath());
    }

    @Test
    public void analysesRecordWithCallerSuppliedConceptPathsAndExtraFacets() throws Exception {
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> analyzer = new CmdiRecordAnalyzer<>(config, strategy(noConceptPaths()));

        final Optional<CMDIData<Map<String, List<ValueSet>>>> result = analyzer.analyze(corpusRecord());

        assertTrue(result.isPresent(), "record should not have been skipped");
        final CMDIData<Map<String, List<ValueSet>>> data = result.get();

        assertEquals("test-hdl_58_1839_47_00-0000-0000-0000-0001-D", data.getId());
        final List<Resource> metadataResources = data.getMetadataResources();
        assertEquals(3, metadataResources.size());
        assertEquals("../acqui_data/Corpusstructure/acqui.imdi.cmdi",
                metadataResources.getFirst().getResourceName());

        // a value that only the extra facet captures
        final Map<String, List<ValueSet>> document = data.getDocument();
        final List<ValueSet> mdProfile = document.get("test_mdProfile");
        assertNotNull(mdProfile, "extra facet should have been applied; facets present: " + document.keySet());
        assertEquals(PROFILE_ID, mdProfile.getFirst().getValue());

        final ValueSet valueSet = mdProfile.getFirst();
        assertEquals("test_mdProfile", valueSet.getTargetFacetName());
        assertFalse(valueSet.isDerived());
        assertFalse(valueSet.isResultOfValueMapping());
        assertNotNull(valueSet.getValueLanguagePair());
        assertTrue(valueSet.getVtdIndex() >= 0, "value should carry its source position");
    }

    @Test
    public void buildsEachProfileMappingOnce() throws Exception {
        final List<String> requested = new CopyOnWriteArrayList<>();
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> analyzer = new CmdiRecordAnalyzer<>(config,
                strategy(schemaLocation -> {
                    requested.add(schemaLocation);
                    return Map.of();
                }));

        analyzer.analyze(corpusRecord());
        analyzer.analyze(corpusRecord());

        final String schemaLocation = config.getComponentRegistryProfileSchema(PROFILE_ID);
        assertEquals(List.of(schemaLocation), requested, "one lookup, with the resolved schema location");

        final FacetsMapping mapping = analyzer.getFacetsMapping(schemaLocation);
        assertSame(mapping, analyzer.getFacetsMapping(schemaLocation));
        assertEquals(1, requested.size(), "the profile report reuses the mapping built for analysis");
        assertEquals("test_mdProfile", mapping.getFacetDefinition("test_mdProfile").getName());
    }

    @Test
    public void doesNotCacheLocalSchemaLocations() {
        final AtomicInteger lookups = new AtomicInteger();
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> analyzer = new CmdiRecordAnalyzer<>(config,
                strategy(_ -> {
                    lookups.incrementAndGet();
                    return Map.of();
                }));

        analyzer.getFacetsMapping("file:///tmp/uploaded.xsd");
        analyzer.getFacetsMapping("file:///tmp/uploaded.xsd");

        assertEquals(2, lookups.get());
    }

    @Test
    public void appliesSuppliedConceptPathsToTheMatchingFacet() {
        // mapped to the facet projectName in facetConceptsTest.xml
        final String concept = "http://www.isocat.org/datcat/DC-2536";
        final String xpath = "/cmd:CMD/cmd:Components/cmdp:Test/cmdp:Name/text()";
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> analyzer = new CmdiRecordAnalyzer<>(config,
                strategy(_ -> Map.of(concept, List.of(new Pattern(xpath)))));

        final FacetsMapping mapping = analyzer.getFacetsMapping("https://example.org/profile.xsd");

        assertTrue(mapping.getFacetDefinition("projectName").getPatterns().stream()
                .anyMatch(pattern -> xpath.equals(pattern.getPattern())),
                "the XPath of the concept should be a pattern of projectName");
    }

    @Test
    public void requiresAllArguments() {
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer<>(null, strategy(noConceptPaths())));
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer<>(config, null));
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), null, false, false)));
    }

    @Test
    public void rejectsEmptyExtraFacets() {
        assertThrows(IllegalArgumentException.class, () -> new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), Map.of(), false, false)));
    }

    @Test
    public void skipsDuplicateIdsOnlyIfTheStrategySaysSo() throws Exception {
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> keeping = new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), EXTRA_FACETS, false, false));
        assertTrue(keeping.analyze(corpusRecord()).isPresent());
        assertTrue(keeping.analyze(corpusRecord()).isPresent());

        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> skipping = new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), EXTRA_FACETS, true, false));
        assertTrue(skipping.analyze(corpusRecord()).isPresent());
        assertFalse(skipping.analyze(corpusRecord()).isPresent());
    }

    @Test
    public void skipsRecordsWithoutResourcesOnlyIfTheStrategySaysSo() throws Exception {
        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> keeping = new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), EXTRA_FACETS, false, false));
        assertTrue(keeping.analyze(recordWithoutResources()).isPresent());

        final CmdiRecordAnalyzer<Map<String, List<ValueSet>>> skipping = new CmdiRecordAnalyzer<>(config,
                new TestStrategy(noConceptPaths(), EXTRA_FACETS, false, true));
        assertFalse(skipping.analyze(recordWithoutResources()).isPresent());
    }

    private static TestStrategy strategy(Function<String, Map<String, List<Pattern>>> conceptPaths) {
        return new TestStrategy(conceptPaths, EXTRA_FACETS, false, false);
    }

    private static Function<String, Map<String, List<Pattern>>> noConceptPaths() {
        // the fallback patterns from facetConcepts.xml still apply
        return schemaLocation -> Map.of();
    }

    private File corpusRecord() throws Exception {
        return createCmdiFile("contractCorpus", CORPUS_RECORD);
    }

    private File recordWithoutResources() throws Exception {
        return createCmdiFile("contractNoResources",
                CORPUS_RECORD.replaceAll("(?s)<ResourceProxyList>.*</ResourceProxyList>", "<ResourceProxyList/>"));
    }

    private record TestStrategy(Function<String, Map<String, List<Pattern>>> conceptPaths,
            Map<String, String> extraFacets, boolean skipDuplicates, boolean skipNoResources)
            implements ProcessingStrategy<Map<String, List<ValueSet>>> {

        @Override
        public CMDIRecordProcessor<Map<String, List<ValueSet>>> recordProcessor(
                CMDIDataProcessor<Map<String, List<ValueSet>>> parser, FieldNameService fieldNames) {
            return new CMDIRecordProcessor<>(parser, fieldNames) {
                @Override
                protected boolean skipOnDuplicateId() {
                    return skipDuplicates;
                }

                @Override
                protected boolean skipOnNoResources() {
                    return skipNoResources;
                }
            };
        }

        @Override
        public CMDIDataFactory<Map<String, List<ValueSet>>> dataFactory(FieldNameService fieldNames) {
            return () -> new ValueSetData(fieldNames);
        }

        @Override
        public ConceptLinkPathMapper conceptLinkPathMapper(String schemaLocation) {
            return new ConceptLinkPathMapper() {
                @Override
                public Map<String, List<Pattern>> createConceptLinkPathMapping() {
                    return conceptPaths.apply(schemaLocation);
                }

                @Override
                public String getXsd() {
                    return schemaLocation;
                }

                @Override
                public Boolean useLocalXSDCache() {
                    return true;
                }
            };
        }
    }

    /**
     * Keeps the {@link ValueSet}s, so a test can read their source position.
     */
    private static class ValueSetData extends CMDIDataBaseImpl<Map<String, List<ValueSet>>> {

        private final Map<String, List<ValueSet>> values = new HashMap<>();

        ValueSetData(FieldNameService fieldNameService) {
            super(fieldNameService);
        }

        @Override
        public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
            if (fieldNameService.getFieldName(FieldKey.ID).equals(valueSet.getTargetFacetName())) {
                setId(valueSet.getValue().trim());
            } else {
                values.computeIfAbsent(valueSet.getTargetFacetName(), name -> new ArrayList<>()).add(valueSet);
            }
        }

        @Override
        public void addDocField(String facetName, Object value, boolean caseInsensitive) {
        }

        @Override
        public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
            if (!hasField(valueSet.getTargetFacetName())) {
                addDocField(valueSet, caseInsensitive);
            }
        }

        @Override
        public Collection<Object> getDocField(String facetName) {
            final List<ValueSet> valueSets = values.get(facetName);
            return valueSets == null
                    ? null
                    : valueSets.stream().map(ValueSet::getValue).collect(Collectors.toList());
        }

        @Override
        public Map<String, List<ValueSet>> getDocument() {
            return values;
        }

        @Override
        public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
            removeField(valueSet.getTargetFacetName());
            addDocField(valueSet, caseInsensitive);
        }

        @Override
        public void replaceDocField(String facetName, Object value, boolean caseInsensitive) {
        }

        @Override
        public void removeField(String facetName) {
            values.remove(facetName);
        }

        @Override
        public boolean hasField(String facetName) {
            return values.containsKey(facetName);
        }

        @Override
        public Collection<Object> getFieldValues(String facetName) {
            return getDocField(facetName);
        }
    }

    private static final String CORPUS_RECORD = """
                <?xml version="1.0" encoding="UTF-8"?>
                <CMD xmlns="http://www.clarin.eu/cmd/1" xmlns:cmdp="http://www.clarin.eu/cmd/1/profiles/clarin.eu:cr1:p_1274880881885" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                   <Header>
                      <MdCreationDate>2003-01-14</MdCreationDate>
                      <MdSelfLink>test-hdl:1839/00-0000-0000-0000-0001-D</MdSelfLink>
                      <MdProfile>clarin.eu:cr1:p_1274880881885</MdProfile>
                   </Header>
                   <Resources>
                      <ResourceProxyList>
                         <ResourceProxy id="d28635e19">
                            <ResourceType>Metadata</ResourceType>
                            <ResourceRef>../acqui_data/Corpusstructure/acqui.imdi.cmdi</ResourceRef>
                         </ResourceProxy>
                         <ResourceProxy id="d28635e23">
                            <ResourceType>Metadata</ResourceType>
                            <ResourceRef>../Comprehension/Corpusstructure/comprehension.imdi.cmdi</ResourceRef>
                         </ResourceProxy>
                         <ResourceProxy id="d28635e26">
                            <ResourceType>Metadata</ResourceType>
                            <ResourceRef>../lac_data/Corpusstructure/lac.imdi.cmdi</ResourceRef>
                         </ResourceProxy>
                      </ResourceProxyList>
                      <JournalFileProxyList/>
                      <ResourceRelationList/>
                   </Resources>
                   <Components>
                      <cmdp:imdi-corpus>
                         <cmdp:Corpus>
                            <cmdp:Name>MPI corpora</cmdp:Name>
                            <cmdp:Title>Corpora of the Max-Planck Institute for Psycholinguistics</cmdp:Title>
                            <cmdp:descriptions>
                               <cmdp:Description LanguageId="">IMDI corpora</cmdp:Description>
                            </cmdp:descriptions>
                         </cmdp:Corpus>
                      </cmdp:imdi-corpus>
                   </Components>
                </CMD>
                """;
}
