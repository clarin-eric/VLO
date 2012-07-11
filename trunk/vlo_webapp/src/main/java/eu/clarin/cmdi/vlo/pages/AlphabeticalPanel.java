package eu.clarin.cmdi.vlo.pages;

/**
 *
 * @author paucas
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
    private final List<String> foundCharacters = new LinkedList<String>();
    private final Integer facetMinOccurs;

    public AlphabeticalPanel(String id, IDataProvider data, final SearchPageQuery query, Integer facetMinOccurs) {
        super(id);
        this.facetMinOccurs = facetMinOccurs;

        RepeatingView sortedDataViewLeft = new RepeatingView("sortedDataViewLeft");
        RepeatingView sortedDataViewRight = new RepeatingView("sortedDataViewRight");
        TreeMap<Character, List<Count>> dataMap = transposeToMap(data);
        int numberOfKeys = 0;
        for (Entry entry : dataMap.entrySet()) {
            if (numberOfKeys < dataMap.size() / 2) {
                sortedDataViewLeft.add(categoryContainer(sortedDataViewLeft, entry, query));
                numberOfKeys++;
            } else {
                sortedDataViewRight.add(categoryContainer(sortedDataViewRight, entry, query));
            }
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
        add(sortedDataViewLeft);
        add(sortedDataViewRight);
    }

    private WebMarkupContainer categoryContainer(RepeatingView parentView, Entry entry, SearchPageQuery query) {
        WebMarkupContainer container = new WebMarkupContainer(parentView.newChildId());
        Character charSet = (Character) entry.getKey();
        if (Character.isLetter(charSet)) {
            container.add(new AnchorPanel("anchorPanel", charSet.toString()));
            foundCharacters.add(charSet.toString());
        } else if (Character.isDigit(charSet) & !foundCharacters.contains("0...9")) {
            container.add(new AnchorPanel("anchorPanel", "0...9"));
            foundCharacters.add("0...9");
        } else if (!Character.isLetterOrDigit(charSet) & !foundCharacters.contains("Other")) {
            container.add(new AnchorPanel("anchorPanel", "Other"));
            foundCharacters.add("Other");
        }
        RepeatingView charView = new RepeatingView("valueList");
        List<Count> countList = (List<Count>) entry.getValue();
        for (Count c : countList) {
            charView.add(new FacetLinkPanel(charView.newChildId(), new Model<Count>(c), query));
            container.add(charView);
        }
        return container;
    }

    /**
     * 
     * @param data
     * @return
     */
    private TreeMap<Character, List<Count>> transposeToMap(IDataProvider data) {
        TreeMap<Character, List<Count>> dataMap = new TreeMap<Character, List<Count>>(new CharGroupComparator());
        Iterator<? extends Count> iter = data.iterator(0, data.size());
        List<Count> numericalCount = new ArrayList<Count>();
        List<Count> otherCount = new ArrayList<Count>();
        while (iter.hasNext()) {
            Count count = iter.next();
            if(count.getCount() < facetMinOccurs)
            	continue;
            Character key = count.getName().trim().charAt(0);
            if (Character.isDigit(key)) {
                numericalCount.add(count);
            } else if (!Character.isLetterOrDigit(key)) {
                otherCount.add(count);
            } else if (!dataMap.containsKey(key)) {
                List<Count> countList = new ArrayList<Count>();
                countList.add(count);
                dataMap.put(Character.toUpperCase(key), countList);
            } else {
                dataMap.get(key).add(count);
            }
        }
        if (!numericalCount.isEmpty()) {
            dataMap.put('0', numericalCount);
        }
        if (!otherCount.isEmpty()) {
            dataMap.put('!', otherCount);
        }
        return dataMap;
    }

    /**
     *
     */
    private class CharGroupComparator implements Comparator<Character> {

        @Override
        public int compare(Character a, Character b) {
            if (Character.isLetter(a) & !Character.isLetter(b)) {
                return -1;
            } else if (Character.isLetter(a) && Character.isLetter(b)) {
                return a.toString().compareToIgnoreCase(b.toString());
            } else if (Character.isDigit(a) & !Character.isLetterOrDigit(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
