package eu.clarin.cmdi.vlo.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.dao.DaoLocator;

public class PrevNextHeaderPanel extends Panel {

    private static final long serialVersionUID = 1L;

    /**
     * Creates Prev/Next Header can take a while to generate
     * @param id
     * @param docId
     * @param query
     */
    public PrevNextHeaderPanel(String id, String docId, SearchPageQuery query) {
        super(id);
        addPrevNextLabels(docId, query);
    }

    /**
     * Constructor that creates dummy prev/next label, which can be used to load before the real labels are calculated.
     * @param id
     */
    public PrevNextHeaderPanel(String id) {
        super(id);
        addDummyPrev();
        addDummyResultCount();
        addDummyNext();
    }

    private void addDummyPrev() {
        add(new WebMarkupContainer("prev").setVisible(false));
    }

    private void addDummyNext() {
        add(new WebMarkupContainer("next").setVisible(false));
    }
    
    private void addDummyResultCount() {
    	add(new WebMarkupContainer("resultcount"));
    }

    private void addPrevNextLabels(String docId, SearchPageQuery query) {
        int index = -1;
        //Very inefficient query, possibly gets all the docId's (when no facets are selected) just to create the next/prev labels. 
        //Cannot see another way of doing this because the result page can be bookmarked and therefor the list needs to be refetched. 
        //If some facets are selected the list of id's is already a lot smaller and performance is not an issue. I lazy loaded the panel just to everything a bit more snappy.
        SolrDocumentList docIdList = DaoLocator.getSearchResultsDao().getDocIdList(query.getSolrQuery().getCopy());  

        for (int i = 0; i < docIdList.size(); i++) {
            SolrDocument doc = docIdList.get(i);
            if (doc.getFieldValue(FacetConstants.FIELD_ID).equals(docId)) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            String prevDocId = docIdList.get(index - 1).getFieldValue(FacetConstants.FIELD_ID).toString();
            BookmarkablePageLink<ShowResultPage> prev = ShowResultPage.createBookMarkableLink("prev", query, prevDocId);
            add(prev);
        } else {
            addDummyPrev();
        }
        
        add(new Label("resultcount", "Record "+(index+1)+" out of "+docIdList.size()));
        
        if (index < (docIdList.size() - 1) && index >= 0) {
            String prevDocId = docIdList.get(index + 1).getFieldValue(FacetConstants.FIELD_ID).toString();
            BookmarkablePageLink<ShowResultPage> next = ShowResultPage.createBookMarkableLink("next", query, prevDocId);
            add(next);
        } else {
            addDummyNext();
        }
    }

}
