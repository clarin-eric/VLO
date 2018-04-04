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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Panel with expand/collapse controls which control the css class of the
 * component's markup element (as defined in {@link #getExpandedClass() }, {@link #getCollapsedClass()
 * } and {@link #getFallbackClass() }). It also adds a title that doubles as an
 * expansion state toggler.
 *
 * @author twagoo
 * @param <T> the type of the panel's model object
 */
public abstract class ExpandablePanel<T> extends GenericPanel<T> {

    protected final IModel<ExpansionState> expansionModel;

    /**
     * Creates the panel with its own expansion state model which is closed
     * initially
     *
     * @param id component id
     * @param model core model of this panel
     */
    public ExpandablePanel(String id, IModel<T> model) {
        this(id, model, Model.of(ExpansionState.COLLAPSED));
    }

    /**
     *
     * @param id component id
     * @param model core model of this panel
     * @param expansionStateModel model that holds the expansion state
     */
    public ExpandablePanel(String id, IModel<T> model, IModel<ExpansionState> expansionStateModel) {
        super(id, model);
        this.expansionModel = expansionStateModel;

        // title annex expansion toggler
        add(createTitleToggler());
        // expand/collapse controls
        addExpandCollapse();

        setOutputMarkupId(true);
    }

    private void addExpandCollapse() {
        // class modifier to apply correct class depending on state
        add(new AttributeModifier("class", new ExpansionStateRepresentationModel(expansionModel, getExpandedClass(), getCollapsedClass(), getFallbackClass())));
        // aria-expended attribute for screenreaders
        add(new AttributeModifier("aria-expanded", new ExpansionStateRepresentationModel(expansionModel, "true", "false", "undefined")));
    }

    protected Link createTitleToggler() {
        // title is also a link that toggles expansion state
        final AjaxFallbackLink titleLink = new IndicatingAjaxFallbackLink("titleToggle") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                final ExpansionState expansionState = expansionModel.getObject();
                if (expansionState == ExpansionState.COLLAPSED) {
                    expansionModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionModel.setObject(ExpansionState.COLLAPSED);
                }
                if (target != null) {
                    target.add(ExpandablePanel.this);
                }
                onExpansionToggle(target);
            }
        };

        // Facet name becomes title
        titleLink.add(createTitleLabel("title"));
        titleLink.add(new AttributeModifier("aria-controls", ExpandablePanel.this.getMarkupId()));

        titleLink.add(new WebMarkupContainer("expand"));
        titleLink.add(new WebMarkupContainer("collapse"));
        titleLink.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                final boolean expanded = expansionModel.getObject().equals(ExpansionState.EXPANDED);
                component.get("expand").setVisible(!expanded);
                component.get("collapse").setVisible(expanded);
            }

        });

        return titleLink;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionModel.detach();
    }

    public final IModel<ExpansionState> getExpansionModel() {
        return expansionModel;
    }

    protected abstract Label createTitleLabel(String id);

    /**
     *
     * @return css class for panel if the expansion state is neither collapsed
     * or expanded
     */
    protected String getFallbackClass() {
        return "facet";
    }

    /**
     *
     * @return css class for panel if the expansion state expanded
     */
    protected String getExpandedClass() {
        return "facet expandedfacet";
    }

    /**
     *
     * @return css class for panel if the expansion state is collapsed
     */
    protected String getCollapsedClass() {
        return "facet collapsedfacet";
    }
    
    /**
     * Override to apply logic when expansion state is toggled
     * @param target 
     */
    protected void onExpansionToggle(AjaxRequestTarget target) {
       //NOOP 
    }

    private static class ExpansionStateRepresentationModel extends AbstractReadOnlyModel<String> {

        private final IModel<ExpansionState> stateModel;
        private final String expanded;
        private final String collapsed;
        private final String fallback;

        public ExpansionStateRepresentationModel(IModel<ExpansionState> stateModel, String expanded, String collapsed, String fallback) {
            this.stateModel = stateModel;
            this.expanded = expanded;
            this.collapsed = collapsed;
            this.fallback = fallback;
        }

        @Override
        public String getObject() {
            switch (stateModel.getObject()) {
                case COLLAPSED:
                    return collapsed;
                case EXPANDED:
                    return expanded;
                default:
                    return fallback;
            }
        }

        @Override
        public void detach() {
            stateModel.detach();
        }

    }

}
