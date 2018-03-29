/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package eu.clarin.cmdi.vlo.wicket.components;

import java.util.Set;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.model.IModel;

/**
 * Nesting tree that has {@link IndicatingAjaxFallbackLink} instances for
 * junction links so that an indicator is shown when a user expands a node.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @param <T>
 */
public abstract class IndicatingNestedTree<T> extends DefaultNestedTree<T> {

    public IndicatingNestedTree(String id, ITreeProvider<T> provider) {
        super(id, provider);
    }

    public IndicatingNestedTree(String id, ITreeProvider<T> provider, IModel<Set<T>> state) {
        super(id, provider, state);
    }

    @Override
    public Component newNodeComponent(String id, final IModel<T> model) {
        // when aksed to create a node, return an instance that provides an indicating link for junctions
        final Node node = new IndicatingNode(id, this, model);
        node.setOutputMarkupId(true);
        return node;
    }

    public static class IndicatingNode<U> extends Node<U> {

        private final IModel<U> model;
        private final AbstractTree<U> tree;

        public IndicatingNode(String id, AbstractTree<U> tree, IModel<U> model) {
            super(id, tree, model);
            this.model = model;
            this.tree = tree;
        }
        private static final long serialVersionUID = 1L;

        @Override
        protected Component createContent(String id, IModel<U> model) {
            return ((IndicatingNestedTree) tree).newContentComponent(id, model);
        }

        @Override
        protected MarkupContainer createJunctionComponent(String id) {
            // based on the default implementation in Node, but returning an indicating link instead
            return new IndicatingAjaxFallbackLink<Void>(id) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    toggle();
                }

                @Override
                public boolean isEnabled() {
                    return tree.getProvider().hasChildren(model.getObject());
                }
            };
        }
    }

}
