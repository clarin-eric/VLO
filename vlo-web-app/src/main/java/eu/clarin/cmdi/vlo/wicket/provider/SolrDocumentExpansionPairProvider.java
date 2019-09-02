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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionList;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentExpansionPairImpl;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentExpansionPairModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.vlo.VloWebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.LoggerFactory;

import com.sun.istack.logging.Logger;

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import eu.clarin.cmdi.vlo.config.ServletVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;

/**
 *
 * @author twagoo
 */
public class SolrDocumentExpansionPairProvider implements IDataProvider<SolrDocumentExpansionPair> {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SolrDocumentExpansionPairProvider.class);

    private final FieldNameService fieldNameService;

    private final IModel<QueryFacetsSelection> selectionModel;

    private Long size;

    public SolrDocumentExpansionPairProvider(IModel<QueryFacetsSelection> selection,
            FieldNameService fieldNameService) {
        this.selectionModel = selection;
        this.fieldNameService = fieldNameService;
    }

    @Override
    public Iterator<? extends SolrDocumentExpansionPair> iterator(long first, long count) {
        final SolrDocumentExpansionList documents = getDocumentService().getDocumentsWithExpansion(
                selectionModel.getObject(), BigDecimal.valueOf(first).intValueExact(), // safe long->int conversion
                BigDecimal.valueOf(count).intValueExact(), FacetConstants.COLLAPSE_FIELD_NAME); // safe long->int
                                                                                                // conversion

        VloConfig vloConfig = VloWicketApplication.get().getVloConfig();
        if (vloConfig.isVloExposureEnabled()) {
            try {
                // these values to be saved in postgresql to calculate record-exposure
                // get page url
                String pageUrl = ((ServletWebRequest) RequestCycle.get().getRequest()).getContainerRequest()
                        .getRequestURL().toString();
                // get user ip address
                String ip = ((WebClientInfo) VloWebSession.get().getClientInfo()).getProperties().getRemoteAddress();
                QueryFacetsSelection selection = this.selectionModel.getObject();
                // get search term
                String searchTerm = this.selectionModel.getObject().getQuery();
                List<SearchResult> res = new ArrayList<SearchResult>();
                // get search results record ids
                for (int i = 0; i < documents.getDocuments().size(); i++) {
                    String id = ((SolrDocumentExpansionPairImpl) documents.getDocuments().get(i)).getDocument()
                            .get("id").toString();
                    res.add(new SearchResult(id, i + 1, 0));
                }
                // create SearchQuery object and save it to DB
                SearchQuery sq = new SearchQuery(searchTerm, selection.getSelection().toString(), res, ip, pageUrl);
                sq.save(vloConfig);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        return documents.iterator();
    }

    @Override
    public long size() {
        if (size == null) {
            size = getDocumentService().getResultCount(selectionModel.getObject());
        }
        return size;
    }

    @Override
    public IModel<SolrDocumentExpansionPair> model(SolrDocumentExpansionPair object) {
        return new SolrDocumentExpansionPairModel(object, selectionModel, fieldNameService,
                FacetConstants.COLLAPSE_FIELD_NAME);
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
