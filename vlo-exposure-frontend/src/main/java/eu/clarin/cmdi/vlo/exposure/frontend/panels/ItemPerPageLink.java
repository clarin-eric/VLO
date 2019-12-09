package eu.clarin.cmdi.vlo.exposure.frontend.panels;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.data.DataView;

public class ItemPerPageLink<T> extends Link<T> {

    private final int itemsPerPage;
    private final DataView<?> dataView;
    private final WebMarkupContainer pagingLinksContainer;

    public ItemPerPageLink(final String id, final DataView<?> dataView, WebMarkupContainer pagingLinksContainer, int itemsPerPageValue) {
        super(id);
        this.dataView = dataView;
        this.pagingLinksContainer = pagingLinksContainer;
        this.itemsPerPage = itemsPerPageValue;
        setEnabled(itemsPerPageValue != dataView.getItemsPerPage());

    }

    @Override
    public void onClick() {
        dataView.setItemsPerPage(itemsPerPage);
        pagingLinksContainer.setVisible(dataView.getPageCount() > 1);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("title", itemsPerPage);
    }

}