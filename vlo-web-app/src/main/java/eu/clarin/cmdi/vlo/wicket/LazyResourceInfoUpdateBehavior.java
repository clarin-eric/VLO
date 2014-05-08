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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.time.Duration;

/**
 * A behavior that updates a {@link ResourceInfo} model using the provided
 * conversion service. This is useful for a two-stage process in which 'cheap'
 * details are shown first, while more expensive information (such as the
 * resolved URL of a handle) are retrieved lazily and asynchronously.
 *
 * @author twagoo
 */
public abstract class LazyResourceInfoUpdateBehavior extends AbstractAjaxTimerBehavior {

    private final ResourceStringConverter converter;
    private final ResourceInfoModel resourceInfoModel;

    /**
     *
     * @param converter converter to get resource info object from
     * @param resourceInfoModel model that should be updated
     */
    public LazyResourceInfoUpdateBehavior(ResourceStringConverter converter, ResourceInfoModel resourceInfoModel) {
        super(Duration.ONE_SECOND);
        this.converter = converter;
        this.resourceInfoModel = resourceInfoModel;
    }

    @Override
    protected void onTimer(AjaxRequestTarget target) {
        // stop timer so that it gets called only once
        this.stop(target);
        
        // inject the advanced converter into the model
        resourceInfoModel.setResourceStringConverter(converter);
        
        // AJAX update of components
        onUpdate(target);
    }

    /**
     * Gets called after the model has been updated
     *
     * @param target the request target
     */
    protected abstract void onUpdate(AjaxRequestTarget target);

}
