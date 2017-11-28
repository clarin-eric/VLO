/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.pojo;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.util.convert.IConverter;

/**
 * Values filter that only lets through values from a provided fixed set
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FixedSetFieldValuesFilter implements FieldValuesFilter, Serializable {

    private final List<String> values;

    /**
     *
     * @param values values to allow - a shallow copy will be used, so changes
     * to the original collection will not affect the filtering
     */
    public FixedSetFieldValuesFilter(Collection<String> values) {
        this.values = new ArrayList<>(values); //make sure list is serializable
    }

    @Override
    public boolean matches(FacetField.Count count, IConverter<String> converter) {
        return values.contains(count.getName());
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public FieldValuesFilter copy() {
        return new FixedSetFieldValuesFilter(Lists.newArrayList(values));
    }

}


