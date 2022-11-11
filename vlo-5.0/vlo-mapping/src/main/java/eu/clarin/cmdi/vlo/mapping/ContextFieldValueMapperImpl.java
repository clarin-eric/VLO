/*
 * Copyright (C) 2022 CLARIN
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

import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.definition.MappingRule;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Rule definitions - {1..*} Context assertion - {1..*} Transformations
 *
 * Examples of rules
 */
/**
 * Responsible for mapping value contexts from metadata records to field values
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class ContextFieldValueMapperImpl implements ContextFieldValueMapper {

    private final Iterable<MappingRule> rules;
    private final VloMappingConfiguration mappingConfig;

    public ContextFieldValueMapperImpl(Iterable<MappingRule> rules, VloMappingConfiguration mappingConfig) {
        this.rules = rules;
        this.mappingConfig = mappingConfig;
    }

    @Override
    public Stream<FieldMappingResult> mapContext(ValueContext context) {
        log.trace("Mapping value context {}", context);
        final Stream.Builder<Stream<FieldMappingResult>> builder = Stream.builder();
        
        for (MappingRule rule : rules) {
            if (rule.applies(context)) {
                builder.add(rule
                        .getTransformerStream().map(
                                t -> new FieldMappingResult(
                                        t.getTargetField(),
                                        context,
                                        t.apply(context, mappingConfig).collect(Collectors.toList()))));
                if (rule.isTerminal()) {
                    break;
                }
            }
        }

        return builder.build().flatMap(identity());
    }

}
