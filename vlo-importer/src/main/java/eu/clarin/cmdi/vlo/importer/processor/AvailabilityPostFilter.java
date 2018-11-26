/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer.processor;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In case that Availability facet has more then one value use the most
 * restrictive tag from PUB, ACA and RES.
 */
public class AvailabilityPostFilter implements FacetValuesMapFilter {

    private final static Logger LOG = LoggerFactory.getLogger(AvailabilityPostFilter.class);

    private final List<String> fields;

    public AvailabilityPostFilter(String... field) {
        this(ImmutableList.copyOf(field));
    }

    public AvailabilityPostFilter(List<String> fields) {
        this.fields = fields;
        LOG.debug("Post-filtering fields {}", fields);
    }

    @Override
    public void filter(FacetValuesMap map) {
        fields.forEach(field -> reduceAvailabilityForField(map, field));
    }

    private void reduceAvailabilityForField(FacetValuesMap map, String fieldName) {
        final List<ValueSet> values = map.get(fieldName);
        if (values != null) {
            final List<ValueSet> reduced = reduceAvailability(values);
            map.put(fieldName, reduced);

            LOG.trace("Reduced values for {}: {}->{}", fieldName, values, reduced);
        }
    }

    private List<ValueSet> reduceAvailability(List<ValueSet> valueSets) {
        if (valueSets.isEmpty()) {
            return valueSets;
        }

        // find 'other tags'
        final Stream<ValueSet> otherTags = valueSets.stream()
                .filter((v) -> availabilityToLvl(v) == -1);

        // find known levels
        final Stream<ValueSet> highestLevel = valueSets.stream()
                .filter((v) -> availabilityToLvl(v) > 0)
                // keep highest
                .max((vs1, vs2) -> availabilityToLvl(vs1) - availabilityToLvl(vs2))
                // map to stream so that we can concatenate
                .map(Stream::of).orElseGet(Stream::empty);

        return Stream.concat(highestLevel, otherTags)
                .collect(Collectors.toList());
    }

    private int availabilityToLvl(ValueSet vs) {
        final String availabilty = vs.getValue();
        if (availabilty == null) {
            return 0;
        }
        switch (availabilty) {
            case FacetConstants.AVAILABILITY_LEVEL_PUB:
                return 1;
            case FacetConstants.AVAILABILITY_LEVEL_ACA:
                return 2;
            case FacetConstants.AVAILABILITY_LEVEL_RES:
                return 3;
            default:
                return -1; // other tags
        }
    }

}
