package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloPageParameters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FacetBoxPanel extends BasePanel {
    private static final Set<String> IGNORABLE_VALUES = new HashSet<String>();
    static {
        IGNORABLE_VALUES.add("unknown");
        IGNORABLE_VALUES.add("unspecified");
    }
    private static final int MAX_NR_OF_FACET_VALUES = 10;
    private static final long serialVersionUID = 1L;
    private Label label;
    private FacetHeaderPanel facetHeaderPanel;
    private FacetModel facetModel;
    private int maxNrOfFacetValues;
    
    public FacetBoxPanel(String id, IModel<FacetField> model, String tooltipText) {
        super(id, model);
        setOutputMarkupId(true);
        setMaxNrOfFacetValues(MAX_NR_OF_FACET_VALUES);
        add(new AttributeModifier("title", tooltipText));
        
    }
     
    @SuppressWarnings({"serial"})
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
        final boolean showMore = facetField.getValueCount() > maxNrOfFacetValues + 1;
        List<Count> values = getFacetListForBox(facetField, showMore);
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
        VloPageParameters facetParameters = new VloPageParameters ();
           
        PageParameters extraParameters = new PageParameters ();
        extraParameters.add(ShowAllFacetValuesPage.SELECTED_FACET_PARAM, facetField.getName());
        extraParameters.add(ShowAllFacetValuesPage.FACET_MIN_OCCURS, "1");
        //TODO: merge again once implemented
        facetParameters.merge(extraParameters);

        add(new BookmarkablePageLink("showMore", ShowAllFacetValuesPage.class, extraParameters) {
            @Override
            public boolean isVisible() {
                return !facetModel.isSelected() && showMore;
            }
        });
        return this;
    }

    List<Count> getFacetListForBox(final FacetField facetField, final boolean showMore) {
        List<Count> allValues = facetField.getValues();
        List<Count> values = new ArrayList<Count>();
        if (showMore) {
            if (facetField.getValueCount() == maxNrOfFacetValues || facetField.getValueCount() == maxNrOfFacetValues + 1) { //Show all values, the "more" link can be used as the extra facet
                values = allValues;
            } else {// make a sublist
                //IGNORABLE_VALUES (like "unknown") are move to the back of the list and should only be shown when you click "more...", unless the list is too small then whe can just show them.
                List<Count> ignorables = new ArrayList<Count>();
                for (int i = 0; values.size() < maxNrOfFacetValues && i < allValues.size(); i++) {
                    Count count = allValues.get(i);
                    if (!IGNORABLE_VALUES.contains(count.getName().toLowerCase())) {
                        values.add(count);
                    } else {
                        ignorables.add(count);
                    }
                }
                int stillToAdd = maxNrOfFacetValues - values.size();
                for (int i = 0; i < stillToAdd && i < ignorables.size(); i++) {
                    values.add(ignorables.get(i));

                }
            }
        } else { //show all values
            values = allValues;
        }
        return values;
    }

    public void replaceHeader(boolean isSelected, String selectedValue) {
        facetModel.setSelectedValue(selectedValue);
        if (isSelected) {
            label.replaceWith(facetHeaderPanel);
        } else {
            facetHeaderPanel.replaceWith(label);
        }

    }

    void setMaxNrOfFacetValues(int maxNrOfFacetValues) {
        this.maxNrOfFacetValues = maxNrOfFacetValues;
    }

}
