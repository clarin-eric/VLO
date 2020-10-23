/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.TemporalCoverageRange;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporalCoverageRangeModel extends LoadableDetachableModel<TemporalCoverageRange> {

    private final IModel<QueryFacetsSelection> selectionModel;
    private final String TEMPORAL_COVERAGE_START;
    private final String TEMPORAL_COVERAGE_END;

    private final static Logger logger = LoggerFactory.getLogger(TemporalCoverageRangeModel.class);

    public TemporalCoverageRangeModel(IModel<QueryFacetsSelection> selectionModel, FieldNameService fieldNameService) {
        this.TEMPORAL_COVERAGE_START = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_START);
        this.TEMPORAL_COVERAGE_END = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_END);
        this.selectionModel = selectionModel;
    }

    @Override
    protected TemporalCoverageRange load() {
        final SolrDocumentService documentService = VloWicketApplication.get().getDocumentService();
        final Calendar calendar = new GregorianCalendar();

        final QueryFacetsSelection unfilteredSelection = getUnfilteredSelection(selectionModel.getObject());
        final List<SolrDocument> startDocs = documentService.getSortedDocuments(unfilteredSelection, TEMPORAL_COVERAGE_START, "asc", 0, 1);
        final int startYear = getBoundaryYearFromSelection(calendar, startDocs, TEMPORAL_COVERAGE_START)
                .orElse(Integer.MIN_VALUE);

        final List<SolrDocument> endDocs = documentService.getSortedDocuments(unfilteredSelection, TEMPORAL_COVERAGE_END, "desc", 0, 1);
        final int endYear = getBoundaryYearFromSelection(calendar, endDocs, TEMPORAL_COVERAGE_END)
                .orElse(Integer.MAX_VALUE);

        return new TemporalCoverageRange(startYear, endYear);
    }

    /**
     * Remove existing temporal coverage facet from selection if present
     *
     * @return
     */
    private QueryFacetsSelection getUnfilteredSelection(QueryFacetsSelection originalSelection) {
        final FieldNameService fieldNameService = VloWicketApplication.get().getFieldNameService();
        final String temporalCoverageField = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE);
        
        if (originalSelection.getFacets().contains(temporalCoverageField)) {
            final QueryFacetsSelection unfilteredSelection = originalSelection.copy();
            unfilteredSelection.getSelection().remove(temporalCoverageField);
            return unfilteredSelection;
        } else {
            return originalSelection;
        }
        
    }

    private Optional<Integer> getBoundaryYearFromSelection(Calendar calendar, List<SolrDocument> docs, String field) {
        if (docs.size() == 1) {
            final SolrDocument firstDoc = docs.get(0);
            if (firstDoc.containsKey(field)) {
                final Object value = firstDoc.getFieldValue(field);
                if (value instanceof ArrayList) {
                    calendar.setTime((Date) ((ArrayList) value).get(0));
                    return Optional.of(calendar.get(Calendar.YEAR));
                } else {
                    logger.error("Start value in {} is not an array as expected", field);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected void onDetach() {
        selectionModel.detach();
    }

}
