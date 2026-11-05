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
 * API for running a CMDI record through VLO's facet mapping and inspecting the
 * result, without running a VLO import.
 *
 * <h2>Typical use</h2>
 * <pre>{@code
 * // once per run
 * CmdiRecordAnalyzer<MyDocument> analyzer = new CmdiRecordAnalyzer<>(vloConfig, myProcessingStrategy);
 *
 * // per record
 * Optional<CMDIData<MyDocument>> record = analyzer.analyze(file);
 *
 * // per profile, e.g. for a coverage report
 * FacetsMapping mapping = analyzer.getFacetsMapping(schemaLocation);
 * }</pre>
 *
 * <h2>Customization</h2>
 * <p>
 * The {@link eu.clarin.cmdi.vlo.importer.api.ProcessingStrategy} supplies the
 * document type, the concept links, the extra facets and the record processor.
 * </p>
 *
 * <h2>Compatibility</h2>
 * <p>
 * The public types here are a published contract, pinned by
 * {@code CmdiRecordAnalyzerContractTest}: if it stops compiling, the change is
 * breaking.
 * </p>
 */
package eu.clarin.cmdi.vlo.importer.api;
