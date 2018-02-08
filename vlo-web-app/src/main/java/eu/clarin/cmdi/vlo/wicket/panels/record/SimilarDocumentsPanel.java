/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.panels.record;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.solr.SimilarDocumentsService;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import java.util.Collections;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author twagoo
 */
public class SimilarDocumentsPanel extends GenericPanel<SolrDocument> {
    @SpringBean
    private FieldNameService fieldNameService;

    private static final int MAX_TITLE_LENGTH = 150;
    private static final int LONG_TITLE_TRUNCATE_POINT = 130;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int LONG_DESCRIPTION_TRUNCATE_POINT = 180;

    private final static Logger logger = LoggerFactory.getLogger(SimilarDocumentsPanel.class);

    @SpringBean
    private SimilarDocumentsService similarDocumentsService;

    private final IModel<List<SolrDocument>> similarDocumentsModel;

    public SimilarDocumentsPanel(String id, final IModel<SolrDocument> model) {
        super(id, model);
        similarDocumentsModel = new LoadableDetachableModel<List<SolrDocument>>() {
            @Override
            public List<SolrDocument> load() {
                final Object docId = model.getObject().getFieldValue(fieldNameService.getFieldName(FieldKey.ID));
                if (docId instanceof String) {
                    return similarDocumentsService.getDocuments((String) docId);
                } else {
                    logger.warn("No (usable) document id for document, could not query for similar documents");
                    return Collections.emptyList();
                }
            }
        };

        add(new ListView<SolrDocument>("document", similarDocumentsModel) {
            @Override
            protected void populateItem(ListItem<SolrDocument> item) {
                item
                        .add(new RecordPageLink("record", item.getModel())
                                .add(new SolrFieldLabel("name", item.getModel(), fieldNameService.getFieldName(FieldKey.NAME), "Unnamed record", MAX_TITLE_LENGTH, LONG_TITLE_TRUNCATE_POINT, true))
                        )
                        .add(new SolrFieldLabel("description", item.getModel(), fieldNameService.getFieldName(FieldKey.DESCRIPTION), "No description", MAX_DESCRIPTION_LENGTH, LONG_DESCRIPTION_TRUNCATE_POINT, true));
            }
        });
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(!similarDocumentsModel.getObject().isEmpty());
    }

}
