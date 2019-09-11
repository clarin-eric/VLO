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
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.InvisibleIfNullBehaviour;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SingleValueSolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentExpansionPairModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.TruncatingStringModel;
import static java.lang.Math.toIntExact;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that optionally (on expansion) lists the 'near duplicate' items for a
 * search result
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class DuplicateSearchResultItemsPanel extends GenericPanel<SolrDocumentExpansionPair> {

    private final static Logger LOG = LoggerFactory.getLogger(DuplicateSearchResultItemsPanel.class);

    private static final int ITEMS_PER_PAGE = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 120;
    private static final int DESCRIPTION_TRUNCATE_POINT = 100;

    @SpringBean
    private FieldNameService fieldNameService;

    /**
     *
     * @param id component id
     * @param documentExpansionPairModel model of Solr document and its
     * expansion
     * @param expandedModel model for current 'duplicate items' expansion state
     */
    public DuplicateSearchResultItemsPanel(String id, SolrDocumentExpansionPairModel documentExpansionPairModel, IModel<QueryFacetsSelection> selectionModel, IModel<ExpansionState> expandedModel) {
        super(id, documentExpansionPairModel);

        final IModel<Boolean> isExpandedModel = new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return expandedModel.getObject() == ExpansionState.EXPANDED;
            }
        };

        //container with class that represents expansion state
        final WebMarkupContainer container = new WebMarkupContainer("duplicatesViewContainer");

        // header contains link to expand/collapse duplicates list
        final IndicatingAjaxFallbackLink toggleHeaderLink = new IndicatingAjaxFallbackLink<Void>("toggleExpansion") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                expandedModel.setObject(isExpandedModel.getObject() ? ExpansionState.COLLAPSED : ExpansionState.EXPANDED);
                target.ifPresent(t -> {
                    t.add(container);
                });
            }
        };

        //Model for "The search results include N record(s) with the same title
        final StringResourceModel duplicatesCountModel = new StringResourceModel("duplicateresults.count")
                .setParameters(new PropertyModel<>(documentExpansionPairModel, "expansionCount"));

        add(container
                .add(toggleHeaderLink.add(new Label("expansionCount", duplicatesCountModel)))
                .add(new AttributeAppender("class",
                        new BooleanOptionsModel<>(isExpandedModel,
                                Model.of("duplicates-expanded duplicates-was-expanded"),
                                Model.of("duplicates-collapsed")))));

        // view of documents list
        final DataView<SolrDocument> duplicatesView = new DataView<SolrDocument>("duplicateItem", new DuplicateDocumentsProvider(documentExpansionPairModel, fieldNameService), ITEMS_PER_PAGE) {
            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final IModel<String> descriptionModel = new TruncatingStringModel(
                        new SolrFieldStringModel(item.getModel(), fieldNameService.getFieldName(FieldKey.DESCRIPTION), false),
                        MAX_DESCRIPTION_LENGTH, DESCRIPTION_TRUNCATE_POINT);

                item
                        .add(new RecordPageLink("duplicateItemLink", item.getModel(), new SearchContextModel(selectionModel))
                                .add(new SingleValueSolrFieldLabel("duplicateItemName", item.getModel(), fieldNameService.getFieldName(FieldKey.NAME), new StringResourceModel("searchpage.unnamedrecord", this))))
                        .add(new Label("duplicateItemDescription", descriptionModel)
                                .add(new InvisibleIfNullBehaviour(descriptionModel)));
            }

        };

        // container for list view and pagination
        container.add(new WebMarkupContainer("duplicatesView")
                .add(duplicatesView)
                .add(new BootstrapAjaxPagingNavigator("duplicatesPaging", duplicatesView) // pagination for list view
                        .add(new Behavior() {
                            @Override
                            public void onConfigure(Component component) {
                                component.setVisible(duplicatesView.getPageCount() > 1);
                            }

                        })
                )
                .add(BooleanVisibilityBehavior.visibleOnTrue(isExpandedModel))
                .setOutputMarkupId(true) // container must be Ajax updateable
        );

        // component must be Ajax updateable (on expansion)
        container.setOutputMarkupId(true);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // show panel only if something to be shown
        setVisible(getModelObject().getExpansionCount() > 0);
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
