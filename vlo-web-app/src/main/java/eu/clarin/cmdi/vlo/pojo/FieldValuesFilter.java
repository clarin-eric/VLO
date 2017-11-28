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

import java.io.Serializable;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public interface FieldValuesFilter extends Serializable {

    /**
     *
     * @param count count (name + count) to check
     * @param converter optional converter to apply to value before comparison
     * @return true IFF the {@link Count#getCount() } is more than {@link #getMinimalOccurence()
     * } {@link Count#getName() } contains {@link #getName() } (case
     * insensitive)
     */
    boolean matches(FacetField.Count count, IConverter<String> converter);
    
    boolean isEmpty();
    
    FieldValuesFilter copy();
    
}
