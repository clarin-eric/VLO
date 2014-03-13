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
package eu.clarin.cmdi.vlo.pojo;

import java.io.Serializable;

/**
 *
 * @author twagoo
 */
public class StaticSearchContext implements SearchContext, Serializable {

    private final long index;
    private final long resultCount;
    private final QueryFacetsSelection selection;

    public StaticSearchContext(long index, long resultCount, QueryFacetsSelection selection) {
        this.index = index;
        this.resultCount = resultCount;
        this.selection = selection;
    }

    @Override
    public QueryFacetsSelection getSelection() {
        return selection;
    }

    @Override
    public long getResultCount() {
        return resultCount;
    }

    @Override
    public long getIndex() {
        return index;
    }

}
