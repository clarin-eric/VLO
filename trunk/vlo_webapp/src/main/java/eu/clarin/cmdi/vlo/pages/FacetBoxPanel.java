package eu.clarin.cmdi.vlo.pages;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class FacetBoxPanel extends Panel {
    
    private static final int MAX_NR_OF_FACET_VALUES = 5;
    private static final long serialVersionUID = 1L;
    private Label label;
    private FacetHeaderPanel facetHeaderPanel;
    private FacetModel facetModel;

    public FacetBoxPanel(String id, IModel<FacetField> model) {
        super(id, model);
        setOutputMarkupId(true);
    }

    @SuppressWarnings("serial")
    public FacetBoxPanel create(final SearchPageQuery query) {
        final FacetField facetField = (FacetField) getDefaultModelObject();
        facetModel = new FacetModel(facetField);
        facetModel.setSelectedValue(query.getSelectedValue(facetField));
        label = new Label("headerLabel", facetField.getName());
        add(label);
        facetHeaderPanel = new FacetHeaderPanel("facetHeaderPanel", new Model<FacetModel>(facetModel), query);
        if (facetModel.isSelected()) {
            add(facetHeaderPanel);
        } else {
            add(new WebMarkupContainer("facetHeaderPanel"));
        }
        List<Count> allValues = facetField.getValues();
        List<Count> values = allValues;
        final boolean showMore = allValues != null && allValues.size() > MAX_NR_OF_FACET_VALUES;
        if (showMore) {
            values = allValues.subList(0, MAX_NR_OF_FACET_VALUES);
        }
        ListView<Count> facetList = new ListView<Count>("facetList", values) {
            @Override
            protected void populateItem(ListItem<Count> item) {
                item.add(new FacetLinkPanel("facetLinks", item.getModel(), query));
            }

            @Override
            public boolean isVisible() {
                return !facetModel.isSelected();
            }
        };
        add(facetList);
        PageParameters pageParameters = query.getPageParameters();
        pageParameters.add(ShowAllFacetValuesPage.SELECTED_FACET_PARAM, facetField.getName());
        add(new BookmarkablePageLink("showMore", ShowAllFacetValuesPage.class, pageParameters) {

            public boolean isVisible() {
                return !facetModel.isSelected() && showMore;
            }

        });
        return this;
    }

    public void replaceHeader(boolean isSelected, String selectedValue) {
        facetModel.setSelectedValue(selectedValue);
        if (isSelected) {
            label.replaceWith(facetHeaderPanel);
        } else {
            facetHeaderPanel.replaceWith(label);
        }

    }

}
