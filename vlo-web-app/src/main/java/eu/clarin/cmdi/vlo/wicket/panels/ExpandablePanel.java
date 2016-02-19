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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
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
        createTitleToggler();
        // expand/collapse controls
        addExpandCollapse();

        setOutputMarkupId(true);
    }

    private void addExpandCollapse() {
        // class modifier to apply correct class depending on state
        add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                switch (expansionModel.getObject()) {
                    case COLLAPSED:
                        return getCollapsedClass();
                    case EXPANDED:
                        return getExpandedClass();
                    default:
                        return getFallbackClass();
                }
            }
        }));

        // add expansion link
        add(new IndicatingAjaxFallbackLink("expand") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                expansionModel.setObject(ExpansionState.EXPANDED);
                if (target != null) {
                    target.add(ExpandablePanel.this);
                }
            }
        });

        // add collapse link
        add(new IndicatingAjaxFallbackLink("collapse") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                expansionModel.setObject(ExpansionState.COLLAPSED);
                if (target != null) {
                    target.add(ExpandablePanel.this);
                }
            }
        });
    }

    private void createTitleToggler() {
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
            }
        };

        // Facet name becomes title
        titleLink.add(createTitleLabel("title"));
        add(titleLink);
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

}
