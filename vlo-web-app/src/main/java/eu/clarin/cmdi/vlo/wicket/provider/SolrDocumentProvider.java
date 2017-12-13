/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.provider;

import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class SolrDocumentProvider implements IDataProvider<SolrDocument> {

    private final FieldNameService fieldNameService;

    private final IModel<QueryFacetsSelection> selectionModel;

    private Long size;

    public SolrDocumentProvider(IModel<QueryFacetsSelection> selection, FieldNameService fieldNameService) {
        this.selectionModel = selection;
        this.fieldNameService = fieldNameService;
    }

    @Override
    public Iterator<? extends SolrDocument> iterator(long first, long count) {
        final List<SolrDocument> documents = getDocumentService().getDocuments(selectionModel.getObject(),
                BigDecimal.valueOf(first).intValueExact(), // safe long->int conversion
                BigDecimal.valueOf(count).intValueExact()); // safe long->int conversion
        return documents.iterator();
    }

    @Override
    public long size() {
        if (size == null) {
            size = getDocumentService().getDocumentCount(selectionModel.getObject());
        }
        return size;
    }

    @Override
    public IModel<SolrDocument> model(SolrDocument object) {
        return new SolrDocumentModel(object, fieldNameService);
    }

    @Override
    public void detach() {
        selectionModel.detach();
        size = null;
    }

    private SolrDocumentService getDocumentService() {
        return VloWicketApplication.get().getDocumentService();
    }
}
