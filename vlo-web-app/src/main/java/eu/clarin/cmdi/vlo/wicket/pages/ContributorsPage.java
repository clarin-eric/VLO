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

import com.google.common.base.Strings;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.service.centreregistry.EndpointProvider;
import eu.clarin.cmdi.vlo.service.centreregistry.EndpointProvidersService;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class ContributorsPage extends VloBasePage {

    private final static Logger logger = LoggerFactory.getLogger(ContributorsPage.class);

    @SpringBean
    private EndpointProvidersService providersService;
    @SpringBean
    private VloConfig vloConfig;

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

        final String otherProvidersListFile = vloConfig.getOtherProvidersMarkupFile();

        if (!Strings.isNullOrEmpty(otherProvidersListFile) && new File(otherProvidersListFile).canRead()) {
            add(new Include("othersList", "file://" + otherProvidersListFile));
        } else {
            logger.warn("Could not load list of 'other' metadata providers from {}", otherProvidersListFile);
            error("List of other metadata providers could not be loaded");
            add(new Label("othersList", ""));
        }

    }

    private class EndpointProvidersModel extends LoadableDetachableModel<List<EndpointProvider>> {

        @Override
        protected List<EndpointProvider> load() {
            try {
                return providersService.retrieveCentreEndpoints();
            } catch (IOException ex) {
                logger.error("Failed to retrieve endpoint information from centre registry", ex);
                error("Failed to retrieve endpoint information from the CLARIN Centre Registry");
                return Collections.emptyList();
            }
        }
    }
}
