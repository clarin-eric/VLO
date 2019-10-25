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
package eu.clarin.cmdi.vlo.service.impl;



import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.VloWebSession;
import eu.clarin.cmdi.vlo.service.ExposureTracker;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionList;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentExpansionPairImpl;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentExpansionPairProvider;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;

import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExposureTrackerImpl implements ExposureTracker {

    private final static Logger logger = LoggerFactory.getLogger(ExposureTrackerImpl.class);
    private final VloConfig vloConfig;
    
    public ExposureTrackerImpl(VloConfig vloConfig) {
        this.vloConfig = vloConfig;
    }
    
    private String anonymizeIP(String ip) {
        int index = ip.length()-1;
        if(ip.contains("."))
            index = ip.lastIndexOf(".");
        else if(ip.contains(":"))
            index = ip.lastIndexOf(":");
        ip =ip.substring(0, index+1).concat("0");
        return ip;
    }
    
    @Override
    public void track(QueryFacetsSelection selection, SolrDocumentExpansionList documents, long first, long count) {
        if (vloConfig.isVloExposureEnabled()) {
            try {
                // these values to be saved in postgresql to calculate record-exposure
                // get page url
                String pageUrl = ((ServletWebRequest) RequestCycle.get().getRequest()).getContainerRequest()
                        .getRequestURL().toString();
                // get user ip address
                String ip = anonymizeIP(((WebClientInfo) VloWebSession.get().getClientInfo()).getProperties().getRemoteAddress());
                // get search term
                String searchTerm = selection.getQuery();
                int documentsSize = documents.getDocuments().size();
                List<SearchResult> res = new ArrayList<>(documentsSize);
                // get search results record ids
                for (int i = 0; i < documentsSize; i++) {
                    String id = ((SolrDocumentExpansionPairImpl) documents.getDocuments().get(i)).getDocument()
                            .get("id").toString();
                    long  page = first/count +1;
                    res.add(new SearchResult(id, first + i + 1, page));
                }
                // create SearchQuery object and save it to DB
                SearchQuery sq = new SearchQuery(searchTerm, selection.getSelection().toString(), res, ip, pageUrl);
                sq.save(vloConfig);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

}
