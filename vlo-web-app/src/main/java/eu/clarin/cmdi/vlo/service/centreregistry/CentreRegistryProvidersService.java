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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CentreRegistryProvidersService {

    private final String centresJsonUrl;
    private final String endpointsJsonUrl;

    public CentreRegistryProvidersService(String centresJsonUrl, String endpointsJsonUrl) {
        this.centresJsonUrl = centresJsonUrl;
        this.endpointsJsonUrl = endpointsJsonUrl;
    }

    public List<EndpointProvider> retrieveCentreEndpoints() throws IOException, ParseException {
        final List<EndpointProvider> endpoints = parseEndpoints();
        fillInCentreDetails(endpoints);
        return endpoints;
    }

    protected List<EndpointProvider> parseEndpoints() throws MalformedURLException, IOException, ParseException {
        final URL url = new URL(endpointsJsonUrl);
        try (InputStream is = url.openStream()) {
            final JSONTokener tokener = new JSONTokener(is);
            final JSONArray jsonArray = new JSONArray(tokener);
            return jsonArrayToObjectStream(jsonArray)
                    .map(o -> o.getJSONObject("fields"))
                    .map(o
                            -> new EndpointProvider()
                            .setKey(o.getInt("centre"))
                            .setEndpointUrl(o.getString("uri")))
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

    public static class EndpointProvider {

        private String endpointUrl;
        private String centreName;
        private String centreWebsiteUrl;
        private Integer centreKey;

        public Integer getCentreKey() {
            return centreKey;
        }

        public EndpointProvider setKey(Integer key) {
            this.centreKey = key;
            return this;
        }

        public String getCentreName() {
            return centreName;
        }

        public EndpointProvider setName(String name) {
            this.centreName = name;
            return this;
        }

        public String getCentreWebsiteUrl() {
            return centreWebsiteUrl;
        }

        public EndpointProvider setWebsiteUrl(String websiteUrl) {
            this.centreWebsiteUrl = websiteUrl;
            return this;
        }

        public String getEndpointUrl() {
            return endpointUrl;
        }

        public EndpointProvider setEndpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }
    }
}
