/*
 * Copyright (C) 2026 CLARIN
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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.InvisibleIfNullBehaviour;
import eu.clarin.cmdi.vlo.wicket.components.SingleValueSolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * "Tombstone" presentation for a record that has been removed from the main index
 * but for which minimal information is still kept in the morgue index.
 * Reuses the record page's building blocks (record details, links, technical details)
 *
 * <p>
 * This panel is rendered in-place on the {@code RecordPage} (under the regular
 * record URL) when a requested record is found only in the morgue.</p>
 */
public class TombstonePanel extends GenericPanel<SolrDocument> {

    @SpringBean
    private FieldNameService fieldNameService;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;

    public TombstonePanel(String id, IModel<SolrDocument> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new BookmarkablePageLink<>("backToSearch", FacetedSearchPage.class));

        add(new SingleValueSolrFieldLabel("name", getModel(),
                fieldNameService.getFieldName(FieldKey.NAME), getString("tombstone.unnamedrecord")));

        // removal date shown inline in the notice banner
        final IModel<Date> removedDateModel = new SolrFieldModel<Date>(getModel(), fieldNameService.getFieldName(FieldKey.REMOVED_DATE))
                .map(values -> values.isEmpty() ? null : values.iterator().next());
        add(new Label("removedDate", removedDateModel).add(new InvisibleIfNullBehaviour<>(removedDateModel)));

        // The record page sections shown as tabs similar to the live record view.
        final List<ITab> tabs = new ArrayList<>();
        tabs.add(new AbstractTab(new StringResourceModel("tombstone.section.recordDetails", this)) {
            @Override
            public Panel getPanel(String panelId) {
                return fieldsTable(panelId, basicPropertiesFilter);
            }
        });
        tabs.add(new AbstractTab(new StringResourceModel("tombstone.section.links", this)) {
            @Override
            public Panel getPanel(String panelId) {
                return new ResourceLinksPanel(panelId, getModel()) {
                    @Override
                    protected void switchToTab(String tab, Optional<AjaxRequestTarget> target) {}
                };
            }
        });
        tabs.add(new AbstractTab(new StringResourceModel("tombstone.section.technicalDetails", this)) {
            @Override
            public Panel getPanel(String panelId) {
                return fieldsTable(panelId, technicalPropertiesFilter);
            }
        });
        add(new AjaxBootstrapTabbedPanel<>("tabs", tabs));
    }

    private FieldsTablePanel fieldsTable(String id, FieldFilter filter) {
        return new FieldsTablePanel(id, new DocumentFieldsProvider(getModel(), filter, fieldOrder)) {
            @Override
            protected boolean isShowFacetSelectLinks() {
                // no faceted-search links from a removed record
                return false;
            }
        };
    }

}
