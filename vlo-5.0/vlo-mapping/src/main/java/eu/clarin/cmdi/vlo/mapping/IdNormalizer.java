/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.mapping;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author twagoo
 */
public class IdNormalizer {

    private static final Set<Integer> RESERVED_CHARACTERS = ImmutableSet.of(
            '!', '*', '\'', '(', ')', ';', ':', '@', '&',
            '=', '+', '$', ',', '/', '?', '#', '[', ']', ' '
    ).stream().map(c -> (int) c).collect(Collectors.toUnmodifiableSet());

    /**
     * Return normalized String where all reserved characters in URL encoding
     * are replaced by their ASCII code (in underscores)
     *
     * @param idString String that will be normalized
     * @return normalized version of value where all reserved characters in URL
     * encoding are replaced by their ASCII code
     */
    public String normalizeIdString(String idString) {
        final StringBuilder normalizedString = new StringBuilder();
        idString.trim()
                .chars()
                .forEach(charInt -> {
                    if (RESERVED_CHARACTERS.contains(charInt)) {
                        normalizedString.append("_").append(charInt).append("_");
                    } else {
                        normalizedString.append((char) charInt);
                    }
                });
        return normalizedString.toString();
    }
}
