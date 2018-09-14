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
package eu.clarin.cmdi.vlo.wicket.provider;

import com.google.common.collect.Iterators;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import java.util.Iterator;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrDocumentProviderAdapter implements IDataProvider<SolrDocument> {

    private final IDataProvider<SolrDocumentExpansionPair> expansionPairProvider;
    private final FieldNameService fieldNameService;

    public SolrDocumentProviderAdapter(IDataProvider<SolrDocumentExpansionPair> expansionPairProvider, FieldNameService fieldNameService) {
        this.expansionPairProvider = expansionPairProvider;
        this.fieldNameService = fieldNameService;
    }

    @Override
    public Iterator<? extends SolrDocument> iterator(long first, long count) {
        return Iterators.transform(expansionPairProvider.iterator(first, count), SolrDocumentExpansionPair::getDocument);
    }

    @Override
    public long size() {
        return expansionPairProvider.size();
    }

    @Override
    public IModel<SolrDocument> model(SolrDocument object) {
        return new SolrDocumentModel(object, fieldNameService);
    }

    @Override
    public void detach() {
        expansionPairProvider.detach();
    }

}
