package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 *
 * @author paucas
 */
public class PreviousNextPagingNavigator extends PagingNavigator {

    private ShowResultPage parent;
    private SearchPageQuery query;

    public PreviousNextPagingNavigator(final String id, final IPageable pageable, ShowResultPage parent, SearchPageQuery query) {
        this(id, pageable, null, parent, query);
    }

    public PreviousNextPagingNavigator(final String id, final IPageable pageable,
            final IPagingLabelProvider labelProvider, ShowResultPage parent, SearchPageQuery query) {

        super(id, pageable, labelProvider);
        this.parent = parent;
        this.query = query;
    }

    /**
     * This method sets the "first" and "last" link to invisible
     */
    @Override
    protected Link newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        Link link = (Link) super.newPagingNavigationLink(id, pageable, pageNumber);
        link.setVisible(false);
        return link;
    }

    /**
     * This method changes the behavior of the "previous" en "next" links. Instead of presenting the next
     * and previous item of the Dataprovider, it sends its data to ShowResultPage where it is processed
     *
     * @param id
     * @param pageable
     * @param increment
     * @return
     */
    @Override
    protected Link newPagingNavigationIncrementLink(String id, IPageable pageable,
            int increment) {
        PagingNavigationIncrementLink link = new PagingNavigationIncrementLink(id, pageable, increment) {

            @Override
            public void onClick() {
                //refresh navigation bar to new state
                pageable.setCurrentPage(getPageNumber());
                //send new data to ShowResultPage
                parent.setCurrentPage(getPageNumber());


//                SolrDocumentDataProvider dataProvider = new SolrDocumentDataProvider(query.getSolrQuery().getCopy());
//                Iterator it = dataProvider.iterator(getPageNumber(), 1);
//                if (it.hasNext()) {
//                    SolrDocument doc = (SolrDocument) it.next();
//                    PageParameters pageParameters = query.getPageParameters();
//                    pageParameters.put(ShowResultPage.PARAM_DOC_ID, WicketURLEncoder.QUERY_INSTANCE.encode(doc.getFieldValue("id").toString()));
//                    parent.setCurrentPage(getPageNumber(), pageParameters);
//                }
            }
        };

        return link;
    }

    /**
     * This method sets the numerical navigation links to invisible
     *
     *
     * @param pageable
     * @param labelProvider
     * @return
     */
    @Override
    protected PagingNavigation newNavigation(final IPageable pageable,
            final IPagingLabelProvider labelProvider) {
        PagingNavigation pagNav = new PagingNavigation(NAVIGATION_ID, pageable, labelProvider);
        pagNav.setVisible(false);
        return pagNav;
    }
}
