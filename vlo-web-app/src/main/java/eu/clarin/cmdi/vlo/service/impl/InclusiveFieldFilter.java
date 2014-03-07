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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.FieldFilter;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author twagoo
 */
public class InclusiveFieldFilter implements FieldFilter, Serializable {

    private final Collection<String> includeFields;

    public InclusiveFieldFilter(Collection<String> technicalFields) {
        this.includeFields = technicalFields;
    }

    @Override
    public boolean allowField(String fieldName) {
        return includeFields.contains(fieldName);
    }

}
