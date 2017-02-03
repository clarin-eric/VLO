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

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Wrapper for a sortable data provider that partitions the returned data in
 * sublist of a predefined maximum size
 *
 * @author twagoo
 * @param <T> the type of provided data
 * @param <S> the type of the sorting parameter
 */
public class PartitionedDataProvider<T, S> implements ISortableDataProvider<List<T>, S> {

    private final ISortableDataProvider<T, S> provider;
    private final int partitionSize;

    /**
     *
     * @param provider provider to wrap and partition
     * @param partitionSize size of each partition (last partition may be
     * smaller); values {@literal  <= 0} will generate a single partition
     */
    public PartitionedDataProvider(ISortableDataProvider<T, S> provider, int partitionSize) {
        this.provider = provider;
        this.partitionSize = partitionSize;
    }

    @Override
    public Iterator<? extends List<T>> iterator(long first, long count) {
        if (partitionSize > 0) {
            // translate first, count to item level
            final long itemsFirst = first * partitionSize;
            final long itemsCount = count * partitionSize;
            // reduce wildcard to upper bound (i.e. remove "? extends")
            final Iterator<T> iterator
                    = Iterators.transform(provider.iterator(itemsFirst, itemsCount), Functions.<T>identity());
            // split up values 
            return Iterators.partition(iterator, partitionSize);
        } else {
            // return a single list (wrapped in a singleton iterator)
            final List<T> valuesList;
            if (provider instanceof ListProvider) {
                // get list straight out of provider, prevent double wrapping
                valuesList = ((ListProvider<T>) provider).getList();
            } else {
                // put iterator values in list (get all, no pagination at this level)
                valuesList = ImmutableList.<T>copyOf(provider.iterator(0, Long.MAX_VALUE));
            }
            // wrap in iterator
            return Iterators.singletonIterator(valuesList);
        }
    }

    /**
     *
     * @return number of partitions
     */
    @Override
    public long size() {
        if (partitionSize > 0) {
            // 0 or more partitions, calculate from number of items and partition size
            final double valuesCount = (double) provider.size();
            final double partitionsCount = Math.ceil(valuesCount / partitionSize);
            return (long) partitionsCount;
        } else {
            // one partition that has all values
            return 1;
        }
    }

    @Override
    public IModel<List<T>> model(List<T> object) {
        // Iterators.partition sometimes returns lists that are not deeply serializable, 
        // so we need to copy this into a serializable list here :(
        return Model.ofList(ImmutableList.copyOf(object));
    }

    @Override
    public void detach() {
        provider.detach();
    }

    @Override
    public ISortState<S> getSortState() {
        return provider.getSortState();
    }

}
