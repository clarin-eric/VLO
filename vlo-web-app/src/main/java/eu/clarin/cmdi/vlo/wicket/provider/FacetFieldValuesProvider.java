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
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import eu.clarin.cmdi.vlo.wicket.CachingConverter;
import java.io.Serializable;
import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides facet values and counts (both through the {@link Count} object of
 * SOLR) present on a {@link FacetField}, sortable by either count or name
 *
 * @see FieldValuesOrder
 * @author twagoo
 */
public class FacetFieldValuesProvider extends SortableDataProvider<FacetField.Count, FieldValuesOrder> implements ListProvider<FacetField.Count> {

    private final static Logger logger = LoggerFactory.getLogger(FacetFieldValuesProvider.class);
    private final IModel<FacetField> model;
    private final int maxNumberOfItems;
    private final Collection<String> lowPriorityValues;
    private final FieldValueConverterProvider fieldValueConverterProvider;

    /**
     * cached result size
     */
    private Long size;
    /**
     * cached filtered values
     */
    private Iterable<Count> filtered;

    public FacetFieldValuesProvider(IModel<FacetField> model, FieldValueConverterProvider fieldValueConverterProvider) {
        this(model, null, fieldValueConverterProvider);
    }

    /**
     * Creates a provider without a maximum number of values. Bound to
     * {@link Integer#MAX_VALUE}.
     *
     * @param model FacetField model to get values and counts for
     * @param lowPriorityValues values that should with low priority (e.g.
     * unknown, unspecified)
     * @param fieldValueConverterProvider
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, Collection<String> lowPriorityValues, FieldValueConverterProvider fieldValueConverterProvider) {
        this(model, Integer.MAX_VALUE, lowPriorityValues, fieldValueConverterProvider);
    }

    /**
     *
     * @param model FacetField model to get values and counts for
     * @param max maximum number of values to show
     * @param lowPriorityValues (e.g. unknown, unspecified)
     * @param fieldValueConverterProvider
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, int max, Collection<String> lowPriorityValues, FieldValueConverterProvider fieldValueConverterProvider) {
        this(model, max, lowPriorityValues, new SortParam<>(FieldValuesOrder.COUNT, false), fieldValueConverterProvider);
    }

    public FacetFieldValuesProvider(IModel<FacetField> model, int max, SortParam<FieldValuesOrder> sort, FieldValueConverterProvider fieldValueConverterProvider) {
        this(model, max, null, sort, fieldValueConverterProvider);
    }

    /**
     * Creates a provider with a set maximum number of values
     *
     * @param model FacetField model to get values and counts for
     * @param max maximum number of values to show
     * @param lowPriorityValues (e.g. unknown, unspecified)
     * @param sort initial sort property and order
     * @param fieldValueConverterProvider
     */
    public FacetFieldValuesProvider(IModel<FacetField> model, int max, Collection<String> lowPriorityValues, SortParam<FieldValuesOrder> sort, FieldValueConverterProvider fieldValueConverterProvider) {
        this.model = model;
        this.maxNumberOfItems = max;
        this.lowPriorityValues = lowPriorityValues;
        this.fieldValueConverterProvider = fieldValueConverterProvider;
        setSort(sort);
    }

    /**
     * override to achieve filtering
     *
     * @return model of string value that item values should contain
     */
    protected IModel<FieldValuesFilter> getFilterModel() {
        return null;
    }

    @Override
    public Iterator<FacetField.Count> iterator(long first, long count) {
        // return iterator starting at specified offset
        return getList().listIterator((int) first);
    }

    @Override
    public List<FacetField.Count> getList() {
        final Iterable<Count> filteredValues = getFilteredValues();
        // sort what remains
        final ImmutableList sorted = getOrdering().immutableSortedCopy(filteredValues);
        if (sorted.size() > maxNumberOfItems) {
            return Lists.newArrayList(sorted.subList(0, maxNumberOfItems));
        } else {
            // return iterator starting at specified offset
            return sorted;
        }
    }

    @Override
    public long size() {
        if (size == null) {
            size = getSize();
        }
        return size;
    }

    @Override
    public IModel<FacetField.Count> model(FacetField.Count object) {
        return new Model(object);
    }

    /* 
     * CACHING AND FILTERING
     */
    private Iterable<FacetField.Count> filter(List<FacetField.Count> list) {
        if (hasFilter()) {
            final IConverter<String> converter = fieldValueConverterProvider.getConverter(model.getObject().getName());
            return Iterables.filter(list, new Predicate<FacetField.Count>() {
                @Override
                public boolean apply(Count input) {
                    return getFilterModel().getObject().matches(input, converter);
                }
            });
        } else {
            return list;
        }
    }

    private Iterable<Count> getFilteredValues() {
        if (filtered == null) {
            // get all the values
            final List<FacetField.Count> values = model.getObject().getValues();
            filtered = filter(values);
        }
        return filtered;
    }

    private long getSize() {
        if (hasFilter()) {
            return Math.min(maxNumberOfItems, Iterables.size(getFilteredValues()));
        } else {
            // Use value count from Solr, faster.
            //
            // Actual value count might be higher than what we want to show
            // so get minimum.
            return Math.min(maxNumberOfItems, model.getObject().getValueCount());
        }
    }

    private boolean hasFilter() {
        return getFilterModel() != null && getFilterModel().getObject() != null && !getFilterModel().getObject().isEmpty();
    }

    /* 
     * ORDERING
     */
    protected Ordering getOrdering() {
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
        switch (getSort().getProperty()) {
            case COUNT:
                return applyOrder(new CountOrdering());
            case NAME:
                return applyOrder(new NameOrdering(getLocale(), fieldValueConverterProvider.getConverter(model.getObject().getName())));
            default:
                return Ordering.arbitrary();
        }
    }

    private Ordering applyOrder(Ordering ordering) {
        if (getSort().isAscending()) {
            return ordering;
        } else {
            return ordering.reverse();
        }
    }

    protected Locale getLocale() {
        try {
            final Session session = Session.get();
            if (session != null) {
                return session.getLocale();
            }
        } catch (WicketRuntimeException ex) {
            logger.info("No session available, falling back to JVM default locale");
        }
        return Locale.getDefault();
    }

    private final static class CountOrdering extends Ordering<FacetField.Count> {

        @Override
        public int compare(Count arg0, Count arg1) {
            return Long.compare(arg0.getCount(), arg1.getCount());
        }
    };

    private final static class NameOrdering extends Ordering<FacetField.Count> implements Serializable {

        private final Collator collator;
        private final IConverter converter;
        private final Locale locale;

        public NameOrdering(Locale locale, IConverter<String> converter) {
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.PRIMARY);
            this.converter = CachingConverter.wrap(converter);
            this.locale = locale;
        }

        @Override
        public int compare(Count arg0, Count arg1) {
            if (converter == null) {
                return collator.compare(arg0.getName(), arg1.getName());
            } else {
                return collator.compare(
                        converter.convertToString(arg0.getName(), locale),
                        converter.convertToString(arg1.getName(), locale));
            }
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

    @Override
    public void detach() {
        model.detach();
        // invalidate cache variables
        size = null;
        filtered = null;
    }
}