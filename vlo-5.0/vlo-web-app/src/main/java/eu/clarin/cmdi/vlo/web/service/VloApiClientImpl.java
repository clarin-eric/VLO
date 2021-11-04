/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.web.service;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.START_PARAMETER;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
@AllArgsConstructor
public class VloApiClientImpl implements VloApiClient {

    @Qualifier("vloApiRestTemplate")
    private final RestTemplate restTemplate;

    private final UriBuilderFactory uriBuilderFactory;

    @Override
    public List<VloRecord> getRecords(String q, Long rows, Long start) {
        log.debug("Getting records");

        final UriBuilder uriBuilder = uriBuilderFactory.builder().path(RECORDS_PATH);

        if (q != null) {
            uriBuilder.queryParam(QUERY_PARAMETER, q);
        }
        if (rows != null) {
            uriBuilder.queryParam(ROWS_PARAMETER, rows);
        }
        if (start != null) {
            uriBuilder.queryParam(START_PARAMETER, start);
        }

        final RequestEntity requestEntity = RequestEntity
                .get(uriBuilder.build(true))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        final ResponseEntity<VloRecord[]> response = restTemplate.exchange(requestEntity, VloRecord[].class);
        //TODO: handle errors
        return Arrays.asList(response.getBody());
    }
}
