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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.components.NamedRecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HierarchyPanel extends GenericPanel<SolrDocument> {

    @SpringBean
    private SolrDocumentService documentService;

    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;

    public HierarchyPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        add(createParentLink("parent"));
        add(createTree("tree"));
    }

    private MarkupContainer createParentLink(String id) {
        final SolrFieldStringModel parentIdModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_IS_PART_OF);
        final SolrDocumentModel parentModel = new SolrDocumentModel(parentIdModel);

        final MarkupContainer parent = new WebMarkupContainer(id){
            
            @Override
            protected void onConfigure() {
                setVisible(parentModel.getObject() != null);
            }
        };
        parent.add(new NamedRecordPageLink("link", parentModel));

        return parent;
    }

    private AbstractTree createTree(String id) {
        return new DefaultNestedTree<SolrDocument>(id, createProvider()) {

            @Override
            protected Component newContentComponent(String id, final IModel<SolrDocument> node) {
                return new NamedRecordPageLink(id, node);
            }

        };
    }

    private ITreeProvider createProvider() {
        return new SortableTreeProvider<SolrDocument, Object>() {

            @Override
            public Iterator<? extends SolrDocument> getRoots() {
                return Iterators.singletonIterator(HierarchyPanel.this.getModel().getObject());
            }

            @Override
            public boolean hasChildren(SolrDocument node) {
                Object partCount = node.getFieldValue(FacetConstants.FIELD_HAS_PART_COUNT);
                return (partCount instanceof Number) && ((Number) partCount).intValue() > 0;
            }

            @Override
            public Iterator<? extends SolrDocument> getChildren(SolrDocument node) {
                final Collection<Object> parts = node.getFieldValues(FacetConstants.FIELD_HAS_PART);
                return Iterators.transform(parts.iterator(), new Function<Object, SolrDocument>() {

                    @Override
                    public SolrDocument apply(Object input) {
                        String childId = input.toString();
                        return documentService.getDocument(childId);
                    }
                });
            }

            @Override
            public IModel<SolrDocument> model(SolrDocument object) {
                return new SolrDocumentModel(object);
            }
        };
    }

}
