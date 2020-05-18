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


public class TemporalCoverageRangeModel extends LoadableDetachableModel<TemporalCoverageRange> {

    private final IModel<QueryFacetsSelection> selectionModel;
    private final String TEMPORAL_COVERAGE_START;
    private final String TEMPORAL_COVERAGE_END;

    public TemporalCoverageRangeModel(IModel<QueryFacetsSelection> selectionModel, FieldNameService fieldNameService) {
        this.TEMPORAL_COVERAGE_START = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_START);
        this.TEMPORAL_COVERAGE_END = fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_END);
        this.selectionModel = selectionModel;
    }

    @Override
    protected TemporalCoverageRange load() {
        int startYear = 0;
        int endYear = 0;
        Calendar calendar = new GregorianCalendar();
        final SolrDocumentService documentService = VloWicketApplication.get().getDocumentService();

        // TODO: handle the exception
        List<SolrDocument>  lst = documentService.getSortedDocuments(selectionModel.getObject(), TEMPORAL_COVERAGE_START, "asc", 0, 1);
        if(lst.size() == 1){
            ArrayList<Object> start = (ArrayList<Object>)lst.get(0).getFieldValue(TEMPORAL_COVERAGE_START);
            calendar.setTime((Date) start.get(0));
            startYear = calendar.get(Calendar.YEAR);
        }

        List<SolrDocument>  lst2 = documentService.getSortedDocuments(selectionModel.getObject(), TEMPORAL_COVERAGE_END, "desc", 0, 1);
        if(lst.size() == 1){
            ArrayList<Object> end = (ArrayList<Object>)lst2.get(0).getFieldValue(TEMPORAL_COVERAGE_END);
            calendar.setTime((Date) end.get(0));
            endYear = calendar.get(Calendar.YEAR);
        }
        return new TemporalCoverageRange(startYear, endYear);

    }

    @Override
    protected void onDetach() {
        selectionModel.detach();
    }

}
