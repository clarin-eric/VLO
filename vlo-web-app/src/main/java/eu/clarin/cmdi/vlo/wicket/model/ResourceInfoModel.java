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

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Model for {@link ResourceInfo} that dynamically instantiates its objects from
 * a resource string (as retrieved from the Solr index) using the
 * {@link ResourceStringConverter}
 */
public class ResourceInfoModel extends Model<ResourceInfo> {

    private ResourceStringConverter resourceStringConverter;
    private final IModel<String> resourceStringModel;

    /**
     *
     * @param resourceStringConverter converter to use to derive resource info
     * from the resource string
     * @param resourceStringModel
     */
    public ResourceInfoModel(ResourceStringConverter resourceStringConverter, IModel<String> resourceStringModel) {
        this.resourceStringConverter = resourceStringConverter;
        this.resourceStringModel = resourceStringModel;
    }

    @Override
    public ResourceInfo getObject() {
        final ResourceInfo cached = super.getObject();
        if (cached == null) {
            final ResourceInfo object = resourceStringConverter.getResourceInfo(resourceStringModel.getObject());
            setObject(object);
            return object;
        } else {
            return cached;
        }
    }

    /**
     * Replaces the converter used by this model to get a resource info object.
     * This also invalidates the cached resource info object.
     *
     * @param resourceStringConverter
     */
    public void setResourceStringConverter(ResourceStringConverter resourceStringConverter) {
        this.resourceStringConverter = resourceStringConverter;
        // invalidate object
        setObject(null);
    }

}
