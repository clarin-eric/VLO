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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author twagoo
 */
public class SearchContextModel extends AbstractReadOnlyModel<SearchContext> implements SearchContext {

    private final long index;
    private final long resultCount;
    private final IModel<QueryFacetsSelection> selectionModel;

    public SearchContextModel(long index, long resultCount, IModel<QueryFacetsSelection> selectionModel) {
        this.index = index;
        this.resultCount = resultCount;
        this.selectionModel = selectionModel;
    }

    public IModel<QueryFacetsSelection> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public QueryFacetsSelection getSelection() {
        return selectionModel.getObject();
    }

    @Override
    public long getResultCount() {
        return resultCount;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public SearchContext getObject() {
        return this;
    }

    @Override
    public void detach() {
        selectionModel.detach();
    }

    public static SearchContextModel next(SearchContext current) {
        final long count = current.getResultCount();
        final long nextIndex = current.getIndex() + 1;
        if (nextIndex < count) {
            return createNew(current, nextIndex, count);
        } else {
            // last index reached
            return null;
        }
    }

    public static SearchContextModel previous(SearchContext current) {
        final long count = current.getResultCount();
        final long prevIndex = current.getIndex() - 1;
        if (prevIndex >= 0) {
            return createNew(current, prevIndex, count);
        } else {
            // last index reached
            return null;
        }
    }

    private static SearchContextModel createNew(SearchContext current, long nextIndex, long count) {
        if (current instanceof SearchContextModel) {
            return new SearchContextModel(nextIndex, count, ((SearchContextModel) current).getSelectionModel());
        } else {
            return new SearchContextModel(nextIndex, count, Model.of(current.getSelection()));
        }
    }

}
