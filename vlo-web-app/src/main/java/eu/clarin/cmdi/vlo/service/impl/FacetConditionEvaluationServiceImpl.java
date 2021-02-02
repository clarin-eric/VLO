/*
 * Copyright (C) 2021 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.facets.configuration.Condition;
import eu.clarin.cmdi.vlo.facets.configuration.Conditions;
import eu.clarin.cmdi.vlo.facets.configuration.FacetCondition;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetConditionEvaluationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.response.FacetField;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetConditionEvaluationServiceImpl implements FacetConditionEvaluationService {

    private final static FacetDisplayCondition DEFAULT_CONDITION = (selection, facetFields) -> true;

    private final Map<String, FacetDisplayCondition> conditionsMap;

    public FacetConditionEvaluationServiceImpl(FacetsConfiguration config) {
        this(new FacetsConfigurationConditionsConverter(), config);
    }

    public FacetConditionEvaluationServiceImpl(FacetsConfigurationConditionsConverter converter, FacetsConfiguration config) {
        conditionsMap = converter.createConditionsMap(config);
    }

    @Override
    public boolean shouldShow(String facet, QueryFacetsSelection selection, List<FacetField> facetFields) {
        final FacetDisplayCondition condition = conditionsMap.getOrDefault(facet, DEFAULT_CONDITION);
        return condition.evaluate(selection, facetFields);
    }

    public static interface FacetDisplayCondition {

        boolean evaluate(QueryFacetsSelection selection, List<FacetField> facetFields);
    }

    public static class FacetsConfigurationConditionsConverter {

        public Map<String, FacetDisplayCondition> createConditionsMap(FacetsConfiguration config) {
            final ImmutableMap.Builder<String, FacetDisplayCondition> mapBuilder = ImmutableMap.builder();

            config.getFacet().forEach(
                    facet -> conditionsConfigToCondition(facet.getConditions())
                            .ifPresent(condition -> mapBuilder.put(facet.getName(), condition))
            );

            return mapBuilder.build();
        }

        private Optional<FacetDisplayCondition> conditionsConfigToCondition(List<Conditions> conditions) {
            if (conditions.isEmpty()) {
                return Optional.empty();
            } else if (conditions.size() == 1) {
                return Optional.of(createAndCondition(conditions.get(0).getCondition()));
            } else {
                return Optional.of(createOrCondition(
                        conditions
                                .stream()
                                .map(c -> c.getCondition())
                                .map(this::createAndCondition)
                                .collect(Collectors.toList())));
            }
        }

        private FacetDisplayCondition createAndCondition(List<Condition> conditionsConfig) {
            final List<FacetDisplayCondition> conditions = ImmutableList.copyOf(
                    conditionsConfig
                            .stream()
                            .map(this::conditionConfigToCondition)
                            .collect(Collectors.toList()));

            return (selection, facetFields) -> conditions
                    .stream()
                    .allMatch(condition -> condition.evaluate(selection, facetFields));
        }

        private FacetDisplayCondition createOrCondition(List<FacetDisplayCondition> collect) {
            return (selection, facetFields) -> collect
                    .stream()
                    .anyMatch(condition -> condition.evaluate(selection, facetFields));
        }

        private FacetDisplayCondition conditionConfigToCondition(Condition condition) {
            if (condition.getFacetCondition() != null) {
                return createFacetCondition(condition.getFacetCondition());
            }
            // Add other types?
            throw new RuntimeException("Condition missing in definition");
        }

        private FacetDisplayCondition createFacetCondition(FacetCondition condition) {
            final String facet = condition.getFacetName();
            switch (condition.getSelection().getType()) {
                case "anyValue":
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        return selectedValues != null
                                && !selectedValues.getValues().isEmpty();
                    };
                case "anyOf":
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        final List<String> targetValues = condition.getSelection().getValue();
                        return selectedValues != null
                                && targetValues.stream().anyMatch(
                                        value -> selectedValues.getValues().contains(value)
                                );
                    };
                case "allOf":
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        final List<String> targetValues = condition.getSelection().getValue();
                        return selectedValues != null
                                && targetValues.stream().allMatch(
                                        value -> selectedValues.getValues().contains(value)
                                );
                    };
                default:
                    throw new RuntimeException("Unsupported facet conditiont type: " + condition.getSelection().getType());
            }
        }
    }

}
