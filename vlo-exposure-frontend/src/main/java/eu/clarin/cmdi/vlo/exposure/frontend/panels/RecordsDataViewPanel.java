package eu.clarin.cmdi.vlo.exposure.frontend.panels;


import eu.clarin.cmdi.vlo.exposure.models.Record;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.resource.ByteArrayResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RecordsDataViewPanel extends Panel {

    private static final String RECORD_ID = "RECORD_ID";
    private static final String FREQUENCY_ID = "FREQ";
    private static final String LINK_RECORD_PAGE_ID = "LINK_RECORD_PAGE";
    private static final String NAVIGATION_ID = "NAVIGATION";
    private static final String EXPORT_LINK_ID = "EXPORT";
    private String csvText;


    private final static Logger logger = LoggerFactory.getLogger(RecordsDataViewPanel.class);

    public RecordsDataViewPanel(String id, List<Record> records, String title, String[] tableHeaders)   {
        super(id);
        generateCSV(records, tableHeaders);
        createPanel(records, title, tableHeaders);
    }

    private void createPanel(List<Record> records, String title, String[] tableHeaders){
        try {
            add(new Label("PANEL_TITLE", title));
            add(new Label("HEADER_1", tableHeaders[0]));
            add(new Label("HEADER_2", tableHeaders[1]));
            add(new Label("HEADER_3", tableHeaders[2]));
            final DataView<Record> dv = new DataView<>("PAGE_VIEWS_PANEL", new ListDataProvider<>(records)) {
                @Override
                protected void populateItem(Item item) {
                    Record record = (Record) item.getModelObject();
                    item.add(new ExternalLink(RECORD_ID, record.getInternalUrl(), record.getRecordId()));
                    item.add(new ExternalLink(LINK_RECORD_PAGE_ID, record.getExternalUrl()));
                    item.add(new Label(FREQUENCY_ID, record.getFreq()));
                }
            };
            dv.setItemsPerPage(25);
            add(dv);
            add(new CustomPagingNavigator(NAVIGATION_ID, dv));
            add(new ResourceLink<>(EXPORT_LINK_ID, new ByteArrayResource("text/csv",csvText.getBytes(), "export.csv")));
        }catch(Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    private void generateCSV(List<Record> records, String[] tableHeaders){
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(String.join("\t", tableHeaders)).append("\n");
        for(Record record: records){
            stringBuilder.append(record.getExternalUrl()).append("\t");
            stringBuilder.append(record.getRecordId()).append("\t");
            stringBuilder.append(record.getFreq()).append("\n");
        }
        csvText = stringBuilder.toString();
    }
}

