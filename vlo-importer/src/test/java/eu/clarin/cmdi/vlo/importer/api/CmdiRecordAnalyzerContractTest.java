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
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
 * Contract test for {@link CmdiRecordAnalyzer}.
 *
 * <p>
 * This test is the guard on the published curation analysis API. It exercises the whole
 * path an embedder uses: supply concept links and extra facets, analyze a
 * record, read per-value provenance back out and ask for a profile's mapping.
 * A change that breaks embedders fails here rather than downstream.
 * </p>
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

    /**
     * The motivating end-to-end case: an embedder that resolves concept links
     * itself, adds facets VLO does not define, and needs per-value provenance.
     */
    @Test
    public void analysesRecordWithCallerSuppliedConceptPathsAndExtraFacets() throws Exception {
        final CmdiRecordAnalyzer analyzer = new CmdiRecordAnalyzer(config, noConceptPaths(), EXTRA_FACETS);

        final Optional<CMDIData<Map<String, List<ValueSet>>>> result = analyzer.analyze(corpusRecord());

        assertTrue(result.isPresent(), "record should not have been skipped");
        final CMDIData<Map<String, List<ValueSet>>> data = result.get();

        // the record was really parsed, not just constructed
        assertEquals("test-hdl_58_1839_47_00-0000-0000-0000-0001-D", data.getId());
        final List<Resource> metadataResources = data.getMetadataResources();
        assertEquals(3, metadataResources.size());
        assertEquals("../acqui_data/Corpusstructure/acqui.imdi.cmdi",
                metadataResources.getFirst().getResourceName());

        // the extra facet, which exists only because the embedder added it,
        // captured a value that VLO itself never stores
        final Map<String, List<ValueSet>> document = data.getDocument();
        final List<ValueSet> mdProfile = document.get("test_mdProfile");
        assertNotNull(mdProfile, "extra facet should have been applied; facets present: " + document.keySet());
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
     * Concept paths are asked for once per profile, with the profile's schema
     * location; analysis and {@code getFacetsMapping} share the same mapping.
     */
    @Test
    public void buildsEachProfileMappingOnce() throws Exception {
        final List<String> requested = new CopyOnWriteArrayList<>();
        final CmdiRecordAnalyzer analyzer = new CmdiRecordAnalyzer(config,
                schemaLocation -> {
                    requested.add(schemaLocation);
                    return Map.of();
                },
                EXTRA_FACETS);

        analyzer.analyze(corpusRecord());
        analyzer.analyze(corpusRecord());

        final String schemaLocation = config.getComponentRegistryProfileSchema(PROFILE_ID);
        assertEquals(List.of(schemaLocation), requested, "one lookup, with the resolved schema location");

        final FacetsMapping mapping = analyzer.getFacetsMapping(schemaLocation);
        assertSame(mapping, analyzer.getFacetsMapping(schemaLocation));
        assertEquals(1, requested.size(), "the profile report reuses the mapping built for analysis");
        assertEquals("test_mdProfile", mapping.getFacetDefinition("test_mdProfile").getName());
    }

    /**
     * Local schema locations, such as an uploaded profile, are built on every
     * call instead of being kept.
     */
    @Test
    public void doesNotCacheLocalSchemaLocations() {
        final AtomicInteger lookups = new AtomicInteger();
        final CmdiRecordAnalyzer analyzer = new CmdiRecordAnalyzer(config,
                schemaLocation -> {
                    lookups.incrementAndGet();
                    return Map.of();
                },
                Map.of());

        analyzer.getFacetsMapping("file:///tmp/uploaded.xsd");
        analyzer.getFacetsMapping("file:///tmp/uploaded.xsd");

        assertEquals(2, lookups.get());
    }

    /**
     * Supplied concept paths end up as the patterns of the facet whose concept
     * they belong to.
     */
    @Test
    public void appliesSuppliedConceptPathsToTheMatchingFacet() {
        // mapped to the facet projectName in facetConceptsTest.xml
        final String concept = "http://www.isocat.org/datcat/DC-2536";
        final String xpath = "/cmd:CMD/cmd:Components/cmdp:Test/cmdp:Name/text()";
        final CmdiRecordAnalyzer analyzer = new CmdiRecordAnalyzer(config,
                schemaLocation -> Map.of(concept, List.of(xpath)),
                Map.of());

        final FacetsMapping mapping = analyzer.getFacetsMapping("https://example.org/profile.xsd");

        assertTrue(mapping.getFacetDefinition("projectName").getPatterns().stream()
                .anyMatch(pattern -> xpath.equals(pattern.getPattern())),
                "the XPath of the concept should be a pattern of projectName");
    }

    @Test
    public void requiresAllArguments() {
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer(null, noConceptPaths(), Map.of()));
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer(config, null, Map.of()));
        assertThrows(NullPointerException.class, () -> new CmdiRecordAnalyzer(config, noConceptPaths(), null));
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

    /**
     * {@code addDocFieldIfNull} adds only to an absent facet, as in VLO's Solr
     * implementation, so {@code replaceDocField} keeps the new value.
     */
    @Test
    public void addsIfNullOnlyToAnAbsentFacet() {
        final ValueSetCmdiData data = new ValueSetCmdiData(fieldNameService);
        data.addDocField("some_facet", "first", false);
        final ValueSet first = data.getDocument().get("some_facet").getFirst();

        data.addDocFieldIfNull(first, false);
        assertEquals(1, data.getDocument().get("some_facet").size(), "present facet is left alone");

        data.replaceDocField(first, false);
        assertEquals(List.of("first"), List.copyOf(data.getDocField("some_facet")), "replace keeps the value");

        data.removeField("some_facet");
        data.addDocFieldIfNull(first, false);
        assertTrue(data.hasField("some_facet"), "absent facet is added");
    }

    private static Function<String, Map<String, List<String>>> noConceptPaths() {
        // an embedder that finds no concept links still gets a usable mapping:
        // the fallback patterns from facetConcepts.xml apply
        return schemaLocation -> Map.of();
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
