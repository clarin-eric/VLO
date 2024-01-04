/*
 * Copyright (C) 2024 twagoo
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

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import eu.clarin.cmdi.vlo.api.service.FilterMapFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@Component
@Slf4j
@AllArgsConstructor
public class FilterMapFactoryImpl implements FilterMapFactory {

    protected final static Splitter FQ_SPLITTER = Splitter.on(':').limit(2);

    @Override
    public Map<String, ? extends Iterable<String>> createFilterMap(List<String> fq) {
        if (fq == null || fq.isEmpty()) {
            return Collections.emptyMap();
        } else {
            final ArrayListMultimap<String, String> map = ArrayListMultimap.<String, String>create();
            fq.forEach(fqVal -> {
                final List<String> fqDecomposed = FQ_SPLITTER.splitToList(fqVal);
                if (fqDecomposed.size() == 2) {
                    map.put(fqDecomposed.get(0), fqDecomposed.get(1));
                } else {
                    log.warn("Ignoring invalid fq parameter: {}", fq);
                }
            });

            return map.asMap();
        }

    }

}
