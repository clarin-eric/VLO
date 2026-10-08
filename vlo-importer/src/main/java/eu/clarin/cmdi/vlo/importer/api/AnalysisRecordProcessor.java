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
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.util.List;
import java.util.Map;

class AnalysisRecordProcessor extends CMDIRecordProcessor<Map<String, List<ValueSet>>> {

    AnalysisRecordProcessor(CMDIDataProcessor<Map<String, List<ValueSet>>> dataProcessor,
            FieldNameService fieldNameService) {
        super(dataProcessor, fieldNameService);
    }

    @Override
    protected boolean skipOnDuplicateId() {
        return false;
    }

    @Override
    protected boolean skipOnNoResources() {
        return false;
    }
}
