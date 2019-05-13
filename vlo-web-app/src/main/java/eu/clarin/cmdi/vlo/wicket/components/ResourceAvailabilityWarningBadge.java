/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceAvailabilityWarningBadge extends Panel {

    private static final String UNAVAILABLE_ICON_CLASS = "glyphicon glyphicon-exclamation-sign";
    private static final String RESTRICTED_ACCESS_ICON_CLASS = "glyphicon glyphicon-lock";
    private final IModel<Boolean> isUnavailableModel;
    private final IModel<Boolean> isRestrictedAccessModel;

    public ResourceAvailabilityWarningBadge(String id, IModel<Boolean> isUnavailableModel, IModel<Boolean> isRestrictedAccessModel) {
        super(id);
        this.isUnavailableModel = isUnavailableModel;
        this.isRestrictedAccessModel = isRestrictedAccessModel;

        add(new WebMarkupContainer("icon")
                .add(new AttributeModifier("class", new ResourceAvailabilityWarningIconClassModel())))
                .add(new AttributeModifier("title", new ResourceAvailabilityWarningTitleModel()));
    }

    public ResourceAvailabilityWarningBadge(String id, IModel<ResourceInfo> model) {
        this(id, new PropertyModel<>(model, "availabilityWarning"), new PropertyModel<>(model, "restrictedAccessWarning"));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(isUnavailableModel.getObject());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        isUnavailableModel.detach();
        isRestrictedAccessModel.detach();
    }

    private class ResourceAvailabilityWarningIconClassModel extends LoadableDetachableModel<String> {

        @Override
        protected String load() {
            if (isRestrictedAccessModel.getObject()) {
                return RESTRICTED_ACCESS_ICON_CLASS;
            } else {
                return UNAVAILABLE_ICON_CLASS;
            }
        }
    }

    private class ResourceAvailabilityWarningTitleModel extends LoadableDetachableModel<String> {

        @Override
        public String load() {
            if (isRestrictedAccessModel.getObject()) {
                return "Authentication and/or special permissions may be required in order to access the resource. Click to see details.";
            } else {
                return "The resource may not be available at this location. Click to see details.";
            }
        }
    }
}
