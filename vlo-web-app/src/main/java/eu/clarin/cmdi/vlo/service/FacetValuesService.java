/*
 * Copyright (C) 2014 CLARIN
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

package eu.clarin.cmdi.vlo.service;

import eu.clarin.cmdi.vlo.pojo.Facet;
import eu.clarin.cmdi.vlo.pojo.FacetValue;
import eu.clarin.cmdi.vlo.service.impl.FacetValuesProvider;
import java.util.List;

/**
 *
 * @author twagoo
 */
public interface FacetValuesService {

    public List<FacetValue> getValues(Facet facet, String startsWith, FacetValuesProvider.FacetValuesSortProperty sort);
    public long getValueCount(Facet facet, String startsWith);
}
