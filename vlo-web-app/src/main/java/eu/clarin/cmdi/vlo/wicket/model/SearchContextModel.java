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

    private final Long index;
    private final Long resultCount;
    private final IModel<QueryFacetsSelection> selectionModel;

    public SearchContextModel(IModel<QueryFacetsSelection> selectionModel) {
        this(null, null, selectionModel);
    }

    public SearchContextModel(Long index, Long resultCount, IModel<QueryFacetsSelection> selectionModel) {
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
    public Long getResultCount() {
        return resultCount;
    }

    @Override
    public Long getIndex() {
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
        if (current.hasNext()) {
            final Long count = current.getResultCount();
            final Long nextIndex = current.getIndex() + 1;
            return createNew(current, nextIndex, count);
        } else {
            // last index reached
            return null;
        }
    }

    public static SearchContextModel previous(SearchContext current) {
        if (current.hasPrevious()) {
            final long count = current.getResultCount();
            final long prevIndex = current.getIndex() - 1;
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

    @Override
    public boolean hasPrevious() {
        if (index == null || resultCount == null) {
            return false;
        } else {
            return index > 0;
        }
    }

    @Override
    public boolean hasNext() {
        if (index == null || resultCount == null) {
            return false;
        } else {
            return index + 1 < resultCount;
        }
    }

}
