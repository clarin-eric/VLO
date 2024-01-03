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
package eu.clarin.cmdi.vlo.api.service.impl;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.api.service.FieldValueLabelService;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
public class FieldValueLabelServiceImpl implements FieldValueLabelService {

    private final Map<String, Function<String, String>> labelFunctionsMap;
    private final static Function<String, String> NULL_FUNCTION = (s) -> null;

    public FieldValueLabelServiceImpl(Map<String, Function<String, String>> labelFunctionsMap) {
        this.labelFunctionsMap = labelFunctionsMap;
    }

    @Override
    public String getLabelFor(String field, String value) {
        return labelFunctionsMap.getOrDefault(field, NULL_FUNCTION).apply(value);
    }

    private static String labelForLanguageCode(String value) {
        return value;
    }

    @AllArgsConstructor
    public static class PatternMatchingLabelFunction implements Function<String, String> {

        private final Pattern pattern;
        private final Integer targetGroup;

        public PatternMatchingLabelFunction(String pattern, int targetGroup) {
            this(Pattern.compile(pattern), targetGroup);
        }

        @Override
        public String apply(String value) {
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                return matcher.group(targetGroup);
            } else {
                return null;
            }
        }

    }

}
