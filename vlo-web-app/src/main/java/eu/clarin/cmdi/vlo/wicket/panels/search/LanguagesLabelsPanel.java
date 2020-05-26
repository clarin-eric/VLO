/*
 * Copyright (C) 2014 CLARIN
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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.model.LanguageLabelModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import java.util.List;
import java.util.Optional;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.panel.GenericPanel;

/**
 *
 * @author Tariq
 */
public class LanguagesLabelsPanel extends GenericPanel<SolrDocument> {

    @SpringBean
    private FieldNameService fieldNameService;

    private static final int MAX_LABEL_LENGTH = 12;
    private static final int MAX_LANGUAGES = 5;

    private final IModel<ExpansionState> expansionStateModel;
    private final IModel<List<String>> languagesLabelsModel;
    private final IModel<List<String>> languagesListModel;
    private final IModel<SolrDocument> documentModel;

    private final Link showMoreLink;

    public LanguagesLabelsPanel(String id, IModel<SolrDocument> documentModel, IModel<ExpansionState> expansionStateModel) {
        super(id, documentModel);
        this.documentModel = documentModel;
        this.expansionStateModel = expansionStateModel;

        languagesLabelsModel = new LanguageLabelModel(documentModel, fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));
        languagesListModel = () -> {
            final List<String> fullList = languagesLabelsModel.getObject();
            final int size = fullList.size();
            final int numOfItems = (expansionStateModel.getObject().isExpanded()) ? size : Math.min(size, MAX_LANGUAGES);
            return fullList.subList(0, numOfItems);
        };

        add(createListView("languagesList"));

        showMoreLink = new ExpansionToggleLink("showMore");

        showMoreLink.add(new WebMarkupContainer("state").add(
                new AttributeModifier("class",
                        () -> expansionStateModel.getObject().isExpanded()
                        ? "fa fa-minus-square-o"
                        : "fa fa-plus-square-o")
        ));

        showMoreLink.add(BooleanVisibilityBehavior.visibleOnTrue(this::isShowMore));
        add(showMoreLink);
        
        // badge with "... (+n)" label in case there is more        
        final IModel<Integer> remainderCountModel = () -> languagesLabelsModel.getObject().size() - languagesListModel.getObject().size();
        add(new ExpansionToggleLink("showMoreBadge")
                .add(new Label("remainderCount", remainderCountModel))
                .add(BooleanVisibilityBehavior.visibleOnTrue(
                        () -> isShowMore() && !expansionStateModel.getObject().isExpanded())));
        setOutputMarkupId(true);
    }

    private ListView createListView(String id) {
        return new ListView<String>(id, languagesListModel) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final Link languageLink = new RecordPageLink("languageLink", documentModel);
                String label = item.getModel().getObject();
                if (label.length() > MAX_LABEL_LENGTH) {
                    label = label.substring(0, MAX_LABEL_LENGTH) + "..";
                }
                item.add(languageLink
                        .add(new Label("languageName", label))
                        .add(new AttributeModifier("title", item.getModel()))
                );
            }
        };
    }

    private boolean isShowMore() {
        return languagesLabelsModel.getObject().size() > MAX_LANGUAGES;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionStateModel.detach();
        languagesLabelsModel.detach();
    }

    private class ExpansionToggleLink extends IndicatingAjaxFallbackLink<Void> {

        public ExpansionToggleLink(String id) {
            super(id);
        }

        @Override
        public void onClick(Optional<AjaxRequestTarget> t) {
            // toggle the expansion state
            expansionStateModel.setObject(expansionStateModel.getObject().invert());

            t.ifPresent(target -> {
                target.add(LanguagesLabelsPanel.this);
            });
        }

    }

}
