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
package eu.clarin.cmdi.vlo.wicket.provider;

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Provides facet values and counts (both through the {@link Count} object of
 * SOLR) present on a {@link FacetField}, sortable by either count or name
 *
 * @see FieldValuesOrder
 * @author twagoo
 */
public class FacetFieldValuesProvider extends SortableDataProvider<FacetField.Count, FieldValuesOrder> {

    private final IModel<FacetField> model;
    private final int maxNumberOfItems;

    /**
     * Creates a provider without a maximum number of values. Bound to
     * {@link Integer#MAX_VALUE}.
     *
     * @param model FacetField model to get values and counts for
     */
    public FacetFieldValuesProvider(IModel<FacetField> model) {
        this(model, Integer.MAX_VALUE);
    }

    public FacetFieldValuesProvider(IModel<FacetField> model, int max) {
        this(model, max, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, false));
    }

    /**
     * Creates a provider with a set maximum number of values
     *
     * @param model FacetField model to get values and counts for
     * @param max maximum number of values to show
     * @param sort initial sort property and order
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, int max, SortParam<FieldValuesOrder> sort) {
        this.model = model;
        this.maxNumberOfItems = max;
        setSort(sort);
    }

    @Override
    public Iterator<? extends FacetField.Count> iterator(long first, long count) {
        // TODO: From old VLO:
        // IGNORABLE_VALUES (like "unknown") are move to the back of the list and should only be shown when you click "more...", unless the list is too small then whe can just show them.
        final List<FacetField.Count> values = model.getObject().getValues();
        return getOrdering().sortedCopy(values).listIterator((int) first);
    }

    @Override
    public long size() {
        // Actual value count might be higher than what we want to show
        return Math.min(maxNumberOfItems, model.getObject().getValueCount());
    }

    @Override
    public IModel<FacetField.Count> model(FacetField.Count object) {
        return new Model(object);
    }

    @Override
    public void detach() {
        model.detach();
    }

    private Ordering getOrdering() {
        final Ordering ordering;
        if (getSort().getProperty() == FieldValuesOrder.COUNT) {
            ordering = COUNT_ORDERING;
        } else if (getSort().getProperty() == FieldValuesOrder.NAME) {
            ordering = NAME_ORDERING;
        } else {
            ordering = Ordering.natural();
        }

        if (getSort().isAscending()) {
            return ordering;
        } else {
            return ordering.reverse();
        }
    }

    private static Ordering<FacetField.Count> COUNT_ORDERING = new Ordering<FacetField.Count>() {

        @Override
        public int compare(Count arg0, Count arg1) {
            return Long.compare(arg0.getCount(), arg1.getCount());
        }
    };

    private static Ordering<FacetField.Count> NAME_ORDERING = new Ordering<FacetField.Count>() {

        @Override
        public int compare(Count arg0, Count arg1) {
            return arg0.getName().compareTo(arg1.getName());
        }
    };

}
