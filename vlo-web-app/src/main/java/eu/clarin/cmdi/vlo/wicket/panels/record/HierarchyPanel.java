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
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.components.AjaxFallbackLinkLabel;
import eu.clarin.cmdi.vlo.wicket.components.IndicatingNestedTree;
import eu.clarin.cmdi.vlo.wicket.components.NamedRecordPageLink;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HierarchyPanel extends GenericPanel<SolrDocument> {

    /**
     * Number of parent nodes shown initially (before 'show all' expansion)
     */
    public static final int INITIAL_PARENTS_SHOWN = 5;
    public static final int INITIAL_CHILRDEN_SHOWN = 50;

    @SpringBean
    private SolrDocumentService documentService;

    private final IDataProvider<SolrDocument> parentsProvider;
    private final MarkupContainer treeContainer;
    private final AbstractTree<ModelWrapper<SolrDocument>> tree;
    private final IModel<SolrDocument> pageDocumentModel;

    public HierarchyPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);
        pageDocumentModel = documentModel;

        parentsProvider = new ParentsDataProvider();

        add(createParentLinks("parents"));

        tree = createTree("tree");

        treeContainer = new WebMarkupContainer("treeContainer");
        treeContainer.setOutputMarkupId(true);
        treeContainer.add(tree);
        add(treeContainer);

        setOutputMarkupId(true);
    }

    private Component createParentLinks(String id) {
        // data view of (first N) parents and a link to load all in an 
        // Ajax-updatable container
        final WebMarkupContainer container = new WebMarkupContainer(id);

        // actual view
        final DataView<SolrDocument> parentsView = new DataView<SolrDocument>("parentsList", parentsProvider, INITIAL_PARENTS_SHOWN) {

            @Override
            protected void populateItem(final Item<SolrDocument> item) {
                item.add(new NamedRecordPageLink("link", item.getModel()));
                item.add(new IndicatingAjaxFallbackLink("up") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // tree root up one level, expand root for traceability by user
                        HierarchyPanel.this.setModel(item.getModel());
                        tree.expand(new ModelWrapper<>(item.getModel()));
                        if (target != null) {
                            target.add(HierarchyPanel.this);
                        }
                    }
                });

                item.add(new Behavior() {

                    @Override
                    public void onConfigure(Component component) {
                        // null parent, hide
                        component.setVisible(item.getModelObject() != null);
                    }

                });
            }

            @Override
            protected void onConfigure() {
                // hide if there are no parents to show
                setVisible(parentsProvider.size() > 0);
            }
        };

        // ajax link to load the entire list, only shown if applicable
        final Link showAllLink = new IndicatingAjaxFallbackLink("showAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                parentsView.setItemsPerPage(parentsProvider.size());
                if (target != null) {
                    target.add(container);
                }
            }

            @Override
            protected void onConfigure() {
                // hide if there are no more parents to load
                setVisible(parentsView.getItemCount() > parentsView.getItemsPerPage());
            }
        };
        // show how many ndoes can be loaded
        showAllLink.add(new Label("itemCount", new PropertyModel<Integer>(parentsProvider, "size")));

        return container
                .add(parentsView)
                .add(showAllLink)
                .setOutputMarkupId(true);
    }

    private AbstractTree createTree(String id) {
        final HierarchyTreeProvider treeProvider = new HierarchyTreeProvider();
        final AbstractTree<ModelWrapper<SolrDocument>> result = new IndicatingNestedTree<ModelWrapper<SolrDocument>>(id, treeProvider) {

            @Override
            protected Component newContentComponent(String id, final IModel<ModelWrapper<SolrDocument>> node) {
                if (node.getObject().isLimit()) {
                    return new AjaxFallbackLinkLabel(id, node, Model.of("Show all... (" + node.getObject().getCount() + ")")) {

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            treeProvider.setChildrenShown(null);
                            target.add(treeContainer);
                        }

                    };
                } else {
                    return new NamedRecordPageLink(id, node.getObject().getModel()) {

                        @Override
                        protected void onConfigure() {
                            setEnabled(!node.equals(pageDocumentModel));
                        }
                    };
                }
            }

        };
        // style of tree depends on presence of parent nodes
        result.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                if (parentsProvider.size() > 0) {
                    return "has-parent";
                } else {
                    return null;
                }
            }
        }, " "));
        return result;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        pageDocumentModel.detach();
    }

    private class HierarchyTreeProvider extends SortableTreeProvider<ModelWrapper<SolrDocument>, Object> {

        private Integer childrenShown = INITIAL_CHILRDEN_SHOWN;

        public void setChildrenShown(Integer childrenShown) {
            this.childrenShown = childrenShown;
        }

        @Override
        public Iterator<? extends ModelWrapper<SolrDocument>> getRoots() {
            return Iterators.singletonIterator(
                    new ModelWrapper<>(
                            HierarchyPanel.this.getModel()));
        }

        @Override
        public boolean hasChildren(ModelWrapper<SolrDocument> node) {
            if (node.isLimit()) {
                return false;
            } else {
                final Object partCount = node.getModelObject().getFieldValue(FacetConstants.FIELD_HAS_PART_COUNT);
                return (partCount instanceof Number) && ((Number) partCount).intValue() > 0;
            }
        }

        @Override
        public Iterator<? extends ModelWrapper<SolrDocument>> getChildren(ModelWrapper<SolrDocument> node) {
            if (node.isLimit()) {
                return Collections.emptyIterator();
            } else {
                final Collection<Object> parts = node.getModelObject().getFieldValues(FacetConstants.FIELD_HAS_PART);
                final Iterator<Object> objectIterator;
                if (childrenShown == null) {
                    // unlimited
                    objectIterator = parts.iterator();
                } else {
                    objectIterator = Iterators.limit(parts.iterator(), childrenShown);
                }
                final Iterator<ModelWrapper<SolrDocument>> iterator = Iterators.transform(objectIterator, new Function<Object, ModelWrapper<SolrDocument>>() {

                    @Override
                    public ModelWrapper<SolrDocument> apply(Object childId) {
                        final SolrDocument document = documentService.getDocument(childId.toString());
                        return new ModelWrapper<>(new SolrDocumentModel(document));
                    }
                });
                if (childrenShown != null && parts.size() > childrenShown) {
                    return Iterators.concat(iterator, // add empty model wrapper to indicate "end of page"
                            Iterators.singletonIterator(new ModelWrapper<SolrDocument>(parts.size())));
                } else {
                    return iterator;
                }
            }
        }

        @Override
        public IModel<ModelWrapper<SolrDocument>> model(ModelWrapper<SolrDocument> object) {
            return object;
        }
    }

    private class ParentsDataProvider implements IDataProvider<SolrDocument> {

        // used to cache parent count (until detach)
        private Long size = null;

        @Override
        public Iterator<? extends SolrDocument> iterator(long first, long count) {
            final Collection<Object> parentIds = HierarchyPanel.this.getModelObject().getFieldValues(FacetConstants.FIELD_IS_PART_OF);
            if (parentIds == null) {
                // no parents, so provide empty list of documents
                return Collections.emptyIterator();
            } else {
                // parents found, convert ID collection into documents list
                return Iterators.transform(parentIds.iterator(), new Function<Object, SolrDocument>() {

                    @Override
                    public SolrDocument apply(Object docId) {
                        return documentService.getDocument(docId.toString());
                    }
                });
            }
        }

        @Override
        public long size() {
            if (size == null) {
                final Collection<Object> fieldValues = HierarchyPanel.this.getModelObject().getFieldValues(FacetConstants.FIELD_IS_PART_OF);
                if (fieldValues == null) {
                    // no parent field
                    size = 0L;
                } else {
                    // any number of parent fields
                    size = Long.valueOf(fieldValues.size());
                }
            }
            return size;
        }

        @Override
        public IModel<SolrDocument> model(SolrDocument object) {
            if (object == null) {
                // dangling hasPart references can happen...
                return new Model<>();
            } else {
                return new SolrDocumentModel(object);
            }
        }

        @Override
        public void detach() {
            size = null;
        }
    }

    private class ModelWrapper<T> extends AbstractReadOnlyModel<ModelWrapper<T>> implements Serializable {

        /**
         * Limit reached?
         */
        private final boolean limit;

        /**
         * Total number of items (if beyond the limit)
         */
        private final int count;

        /**
         * Inner model
         */
        private final IModel<T> model;

        public ModelWrapper(int count) {
            this.limit = true;
            this.count = count;
            this.model = null;
        }

        public ModelWrapper(IModel<T> model) {
            this.limit = false;
            this.count = -1;
            this.model = model;
        }

        public boolean isLimit() {
            return limit;
        }

        public IModel<T> getModel() {
            return model;
        }

        public int getCount() {
            return count;
        }

        public T getModelObject() {
            if (model == null) {
                return null;
            } else {
                return model.getObject();
            }
        }

        // as IModel<T>
        @Override
        public void detach() {
            if (model != null) {
                model.detach();
            }
        }

        // as IModel<T>
        @Override
        public ModelWrapper<T> getObject() {
            return this;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.limit ? 1 : 0);
            hash = 41 * hash + Objects.hashCode(this.model);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ModelWrapper<?> other = (ModelWrapper<?>) obj;
            if (this.limit != other.limit) {
                return false;
            }
            if (!Objects.equals(this.model, other.model)) {
                return false;
            }
            return true;
        }

    }

}
