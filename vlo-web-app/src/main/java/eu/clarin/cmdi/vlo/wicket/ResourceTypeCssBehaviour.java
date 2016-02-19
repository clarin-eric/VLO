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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;

/**
 * Set the class attribute on a component from the value associated with the
 * resource type as defined in the resourceTypeClass properties file
 *
 * @author twagoo
 */
public class ResourceTypeCssBehaviour extends AttributeAppender {

    public ResourceTypeCssBehaviour(IModel<ResourceInfo> resourceInfoModel) {
        super("class",
                // Matches the 'resourceType' property of the resource info model
                // to one of the properties in resourceTypeClass.properties file
                // (defaults to "")
                StringResourceModelMigration.of("resourcetype.${resourceType}.class", resourceInfoModel, "", new Object[0]));
        // separate CSS classes with a space
        setSeparator(" ");
    }

}
