/*
 * Copyright (C) 2015 CLARIN
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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.Collection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HierarchyPanel extends GenericPanel<SolrDocument> {

    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;

    public HierarchyPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        add(createParentLink("parent"));
        add(new SolrFieldLabel("this", documentModel, FacetConstants.FIELD_NAME, new StringResourceModel("recordpage.unnamedrecord", this, null)));
        add(createChildrenLinks("child"));
    }

    private MarkupContainer createParentLink(String id) {
        final SolrFieldStringModel parentIdModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_IS_PART_OF);
        final SolrDocumentModel parentModel = new SolrDocumentModel(parentIdModel);

        final Link parentLink = new Link("link") {

            @Override
            public void onClick() {
                setResponsePage(RecordPage.class, documentParamConverter.toParameters(parentModel.getObject()));
            }

            @Override
            protected void onConfigure() {
                setVisible(parentModel.getObject() != null);
            }

        };
        parentLink.add(new SolrFieldLabel("name", parentModel, FacetConstants.FIELD_NAME, new StringResourceModel("recordpage.unnamedrecord", this, null)));

        final WebMarkupContainer noParentLabel = new WebMarkupContainer("noparent") {

            @Override
            protected void onConfigure() {
                setVisible(parentModel.getObject() == null);
            }

        };

        final MarkupContainer parent = new WebMarkupContainer(id);
        parent.add(parentLink);
        parent.add(noParentLabel);

        return parent;
    }

    private Component createChildrenLinks(String id) {
        final IModel<Collection<String>> partIdsModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_HAS_PART);
        //TODO: page or limit (collections can be huge!)
        return new ListView<String>(id, CollectionListModel.of(partIdsModel)) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final SolrDocumentModel childModel = new SolrDocumentModel(item.getModel());
                final Link childLink = new Link("link") {

                    @Override
                    public void onClick() {
                        setResponsePage(RecordPage.class, documentParamConverter.toParameters(childModel.getObject()));
                    }
                };
                childLink.add(new SolrFieldLabel("name", childModel, FacetConstants.FIELD_NAME, new StringResourceModel("recordpage.unnamedrecord", this, null)));

                item.add(childLink);
            }
        };
    }

}
