/*
 * Copyright (C) 2022 twagoo
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
package eu.clarin.cmdi.vlo.mapping.processing;

import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter and sort results based on score.
 *
 * TODO: keep all values with max score
 *
 * TODO: absolute range limit
 *
 * @author twagoo
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@XmlRootElement
public class ScoreFilterProcessor extends FieldFilteredProcessor {

    private final static Comparator<FieldMappingResult> scoreComparator
            = Comparator.comparingInt(FieldMappingResult::getScore);

    @XmlElement(nillable = true)
    private Integer keepTop = null;

    @XmlElement
    private boolean keepHighestScoring = false;

    @Override
    protected Collection<ValueLanguagePair> processForMatchedField(List<FieldMappingResult> results) {
        Stream<FieldMappingResult> stream = results
                .stream();

        if (keepHighestScoring) {
            final Stream<FieldMappingResult> oldStream = stream;
            stream = results.stream()
                    // get maximal value
                    .max(scoreComparator)
                    // filter stream by value if maximum found
                    .map(maxResult -> oldStream.filter(r -> r.getScore() == maxResult.getScore()))
                    // if not keep old stream
                    .orElse(stream);
        }

        stream = stream.sorted(scoreComparator.reversed());

        if (keepTop != null) {
            stream = stream.limit(keepTop);
        }

        return stream.flatMap(r -> r.getValues().stream()).toList();
    }

}
