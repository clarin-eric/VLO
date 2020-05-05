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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.InvisibleIfNullBehaviour;
import eu.clarin.cmdi.vlo.wicket.model.EnvironmentVariableModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 *
 * @author twagoo
 */
public class AboutPage extends VloBasePage {

    public static final String VLO_DOCKER_IMAGE_ENV_VAR = "VLO_DOCKER_IMAGE";

    public AboutPage() {
        final IModel<String> containedIdModel = new EnvironmentVariableModel(VLO_DOCKER_IMAGE_ENV_VAR);

        add(new Label("containerId", containedIdModel)
                .add(new InvisibleIfNullBehaviour<>(containedIdModel))
        );
    }

    @Override
    public IModel getTitleModel() {
        return new StringResourceModel("pageTitle.aboutPage", this, super.getTitleModel());
    }

}
