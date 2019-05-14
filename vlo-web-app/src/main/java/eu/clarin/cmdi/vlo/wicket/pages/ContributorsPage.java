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

import eu.clarin.cmdi.vlo.service.centreregistry.CentreRegistryProvidersService;
import eu.clarin.cmdi.vlo.service.centreregistry.CentreRegistryProvidersService.EndpointProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public class ContributorsPage extends VloBasePage {

    public static final String VLO_DOCKER_IMAGE_ENV_VAR = "VLO_DOCKER_IMAGE";

    //TOOD: get from config
    private final static String CENTRE_REGISTRY_ENDPOINTS_LIST_JSON_URL = "https://centres.clarin.eu/api/model/OAIPMHEndpoint";
    private final static String CENTRE_REGISTRY_CENTRES_LIST_JSON_URL = "https://centres.clarin.eu/api/model/Centre";

    public ContributorsPage() {
        add(new ListView<EndpointProvider>("centresList", new EndpointProvidersModel()) {
            @Override
            protected void populateItem(ListItem<EndpointProvider> item) {
                item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));
                item.add(new ExternalLink("website", new PropertyModel<>(item.getModel(), "centreWebsiteUrl"))
                        .add(new Label("centreName"))
                );
            }

        });

    }

    private class EndpointProvidersModel extends LoadableDetachableModel<List<EndpointProvider>> {

        @Override
        protected List<EndpointProvider> load() {
            try {
                final CentreRegistryProvidersService providersService = new CentreRegistryProvidersService(CENTRE_REGISTRY_CENTRES_LIST_JSON_URL, CENTRE_REGISTRY_ENDPOINTS_LIST_JSON_URL);
                return providersService.retrieveCentreEndpoints();
            } catch (IOException ex) {
                error("Failed to retrieve endpoint information from centre registry:" + ex.getMessage());
                return Collections.emptyList();
            }
        }
    }
}
