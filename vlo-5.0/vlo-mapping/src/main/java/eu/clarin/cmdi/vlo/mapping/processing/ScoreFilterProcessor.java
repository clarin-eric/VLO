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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import static java.util.stream.Collectors.teeing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.maxBy;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter and sort results based on score.
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
@XmlAccessorType(XmlAccessType.FIELD)
public class ScoreFilterProcessor extends FieldFilteredProcessor {

    private final static Comparator<FieldMappingResult> scoreComparator
            = Comparator.comparingInt(FieldMappingResult::getScore);

    @XmlAttribute(required = false)
    private Integer keepTop = null;

    @XmlAttribute(required = false)
    private boolean keepHighestScoring = false;

    @XmlAttribute(required = false)
    private Integer minScore = null;

    @XmlAttribute(required = false)
    private Integer maxScore = null;

    @Override
    protected Collection<ValueLanguagePair> processForMatchedField(List<FieldMappingResult> results) {
        Stream<FieldMappingResult> stream = results.stream();

        if (minScore != null || maxScore != null) {
            stream = applyRangeFilter(stream, minScore, maxScore);
        }

        if (keepHighestScoring) {
            stream = keepHighestScoring(stream);
        }

        if (keepTop != null) {
            stream = keepTop(stream, keepTop);
        }

        return stream.flatMap(r -> r.getValues().stream()).toList();
    }

    private static Stream<FieldMappingResult> applyRangeFilter(Stream<FieldMappingResult> stream, Integer minScore, Integer maxScore) {
        if (minScore != null) {
            stream = stream.filter(r -> r.getScore() >= minScore);
        }
        if (maxScore != null) {
            stream = stream.filter(r -> r.getScore() <= maxScore);
        }
        return stream;
    }

    private static Stream<FieldMappingResult> keepHighestScoring(Stream<FieldMappingResult> stream) {
        return stream.collect(teeing(
                // get result with maximal score
                maxBy(scoreComparator),
                // simultaneously materialize results and produce a new stream
                collectingAndThen(toUnmodifiableList(), List::stream),
                // apply max value (if present) as a filter to the new stream
                (max, stream2) -> max
                        .map(maxResult -> stream2.filter(r -> r.getScore() == maxResult.getScore()))
                        // there is no max value, so re-stream unfiltered
                        .orElse(stream2)));
    }

    private static Stream<FieldMappingResult> keepTop(Stream<FieldMappingResult> stream, int keepTop) {
        return stream
                .sorted(scoreComparator.reversed())
                .limit(keepTop);
    }

}
