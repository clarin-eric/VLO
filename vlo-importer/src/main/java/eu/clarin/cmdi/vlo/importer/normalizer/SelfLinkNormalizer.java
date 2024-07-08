/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.PIDType;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Carries out the following normalizations to incoming values:
 * <ul>
 * <li>All handles (as in handle.net) to `hdl:{ID}`</li>
 * <li>All DOIs (as in doi.org) to `doi:{ID}`</li>
 * <li>Urls starting with `https://` to `http://`</li>
 * </ul>
 *
 * @author twagoo
 */
public class SelfLinkNormalizer extends AbstractPostNormalizer {

    @Override
    public List<String> process(String value, DocFieldContainer docFieldContainer) {
        return process(value)
                .map(ImmutableList::of)
                .orElseGet(() -> ImmutableList.of());

    }

    //process single value to single value
    private Optional<String> process(String selfLink) {
        return Optional.ofNullable(selfLink).map(this::normalize);
    }

    private String normalize(String selfLink) {
        if (PIDUtils.isPid(selfLink)) {
            final Optional<String> scheme = PIDUtils.getType(selfLink).flatMap(this::schemeForType);
            if (scheme.isPresent()) {
                return String.format("%s:%s", scheme.get(), PIDUtils.getSchemeSpecificId(selfLink));
            }
        } else {
            try {
                final URI uri = new URI(selfLink);
                final String scheme = uri.getScheme();
                if (scheme != null) {
                    switch (scheme.toLowerCase()) {
                        case "http", "https" -> {
                            return "http:" + uri.getSchemeSpecificPart();
                        }
                    }
                }
            } catch (URISyntaxException ex) {
                // if URI cannot be parsed, use the original value
                return selfLink;
            }
        }
        //no normalization options, return original value
        return selfLink;
    }

    private Optional<String> schemeForType(PIDType type) {
        return switch (type) {
            case DOI ->
                Optional.of("doi");
            case HANDLE ->
                Optional.of("hdl");
            default ->
                Optional.empty();
        };

    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

}
