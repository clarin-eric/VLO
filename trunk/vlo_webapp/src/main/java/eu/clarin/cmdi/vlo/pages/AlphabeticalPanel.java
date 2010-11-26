package eu.clarin.cmdi.vlo.pages;

/**
 *
 * @author paucas
 */
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField.Count;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;

public class AlphabeticalPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public AlphabeticalPanel(String id, IDataProvider data, final SearchPageQuery query) {
        super(id);

        RepeatingView sortedDataView = new RepeatingView("sortedDataView");
        List<String> foundCharacters = new LinkedList<String>();
        Iterator<? extends Count> iter = data.iterator(0, data.size());
        while (iter.hasNext()) {
            Count count = iter.next();
            String countName = count.getName();
            Character startChar = countName.charAt(0);
            if (!foundCharacters.contains(startChar.toString())) {
                foundCharacters.add(startChar.toString());
                sortedDataView.add(new AnchorPanel(sortedDataView.newChildId(), startChar.toString()));
            }
            sortedDataView.add(new FacetLinkPanel(sortedDataView.newChildId(), new Model<Count>(count), query));
        }

        ListView<String> alphabeticalView = new ListView<String>("alphabeticalView", new Model((Serializable) foundCharacters)) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                final String character = item.getModelObject();
                ExternalLink link = new ExternalLink("refLink", "#" + character);
                link.add(new Label("character", character));
                item.add(link);
            }
        };
        add(alphabeticalView);
        add(sortedDataView);
    }
}
