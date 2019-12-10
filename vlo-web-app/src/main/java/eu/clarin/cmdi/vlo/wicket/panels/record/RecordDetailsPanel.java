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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;

import java.util.List;
import java.util.Optional;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows the "basic" (non-technical) property fields of a document
 * through a {@link FieldsTablePanel} with a {@link FieldFilter}; for resources
 * with a single resource, some information about this single resource is also
 * shown.
 *
 * @author twagoo
 */
public abstract class RecordDetailsPanel extends GenericPanel<SolrDocument> {

    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    private final SolrFieldModel<String> resourcesModel;
    private final ResourceInfoModel resourceInfoModel;

    public RecordDetailsPanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        resourcesModel = new SolrFieldModel<>(model, fieldNameService.getFieldName(FieldKey.RESOURCE));
        resourceInfoModel = new ResourceInfoModel(resourceStringConverter, new SolrFieldStringModel(model, fieldNameService.getFieldName(FieldKey.RESOURCE)));

        // Fields table
        add(new FieldsTablePanel("fieldsTable", new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder))
                .add(new HighlightSearchTermBehavior())
        );

        // side panel for 'core links' (landing page and/or single resource)
        add(new RecordDetailsResourceInfoPanel("coreLinks", model) {
            @Override
            protected void switchToTab(String tab, Optional<AjaxRequestTarget> target) {
                RecordDetailsPanel.this.switchToTab(tab, target);
            }
        });
        
        // panel with similar documents (as provided by Solr index)
        add(new SimilarDocumentsPanel("similar", getModel()));
    }

    protected abstract void switchToTab(String tab, Optional<AjaxRequestTarget> target);

    @Override
    public void detachModels() {
        super.detachModels();
        resourcesModel.detach();
        resourceInfoModel.detach();
    }

}
