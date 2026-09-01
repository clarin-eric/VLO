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

import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ImporterTestcase;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test for {@link eu.clarin.cmdi.vlo.importer.api}.
 *
 * <p>
 * This test is the guard on the published embedding API. It exercises the whole
 * path an embedder uses — supply your own concept links, decorate the mapping
 * with facets VLO does not define, cache mappings yourself, analyze a record and
 * read per-value provenance back out — so a change that breaks embedders fails
 * here rather than downstream.
 * </p>
 */
public class CmdiRecordAnalyzerContractTest extends ImporterTestcase {

    private static final String PROFILE_ID = "clarin.eu:cr1:p_1274880881885";

    private FacetsMappingBuilder mappingBuilder;

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();
        config.setFacetConceptsFile(getTestFacetConceptFilePath());
        config.setFacetsConfigFile(
                new File(Objects.requireNonNull(getClass().getResource("/facetConfigTest.xml")).toURI()).getAbsolutePath());
        mappingBuilder = new FacetsMappingBuilder(config, marshaller);
    }

    /**
     * The motivating end-to-end case: an embedder that resolves concept links
     * itself, adds facets VLO does not define, and needs per-value provenance.
     */
    @Test
    public void analysesRecordWithCallerSuppliedMappingAndExtraFacets() throws Exception {
        final FacetsMappingProvider provider = schemaLocation
                -> new FacetsMappingDecorator(
                        mappingBuilder.build(schemaLocation, emptyConceptLinks()),
                        extraFacets());

        final CmdiRecordAnalyzer analyzer = CmdiRecordAnalyzer.create(config, provider);

        final Optional<CMDIData<Map<String, List<ValueSet>>>> result = analyzer.analyze(corpusRecord());

        assertTrue(result.isPresent(), "record should not have been skipped");
        final CMDIData<Map<String, List<ValueSet>>> data = result.get();

        // the record was really parsed, not just constructed
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0000-0001-D", data.getId());
        final List<Resource> metadataResources = data.getMetadataResources();
        assertEquals(3, metadataResources.size());
        assertEquals("../acqui_data/Corpusstructure/acqui.imdi.cmdi",
                metadataResources.getFirst().getResourceName());

        // the decorated facet, which exists only because the embedder added it,
        // captured a value that VLO itself never stores
        final Map<String, List<ValueSet>> document = data.getDocument();
        final List<ValueSet> mdProfile = document.get("test_mdProfile");
        assertNotNull(mdProfile, "decorated facet should have been applied; facets present: " + document.keySet());
        assertEquals(PROFILE_ID, mdProfile.getFirst().getValue());

        // provenance is retained, which is the reason for ValueSetCmdiData
        final ValueSet valueSet = mdProfile.getFirst();
        assertEquals("test_mdProfile", valueSet.getTargetFacetName());
        assertFalse(valueSet.isDerived());
        assertFalse(valueSet.isResultOfValueMapping());
        assertNotNull(valueSet.getValueLanguagePair());
        assertTrue(valueSet.getVtdIndex() >= 0, "value should carry its source position");
    }

    /**
     * The provider is the embedder's caching seam: the analyzer must go through
     * it for every record, and must resolve the profile id to a schema location
     * before doing so.
     */
    @Test
    public void routesEveryRecordThroughTheMappingProvider() throws Exception {
        final Map<String, FacetsMapping> cache = new ConcurrentHashMap<>();
        final AtomicInteger lookups = new AtomicInteger();

        final FacetsMappingProvider caching = schemaLocation -> {
            lookups.incrementAndGet();
            return cache.computeIfAbsent(schemaLocation,
                    location -> mappingBuilder.build(location, emptyConceptLinks()));
        };

        final CmdiRecordAnalyzer analyzer = CmdiRecordAnalyzer.create(config, caching);

        analyzer.analyze(corpusRecord());
        analyzer.analyze(corpusRecord());

        assertEquals(2, lookups.get(), "provider should be consulted per record");
        assertEquals(1, cache.size(), "both records resolve to the same profile");

        // the provider receives a resolved schema location, not the raw profile id
        final String schemaLocation = cache.keySet().iterator().next();
        assertEquals(config.getComponentRegistryProfileSchema(PROFILE_ID), schemaLocation);
    }

    /**
     * The decorator must not disturb the mapping it wraps.
     */
    @Test
    public void decoratorAddsFacetsWithoutMutatingTheDelegate() {
        final FacetsMapping delegate = mappingBuilder.build(
                config.getComponentRegistryProfileSchema(PROFILE_ID), emptyConceptLinks());
        final int facetsBefore = delegate.getFacetDefinitions().size();

        final FacetsMapping decorated = new FacetsMappingDecorator(delegate, extraFacets());

        assertEquals(facetsBefore + 1, decorated.getFacetDefinitions().size());
        assertEquals(facetsBefore, delegate.getFacetDefinitions().size(), "delegate must be untouched");
        assertTrue(decorated.getFacetConfigurationNames().contains("test_mdProfile"));
        assertFalse(delegate.getFacetConfigurationNames().contains("test_mdProfile"));

        // added definitions resolve, delegate definitions still resolve through
        assertEquals("test_mdProfile", decorated.getFacetDefinition("test_mdProfile").getName());
        assertSame(delegate.getFacetsConfigurations(), decorated.getFacetsConfigurations());
    }

    /**
     * A missing mapping provider is the one misconfiguration worth failing loudly
     * on, since everything else has a usable default.
     */
    @Test
    public void requiresAMappingProvider() {
        assertThrows(NullPointerException.class,
                () -> CmdiRecordAnalyzer.create(config, null));
    }

    /**
     * Values added by facet name rather than by {@link ValueSet} still arrive as
     * ValueSets, so consumers never have to special-case them.
     */
    @Test
    public void synthesisesValueSetsForValuesAddedByName() {
        final ValueSetCmdiData data = new ValueSetCmdiData(fieldNameService);

        data.addDocField("some_facet", "some value", false);

        final List<ValueSet> values = data.getDocument().get("some_facet");
        assertEquals(1, values.size());
        assertEquals("some value", values.get(0).getValue());
        assertEquals(-1, values.get(0).getVtdIndex(), "no source position for synthesised values");
        assertEquals(List.of("some value"), List.copyOf(data.getDocField("some_facet")));
        assertTrue(data.hasField("some_facet"));

        data.removeField("some_facet");
        assertFalse(data.hasField("some_facet"));
        assertNull(data.getDocField("some_facet"), "absent facet reads as null, not empty");
    }

    private Map<String, List<Pattern>> emptyConceptLinks() {
        // an embedder that finds no concept links still gets a usable mapping:
        // the fallback patterns from facetConcepts.xml apply
        return Map.of();
    }

    private Map<String, FacetDefinition> extraFacets() {
        final FacetDefinition mdProfile = new FacetDefinition(null, "test_mdProfile");
        mdProfile.setPattern(new Pattern("/cmd:CMD/cmd:Header/cmd:MdProfile/text()"));

        final LinkedHashMap<String, FacetDefinition> extra = new LinkedHashMap<>();
        extra.put("test_mdProfile", mdProfile);
        return extra;
    }

    private File corpusRecord() throws Exception {
        final String content = """
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
        return createCmdiFile("contractCorpus", content);
    }
}
