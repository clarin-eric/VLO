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
 * // once per run
 * CmdiRecordAnalyzer analyzer = new CmdiRecordAnalyzer(
 *         vloConfig,
 *         schemaLocation -> myProfileParser.conceptPaths(schemaLocation),  // concept URI -> XPaths
 *         Map.of("my_mdProfile", "/cmd:CMD/cmd:Header/cmd:MdProfile/text()"));
 *
 * // per record
 * Optional<CMDIData<Map<String, List<ValueSet>>>> record = analyzer.analyze(file);
 *
 * // per profile, e.g. for a coverage report
 * FacetsMapping mapping = analyzer.getFacetsMapping(schemaLocation);
 * }</pre>
 *
 * <h2>Document type</h2>
 * <p>
 * The analyzer produces records whose document is
 * {@code Map<String, List<ValueSet>>}: every facet keeps its full
 * {@link eu.clarin.cmdi.vlo.importer.processor.ValueSet} rather than just the
 * string value, so the caller can still see the VTD index a value came from
 * and whether it was derived or produced by a value mapping. VLO's own importer
 * discards that; an analysis tool generally needs it.
 * </p>
 *
 * <h2>Compatibility</h2>
 * <p>
 * {@link eu.clarin.cmdi.vlo.importer.api.CmdiRecordAnalyzer} is the only public
 * type in this package and is a published contract: additive changes are fine,
 * but changing an existing signature breaks embedders.
 * {@code CmdiRecordAnalyzerContractTest} in the test sources pins it; if it
 * stops compiling, the change is breaking.
 * </p>
 */
package eu.clarin.cmdi.vlo.importer.api;
