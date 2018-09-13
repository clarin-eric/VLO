/*
 * Copyright (C) 2016 CLARIN
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

/**
 *
 * @author Thomas Eckart
 */
public class NamePostNormalizer extends AbstractPostNormalizer {

    // omit leading and trailing quote characters if they are equal
    private static final Pattern OMIT_QUOTES_PATTERN = Pattern.compile("^([\"\'“])(.*)\\1$");

    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        Matcher nameMatcher = OMIT_QUOTES_PATTERN.matcher(value);
        List<String> resultList = new ArrayList<>();

        if (nameMatcher.matches()) {
            resultList.add(nameMatcher.group(2));
        } else {
            resultList.add(value);
        }

        return resultList;
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

}
