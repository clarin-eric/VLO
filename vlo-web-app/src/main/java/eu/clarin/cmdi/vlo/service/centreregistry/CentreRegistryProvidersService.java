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
package eu.clarin.cmdi.vlo.service.centreregistry;

import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CentreRegistryProvidersService implements EndpointProvidersService {

    private final static Logger logger = LoggerFactory.getLogger(CentreRegistryProvidersService.class);

    private final String centresJsonUrl;
    private final String endpointsJsonUrl;

    private final AtomicReference<List<EndpointProvider>> lastResponse = new AtomicReference<>();

    public CentreRegistryProvidersService(VloConfig vloConfig) {
        this(vloConfig.getCentreRegistryCentresListJsonUrl(), vloConfig.getCentreRegistryOaiPmhEndpointsListJsonUrl());
    }

    public CentreRegistryProvidersService(String centresJsonUrl, String endpointsJsonUrl) {
        this.centresJsonUrl = centresJsonUrl;
        this.endpointsJsonUrl = endpointsJsonUrl;
    }

    @Override
    public List<EndpointProvider> retrieveCentreEndpoints() throws IOException {
        try {
            return lastResponse.updateAndGet(this::retrieveCentreEndpointsOrOld);
        } catch (Exception ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    private List<EndpointProvider> retrieveCentreEndpointsOrOld(List<EndpointProvider> old) throws RuntimeException {
        try {
            final List<EndpointProvider> endpoints = parseEndpoints();
            fillInCentreDetails(endpoints);
            endpoints.sort(Comparator.comparing(EndpointProvider::getCentreName));
            return endpoints;
        } catch (IOException ex) {
            // try to return last response
            if (old != null) {
                logger.warn("Error while retrieving new OAI-PMH endpoint list. Returning last response.", ex);
                return old;
            } else {
                logger.error("Error while retrieving new OAI-PMH endpoint list. No old response to return.", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    protected List<EndpointProvider> parseEndpoints() throws IOException {
        final URL url = new URL(endpointsJsonUrl);
        try (InputStream is = url.openStream()) {
            final JSONTokener tokener = new JSONTokener(is);
            final JSONArray jsonArray = new JSONArray(tokener);
            // make endpoint provider object for all endpoints
            final Map<Integer, List<EndpointProvider>> groupedByCentre = jsonArrayToObjectStream(jsonArray)
                    .map(o -> o.getJSONObject("fields"))
                    .map(o
                            -> new EndpointProvider()
                            .setKey(o.getInt("centre"))
                            .setEndpointUrl(Lists.newArrayList(o.getString("uri"))))
                    .collect(Collectors.groupingBy(EndpointProvider::getCentreKey));

            // merge all endpoint provider objects per centre
            return groupedByCentre.entrySet().stream()
                    .map(set -> {
                        //take first endpoint from group and combine all endpoint URLs
                        return set.getValue().get(0)
                                .setEndpointUrl(
                                        set.getValue().stream()
                                                .flatMap(e -> e.getEndpointUrls().stream())
                                                .collect(Collectors.toList())
                                );
                    })
                    .collect(Collectors.toList());
        }
    }

    protected void fillInCentreDetails(List<EndpointProvider> providers) throws IOException {
        Collection<Integer> centreIds = providers.stream().map(EndpointProvider::getCentreKey).collect(Collectors.toSet());

        final URL url = new URL(centresJsonUrl);
        try (InputStream is = url.openStream()) {
            final JSONTokener tokener = new JSONTokener(is);
            final JSONArray jsonArray = new JSONArray(tokener);
            final Stream<JSONObject> centres = jsonArrayToObjectStream(jsonArray)
                    .filter(o -> o.has("pk") && centreIds.contains(o.getInt("pk")));
            centres.forEach(o -> {
                final Integer centreKey = o.getInt("pk");
                final JSONObject fields = o.getJSONObject("fields");

                providers.stream()
                        .filter(e -> e.getCentreKey().equals(centreKey))
                        .forEach(e -> {
                            e.setName(fields.getString("name"));
                            e.setWebsiteUrl(fields.getString("website_url"));
                        });
            });
        }
    }

    /**
     *
     * @param jsonArray
     * @return stream of objects contained (as direct children) in provided
     * JSONArray
     */
    private static Stream<JSONObject> jsonArrayToObjectStream(JSONArray jsonArray) {
        final Stream<Object> objStream = StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.length(), Spliterator.SIZED), false);
        return objStream.filter(o -> o instanceof JSONObject)
                .map(o -> (JSONObject) o);
    }

}
