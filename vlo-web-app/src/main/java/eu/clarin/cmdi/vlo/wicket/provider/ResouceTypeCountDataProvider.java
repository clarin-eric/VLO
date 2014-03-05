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

import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import java.util.Collection;
import java.util.Iterator;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Provider that uses a {@link ResourceTypeCountingService} to count the number
 * of resources of each of the resource types in a collection of resources
 * coming from a Solr document
 *
 * @author twagoo
 */
public class ResouceTypeCountDataProvider implements IDataProvider<ResourceTypeCount> {

    private final ResourceTypeCountingService countingService;
    private final IModel<Collection<String>> resourcesModel;
    private Collection<ResourceTypeCount> resourceTypeCounts;

    /**
     *
     * @param resourcesModel model that holds the resource strings from the
     * document
     * @param countingService service that should be used to do the counting
     */
    public ResouceTypeCountDataProvider(IModel<Collection<String>> resourcesModel, ResourceTypeCountingService countingService) {
        this.resourcesModel = resourcesModel;
        this.countingService = countingService;
    }

    /**
     * get cached counts or put counts in cache
     *
     * @return current count
     */
    private synchronized Collection<ResourceTypeCount> getCount() {
        if (resourceTypeCounts == null) {
            resourceTypeCounts = countingService.countResourceTypes(resourcesModel.getObject());
        }
        return resourceTypeCounts;
    }

    @Override
    public Iterator<? extends ResourceTypeCount> iterator(long first, long count) {
        return getCount().iterator();
    }

    @Override
    public long size() {
        return getCount().size();
    }

    @Override
    public IModel<ResourceTypeCount> model(ResourceTypeCount object) {
        return Model.of(object);
    }

    @Override
    public void detach() {
        resourcesModel.detach();
        // invalidate cached counts
        resourceTypeCounts = null;
    }

}
