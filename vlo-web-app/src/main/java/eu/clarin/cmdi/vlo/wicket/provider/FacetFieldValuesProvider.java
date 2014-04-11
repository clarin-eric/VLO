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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import java.util.Collection;
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
 * TODO: Add option to hide values with only 1 record (as in VLO 2.x)
 *
 * @see FieldValuesOrder
 * @author twagoo
 */
public class FacetFieldValuesProvider extends SortableDataProvider<FacetField.Count, FieldValuesOrder> {

    private final IModel<FacetField> model;
    private final int maxNumberOfItems;
    private final Collection<String> lowPriorityValues;

    /**
     * Creates a provider without a maximum number of values. Bound to
     * {@link Integer#MAX_VALUE}.
     *
     * @param model FacetField model to get values and counts for
     * @param lowPriorityValues values that should with low priority (e.g.
     * unknown, unspecified)
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, Collection<String> lowPriorityValues) {
        this(model, Integer.MAX_VALUE, lowPriorityValues);
    }

    /**
     *
     * @param model FacetField model to get values and counts for
     * @param max maximum number of values to show
     * @param lowPriorityValues (e.g. unknown, unspecified)
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, int max, Collection<String> lowPriorityValues) {
        this(model, max, lowPriorityValues, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, false));
    }

    public FacetFieldValuesProvider(IModel<FacetField> model, int max, SortParam<FieldValuesOrder> sort) {
        this(model, max, null, sort);
    }

    /**
     * Creates a provider with a set maximum number of values
     *
     * @param model FacetField model to get values and counts for
     * @param max maximum number of values to show
     * @param lowPriorityValues (e.g. unknown, unspecified)
     * @param sort initial sort property and order
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, int max, Collection<String> lowPriorityValues, SortParam<FieldValuesOrder> sort) {
        this.model = model;
        this.maxNumberOfItems = max;
        this.lowPriorityValues = lowPriorityValues;
        setSort(sort);
    }

    /**
     * override to achieve filtering
     *
     * @return model of string value that item values should contain
     */
    protected IModel<String> getFilterModel() {
        return null;
    }

    @Override
    public Iterator<? extends FacetField.Count> iterator(long first, long count) {
        // get all the values
        final List<FacetField.Count> values = model.getObject().getValues();
        // filter results
        final Iterable<Count> filtered = filter(values);
        // sort what remains
        final ImmutableList sorted = getOrdering().immutableSortedCopy(filtered);
        if (sorted.size() > maxNumberOfItems) {
            return sorted.subList(0, maxNumberOfItems).listIterator((int) first);
        } else {
            // return iterator starting at specified offset
            return sorted.listIterator((int) first);
        }
    }

    @Override
    public long size() {
        if (hasFilter()) {
            final List<FacetField.Count> values = model.getObject().getValues();
            return Math.min(maxNumberOfItems, Iterables.size(filter(values)));
        } else {
            // Use value count from Solr, faster.
            //
            // Actual value count might be higher than what we want to show
            // so get minimum.
            return Math.min(maxNumberOfItems, model.getObject().getValueCount());
        }
    }

    @Override
    public IModel<FacetField.Count> model(FacetField.Count object) {
        return new Model(object);
    }

    @Override
    public void detach() {
        model.detach();
    }

    private Iterable<FacetField.Count> filter(List<FacetField.Count> list) {
        if (hasFilter()) {
            final String filterValue = getFilterModel().getObject().toLowerCase();
            return Iterables.filter(list, new Predicate<FacetField.Count>() {
                @Override
                public boolean apply(Count input) {
                    return input.getName().toLowerCase().contains(filterValue);
                }
            });
        } else {
            return list;
        }
    }

    private boolean hasFilter() {
        return !(getFilterModel() == null || getFilterModel().getObject() == null || getFilterModel().getObject().isEmpty());
    }

    private Ordering getOrdering() {
        if (lowPriorityValues != null && getSort().getProperty() == FieldValuesOrder.COUNT) {
            // in case of count, low priority fields should always be moved to the back
            // rest should be sorted as requested
            return new PriorityOrdering(lowPriorityValues).compound(getBaseOrdering());
        } else {
            // in other case (name), priorty should not be take into account
            return getBaseOrdering();
        }
    }

    private Ordering getBaseOrdering() {
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
            // TODO: From old VLO:
            return arg0.getName().compareToIgnoreCase(arg1.getName());
        }
    };

    /**
     * An ordering that pushes all values identified in a provided collection to
     * the back of the list
     */
    private static class PriorityOrdering extends Ordering<FacetField.Count> {

        private final Collection<String> lowPriorityValues;

        public PriorityOrdering(Collection<String> lowPriorityValues) {
            this.lowPriorityValues = lowPriorityValues;
        }

        @Override
        public int compare(Count arg0, Count arg1) {

            if (lowPriorityValues.contains(arg0.getName())) {
                if (!lowPriorityValues.contains(arg1.getName())) {
                    //arg0 is low priority, arg1 is not -> move arg0 to back
                    return 1;
                }
            } else if (lowPriorityValues.contains(arg1.getName())) {
                //arg0 is not low priority but arg1 is -> move arg1 to back
                return -1;
            }
            // arg0 and arg1 are either both low priority or neither of them are, 
            // so fall back to secondary comparator (assuming a compound)
            return 0;
        }

    };
}
