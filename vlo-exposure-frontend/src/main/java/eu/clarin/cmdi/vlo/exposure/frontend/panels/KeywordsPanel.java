package eu.clarin.cmdi.vlo.exposure.frontend.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class KeywordsPanel extends Panel {

    private static final String PANEL_ID = "KEYWORDS_PANEL";
    private static final String KEYWORD_ID = "KEYWORD";
    private static final String FREQUENCY_ID = "FREQ";

    private final static Logger logger = LoggerFactory.getLogger(KeywordsPanel.class);

    public KeywordsPanel(String id, HashMap<String,Integer> KeyWords) {
        super(id);
        createPanel(KeyWords);
    }

    private void createPanel(HashMap<String, Integer> keyWords){
        try {
            add(new ListView<>(PANEL_ID,new ArrayList<>(keyWords.keySet())){
                @Override
                protected void populateItem(ListItem<String> item) {
                    String keyword = item.getModelObject();
                    Integer freq = keyWords.get(keyword);
                    item.add(new Label(KEYWORD_ID,keyword));
                    item.add(new Label(FREQUENCY_ID, freq));
                }
            });
        }catch(Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
