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
        int startYear = Integer.MIN_VALUE;
        int endYear = Integer.MAX_VALUE;
        Calendar calendar = new GregorianCalendar();
        final SolrDocumentService documentService = VloWicketApplication.get().getDocumentService();

        final List<SolrDocument> startDocs = documentService.getSortedDocuments(selectionModel.getObject(), TEMPORAL_COVERAGE_START, "asc", 0, 1);
        if (startDocs.size() == 1) {
            final SolrDocument firstDoc = startDocs.get(0);
            if (firstDoc.containsKey(TEMPORAL_COVERAGE_START)) {
                final Object startValue = firstDoc.getFieldValue(TEMPORAL_COVERAGE_START);
                if (startValue instanceof ArrayList) {
                    calendar.setTime((Date) ((ArrayList) startValue).get(0));
                    startYear = calendar.get(Calendar.YEAR);
                } else {
                    logger.error("Start value in {} is not an array as expected", TEMPORAL_COVERAGE_START);
                }
            }
        }

        final List<SolrDocument> endDocs = documentService.getSortedDocuments(selectionModel.getObject(), TEMPORAL_COVERAGE_END, "desc", 0, 1);
        if (startDocs.size() == 1) {
            final SolrDocument firstDoc = endDocs.get(0);
            if (firstDoc.containsKey(TEMPORAL_COVERAGE_START)) {
                final Object endValue = firstDoc.getFieldValue(TEMPORAL_COVERAGE_END);
                if (endValue instanceof ArrayList) {
                    calendar.setTime((Date) ((ArrayList) endValue).get(0));
                    endYear = calendar.get(Calendar.YEAR);
                } else {
                    logger.error("End value in {} is not an array as expected", TEMPORAL_COVERAGE_END);
                    return null;
                }
            }
        }
        return new TemporalCoverageRange(startYear, endYear);

    }

    @Override
    protected void onDetach() {
        selectionModel.detach();
    }

}
