/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentExpansionPairModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import static java.lang.Math.toIntExact;
import java.util.Collections;
import java.util.Iterator;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class DuplicateSearchResultItemsPanel extends GenericPanel<SolrDocumentExpansionPair> {

    private final static Logger LOG = LoggerFactory.getLogger(DuplicateSearchResultItemsPanel.class);

    private static final int ITEMS_PER_PAGE = 10;

    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SearchContext> selectionModel;

    public DuplicateSearchResultItemsPanel(String id, SolrDocumentExpansionPairModel documentExpansionPairModel, IModel<SearchContext> selectionModel, IModel<Boolean> expandedModel) {
        super(id, documentExpansionPairModel);
        this.selectionModel = selectionModel;
        
        add(new Label("expansionCount", new PropertyModel<>(documentExpansionPairModel, "expansionCount")));

        add(new IndicatingAjaxFallbackLink("expandDuplicates") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                expandedModel.setObject(true);
                if (target != null) {
                    target.add(DuplicateSearchResultItemsPanel.this);
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!expandedModel.getObject());
            }

        });

        final DataView<SolrDocument> duplicatesView = new DataView<SolrDocument>("duplicateItem", new DuplicateDocumentsProvider(documentExpansionPairModel, fieldNameService), ITEMS_PER_PAGE) {
            @Override
            protected void populateItem(Item<SolrDocument> item) {
                item.add(
                        new RecordPageLink("duplicateItemLink", item.getModel(), selectionModel)
                                .add(new Label("duplicateItemName", new SolrFieldStringModel(item.getModel(), fieldNameService.getFieldName(FieldKey.NAME), true))));
            }

        };

        add(new WebMarkupContainer("duplicatesView")
                .add(duplicatesView)
                .add(new BootstrapAjaxPagingNavigator("duplicatesPaging", duplicatesView)
                        .add(new Behavior() {
                            @Override
                            public void onConfigure(Component component) {
                                component.setVisible(duplicatesView.getPageCount() > 1);
                            }

                        })
                )
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(expandedModel.getObject());
                    }
                })
                .setOutputMarkupId(true)
        ).add(new Behavior() {
            @Override
            public void onConfigure(Component duplicateResultsView) {
                duplicateResultsView.setVisible(documentExpansionPairModel.getObject().getExpansionCount() > 0);
            }
        });
        setOutputMarkupId(true);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        selectionModel.detach();
    }

    private static class DuplicateDocumentsProvider implements IDataProvider<SolrDocument> {

        private final SolrDocumentExpansionPairModel expansionPairModel;
        private final FieldNameService fieldNameService;

        public DuplicateDocumentsProvider(SolrDocumentExpansionPairModel targetDocument, FieldNameService fieldNameService) {
            this.expansionPairModel = targetDocument;
            this.fieldNameService = fieldNameService;
        }

        @Override
        public Iterator<? extends SolrDocument> iterator(long first, long count) {
            try {
                expansionPairModel.setExpansionPage(toIntExact(first), toIntExact(count));
            } catch (ArithmeticException ex) {
                LOG.error("Failed long to int coversion on paging: first {}, count {}", first, count, ex);
            }
            return expansionPairModel.getObject()
                    .getExpansionDocuments()
                    .map(l -> l.iterator())
                    .orElseGet(Collections::emptyIterator);
        }

        @Override
        public long size() {
            return expansionPairModel.getObject().getExpansionCount();
        }

        @Override
        public IModel<SolrDocument> model(SolrDocument object) {
            return new SolrDocumentModel(object, fieldNameService);
        }

        @Override
        public void detach() {
            expansionPairModel.detach();
        }

    }

}
