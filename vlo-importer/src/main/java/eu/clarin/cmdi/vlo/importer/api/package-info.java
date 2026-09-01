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
/**
 * Stable API for embedding VLO's CMDI analysis in another application.
 *
 * <p>
 * This package exists for consumers that want to run a CMDI record through
 * VLO's facet mapping and inspect the result, without running a VLO import. The
 * CLARIN curation dashboard is the motivating consumer, but nothing here is
 * curation-specific.
 * </p>
 *
 * <h2>Typical use</h2>
 * <pre>{@code
 * // 1. apply VLO's facet config to concept links you resolved yourself
 * FacetsMappingBuilder builder = new FacetsMappingBuilder(vloConfig);
 * Map<String, List<Pattern>> conceptLinkPaths = myProfileParser.conceptLinkPaths(schemaLocation);
 * FacetsMapping mapping = builder.build(schemaLocation, conceptLinkPaths);
 *
 * // 2. optionally add facets that are not in facetConcepts.xml
 * mapping = new FacetsMappingDecorator(mapping, myExtraDefinitions);
 *
 * // 3. analyse records, caching mappings however you like
 * CmdiRecordAnalyzer analyzer = CmdiRecordAnalyzer.create(vloConfig, myCachingProvider);
 *
 * Optional<CMDIData<Map<String, List<ValueSet>>>> record = analyzer.analyze(file);
 * }</pre>
 *
 * <h2>Document type</h2>
 * <p>
 * The analyzer produces records whose document is
 * {@code Map<String, List<ValueSet>>}: every facet keeps its full
 * {@link eu.clarin.cmdi.vlo.importer.processor.ValueSet} rather than just the
 * string value, so the embedder can still see the VTD index a value came from
 * and whether it was derived or produced by a value mapping. VLO's own importer
 * discards that; an analysis tool generally needs it.
 * </p>
 *
 * <h2>Compatibility</h2>
 * <p>
 * Treat the public types in this package as a published contract: additive
 * changes are fine, but changing an existing signature breaks embedders.
 * {@code CmdiRecordAnalyzerContractTest} in the test sources pins the surface;
 * if it stops compiling, the change is breaking.
 * </p>
 */
package eu.clarin.cmdi.vlo.importer.api;
