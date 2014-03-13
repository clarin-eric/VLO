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

}
