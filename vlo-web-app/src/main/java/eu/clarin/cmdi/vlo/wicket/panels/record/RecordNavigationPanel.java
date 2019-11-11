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
package eu.clarin.cmdi.vlo.wicket.panels.record;

import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.wicket.components.RecordNavigationLink;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Panel that shows the index of the current record and has forward/backward
 * navigation
 *
 * @author twagoo
 */
public class RecordNavigationPanel extends GenericPanel<SearchContext> {

    private final IModel<String> tabModel;

    public RecordNavigationPanel(String id, final IModel<SearchContext> model, IModel<String> tabModel) {
        super(id, model);
        this.tabModel = tabModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Add a label 'record X of Y'
        add(new Label("recordIndex",
                new StringResourceModel("record.navigation.index", this, getModel()).setParameters(
                        // These values get inserted into the string
                        // First: index shifted with +1 (because count starts at 0)
                        new ShiftedIndexModel(new PropertyModel<>(getModel(), "index"), +1),
                        // Second: total result count, unmodified
                        new PropertyModel<>(getModel(), "resultCount")
                )
        ));

        // Add a link to go to the previous record
        add(createPreviousLink(getModel(), tabModel));
        // Add a link to go to the next record
        add(createNextLink(getModel(), tabModel));
    }

    private RecordNavigationLink createPreviousLink(final IModel<SearchContext> model, IModel<String> tabModel) {
        return new RecordNavigationLink("previous", model, tabModel) {
            @Override
            protected IModel<SearchContext> getTargetModel() {
                return SearchContextModel.previous(getModelObject());
            }

            @Override
            protected boolean targetExists() {
                // disable for first item
                return getModelObject().hasPrevious();
            }
        };
    }

    private RecordNavigationLink createNextLink(final IModel<SearchContext> model, IModel<String> tabModel) {
        return new RecordNavigationLink("next", model, tabModel) {
            @Override
            protected IModel<SearchContext> getTargetModel() {
                return SearchContextModel.next(getModelObject());
            }

            @Override
            protected boolean targetExists() {
                // disable for last item
                return getModelObject().hasNext();
            }
        };
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        tabModel.detach();
    }

    /**
     * Model that shifts the value provided by the wrapped model with a fixed
     * amount
     */
    public static class ShiftedIndexModel implements IModel<Long> {

        private final IModel<Long> wrappedModel;
        private final long shift;

        public ShiftedIndexModel(IModel<Long> wrappedModel, long shift) {
            this.wrappedModel = wrappedModel;
            this.shift = shift;
        }

        @Override
        public Long getObject() {
            return wrappedModel.getObject() + shift;
        }

        @Override
        public void detach() {
            wrappedModel.detach();
        }

    }

}
