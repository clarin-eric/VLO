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
import org.springframework.core.convert.converter.Converter;

/**
 * Service that evaluates configured display conditions for a query/selection
 * state and its results
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetConditionEvaluationServiceImpl implements FacetConditionEvaluationService {

    private final Map<String, FacetDisplayCondition> conditionsMap;

    public FacetConditionEvaluationServiceImpl(FacetsConfiguration config) {
        this(new FacetsConfigurationConditionsConverter(), config);
    }

    public FacetConditionEvaluationServiceImpl(Converter<FacetsConfiguration, Map<String, FacetDisplayCondition>> converter, FacetsConfiguration config) {
        conditionsMap = converter.convert(config);
    }

    /**
     *
     * @param facet facet to evaluate condition for
     * @param selection selection to evaluate condition for (together with
     * facetFields)
     * @param facetFields facet state to evaluate condition for (together with
     * selection)
     * @return
     */
    @Override
    public boolean shouldShow(String facet, QueryFacetsSelection selection, List<FacetField> facetFields) {
        // get condition for the specified facet and evaluate again selection / results
        return conditionsMap
                .getOrDefault(facet, getDefaultCondition()) // most facets will have no condition, fall back to default
                .evaluate(selection, facetFields);
    }

    /**
     * Default condition to evaluate if no condition is 'registered' for a given
     * facet
     *
     * @return default condition
     */
    protected FacetDisplayCondition getDefaultCondition() {
        return (selection, facetFields) -> true;
    }

    /**
     * Condition that can be evaluated for a selection / facet fields result
     * state
     */
    @FunctionalInterface
    public static interface FacetDisplayCondition {

        boolean evaluate(QueryFacetsSelection selection, List<FacetField> facetFields);
    }

    /**
     * Converter
     */
    public static class FacetsConfigurationConditionsConverter implements Converter<FacetsConfiguration, Map<String, FacetDisplayCondition>> {

        @Override
        public Map<String, FacetDisplayCondition> convert(FacetsConfiguration source) {
            return createConditionsMap(source);
        }

        /**
         * Create a map with conditions for all facets that have one defined in
         * the provided configuration. At most one condition will be created per
         * facet, combining multiple conditions into a single condition where
         * necessary.
         *
         * @param config facets configuration to convert into conditions
         * @return facet -> condition map
         */
        private Map<String, FacetDisplayCondition> createConditionsMap(FacetsConfiguration config) {
            final ImmutableMap.Builder<String, FacetDisplayCondition> mapBuilder = ImmutableMap.builder();

            // All facets can have their own (composite) conditions configured independently. 
            config.getFacet().forEach(
                    facet -> conditionsConfigToCondition(facet.getConditions())
                            .ifPresent(condition -> mapBuilder.put(facet.getName(), condition))
            );

            return mapBuilder.build();
        }

        /**
         * Converts a set of conditions for a single facet into a display
         * condition if applicable
         *
         * @param conditions conditions from configuration to (try to) convert
         * into a facet display condition
         * @return empty iff no conditions are defined
         */
        private Optional<FacetDisplayCondition> conditionsConfigToCondition(List<Conditions> conditions) {
            if (conditions == null || conditions.isEmpty()) {
                // no conditions configured -> no facet display condition
                return Optional.empty();
            } else if (conditions.size() == 1) {
                // one condition -> simple condition (no composite)
                final List<Condition> condition
                        = conditions.get(0).getCondition();
                // convert
                final FacetDisplayCondition facetDisplayCondition
                        = createAndCondition(condition);
                // wrap
                return Optional.of(facetDisplayCondition);
            } else {
                // multiple conditions -> any matched condition should cause 
                // facet to show, therefore combine as OR

                //convert each condition
                final List<FacetDisplayCondition> facetDisplayConditions
                        = conditions
                                .stream()
                                .map(c -> c.getCondition())
                                .map(this::createAndCondition)
                                .collect(Collectors.toList());

                // make OR composite
                final FacetDisplayCondition composite
                        = createOrCondition(facetDisplayConditions);

                // wrap
                return Optional.of(composite);
            }
        }

        /**
         * Creates and AND condition for all requirements within a single
         * condition
         *
         * @param conditionConfig configuration to create display condition for
         * @return single display condition for all requirements within a
         * condition
         */
        private FacetDisplayCondition createAndCondition(List<Condition> conditionConfig) {
            // map all individual requirements within a condition to a facet
            // display condition
            final List<FacetDisplayCondition> conditions = ImmutableList.copyOf(
                    conditionConfig
                            .stream()
                            .map(this::conditionConfigToCondition)
                            .collect(Collectors.toList()));

            // combine into single condition that combines all requirements (all
            // should match)
            final FacetDisplayCondition facetDisplayCondition
                    = (selection, facetFields) -> conditions
                            .stream()
                            .allMatch(condition -> condition.evaluate(selection, facetFields));

            return facetDisplayCondition;
        }

        /**
         * Creates a single display condition that evaluates as true if at least
         * one of the provided display conditions match
         *
         * @param displayConditions conditions to combine into OR condition
         * @return condition that evaluates as true if at least one of the
         * wrapped display conditions match
         */
        private FacetDisplayCondition createOrCondition(List<FacetDisplayCondition> displayConditions) {
            return (selection, facetFields) -> displayConditions
                    .stream()
                    .anyMatch(condition -> condition.evaluate(selection, facetFields));
        }

        /**
         * Makes a display condition for a single requirement within a
         * potentially more complex condition
         *
         * @param conditionConfiguration condition definition to convert
         * @return facet display condition derived from the configuration
         */
        private FacetDisplayCondition conditionConfigToCondition(Condition conditionConfiguration) {
            if (conditionConfiguration.getFacetCondition() != null) {
                // Condition is a "facet condition", i.e. requires a certain
                // selection or result state for a given (other) facet
                return createFacetCondition(conditionConfiguration.getFacetCondition());
            }
            // TODO: Add other types?
            throw new RuntimeException("Condition missing in definition");
        }

        /**
         * Makes a display condition for a "facet condition" definition
         *
         * @param definition definition to implement as a condition
         * @return display condition that can be evaluated
         */
        private FacetDisplayCondition createFacetCondition(FacetCondition definition) {
            final String facet = definition.getFacetName();
            switch (definition.getSelection().getType()) {
                case "anyValue":
                    // any selected value matches
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        return selectedValues != null
                                && !selectedValues.getValues().isEmpty();
                    };
                case "anyOf":
                    // if any of the specified values is selected, there is a match
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        final List<String> targetValues = definition.getSelection().getValue();
                        return selectedValues != null
                                && targetValues.stream().anyMatch(
                                        value -> selectedValues.getValues().contains(value));
                    };
                case "allOf":
                    // all of the specified values must be selected for there to be a match
                    return (actualSelection, actualFacets) -> {
                        final FacetSelection selectedValues = actualSelection.getSelectionValues(facet);
                        final List<String> targetValues = definition.getSelection().getValue();
                        return selectedValues != null
                                && targetValues.stream().allMatch(
                                        value -> selectedValues.getValues().contains(value));
                    };
                default:
                    throw new RuntimeException("Unsupported facet conditiont type: " + definition.getSelection().getType());
            }
        }
    }

}
