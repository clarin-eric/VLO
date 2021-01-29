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

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetConditionEvaluationService;
import java.util.Map;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetConditionEvaluationServiceImpl implements FacetConditionEvaluationService {

    private final static FacetDisplayCondition DEFAULT_CONDITION = (QueryFacetsSelection selection) -> true;
    
    private final Map<String, FacetDisplayCondition> conditionsMap;

    public FacetConditionEvaluationServiceImpl(FacetsConfiguration config) {
        conditionsMap = createConditionsMap(config);
    }

    @Override
    public boolean shouldShow(String facet, QueryFacetsSelection selection) {
        final FacetDisplayCondition condition = conditionsMap.getOrDefault(facet, DEFAULT_CONDITION);
        return condition.evaluate(selection);
    }

    private Map<String, FacetDisplayCondition> createConditionsMap(FacetsConfiguration config) {
        //TODO: generate conditions map based on config
        return ImmutableMap.of();
    }

    public static interface FacetDisplayCondition {

        boolean evaluate(QueryFacetsSelection selection);
    }

}
