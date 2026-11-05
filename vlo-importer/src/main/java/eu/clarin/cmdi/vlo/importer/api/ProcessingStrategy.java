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

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.mapping.ConceptLinkPathMapper;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import java.util.Map;

/**
 * What a caller of {@link CmdiRecordAnalyzer} decides about the analysis.
 *
 * @param <T> document type of the analysed records
 */
public interface ProcessingStrategy<T> {

    /**
     * @param fieldNames VLO's field names
     * @return the factory for the objects that hold an analysed record
     */
    CMDIDataFactory<T> dataFactory(FieldNameService fieldNames);

    /**
     * @param schemaLocation URL of the profile schema
     * @return the mapper that supplies the concept links of that profile
     */
    ConceptLinkPathMapper conceptLinkPathMapper(String schemaLocation);

    /**
     * @return facets to extract in addition to VLO's, as facet name to XPath;
     * at least one
     */
    Map<String, String> extraFacets();

    /**
     * @param parser VLO's parser, to hand to the processor
     * @param fieldNames VLO's field names
     * @return the processor that handles each analysed record
     */
    CMDIRecordProcessor<T> recordProcessor(CMDIDataProcessor<T> parser, FieldNameService fieldNames);
}
