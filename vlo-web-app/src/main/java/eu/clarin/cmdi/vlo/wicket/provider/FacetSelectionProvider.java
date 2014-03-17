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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.CollectionMapEntryModel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class FacetSelectionProvider implements IDataProvider<Map.Entry<String, Collection<String>>> {
    private final IModel<QueryFacetsSelection> selectionModel;

    public FacetSelectionProvider(IModel<QueryFacetsSelection> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public Iterator<? extends Map.Entry<String, Collection<String>>> iterator(long first, long count) {
        return selectionModel.getObject().getSelection().entrySet().iterator();
    }

    @Override
    public long size() {
        return selectionModel.getObject().getSelection().size();
    }

    @Override
    public IModel<Map.Entry<String, Collection<String>>> model(Map.Entry<String, Collection<String>> object) {
        return new CollectionMapEntryModel<String, String>(object);
    }

    @Override
    public void detach() {
        selectionModel.detach();
    }
    
}
