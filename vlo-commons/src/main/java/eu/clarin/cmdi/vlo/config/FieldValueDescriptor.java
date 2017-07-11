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
package eu.clarin.cmdi.vlo.config;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Twan Goosen &lt;twan.goosen@mpi.nl&gt;
 */
public class FieldValueDescriptor implements Serializable {

    private String value;
    private String displayValue;
    private String description;

    public FieldValueDescriptor() {
    }

    public FieldValueDescriptor(String value, String displayValue, String description) {
        this.value = value;
        this.displayValue = displayValue;
        this.description = description;
    }

    /**
     *
     * Get the value of value
     *
     * @return the value of value
     */
    @XmlAttribute
    public String getValue() {
        return value;
    }

    /**
     * Set the value of value
     *
     * @param value new value of value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the value of displayValue
     *
     * @return the value of displayValue
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Set the value of displayValue
     *
     * @param displayValue new value of displayValue
     */
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Get the value of description
     *
     * @return the value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the value of description
     *
     * @param description new value of description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Creates a value to descriptor map
     *
     * @param descriptor
     * @return {@link FieldValueDescriptor} map with keys taken from {@link FieldValueDescriptor#getValue()
     * }
     */
    public static Map<String, FieldValueDescriptor> toMap(Collection<FieldValueDescriptor> descriptor) {
        return Maps.uniqueIndex(descriptor, new Function<FieldValueDescriptor, String>() {
            public String apply(FieldValueDescriptor f) {
                return f.getValue();
            }
        });
    }

    /**
     * Creates list of values
     *
     * @param descriptors descriptors to extract values from
     * @return list of values obtained via
     * {@link FieldValueDescriptor#getValue()}
     */
    public static List<String> valuesList(List<FieldValueDescriptor> descriptors) {
        return Lists.newArrayList(Lists.transform(descriptors, new Function<FieldValueDescriptor, String>() {
            @Override
            public String apply(FieldValueDescriptor input) {
                //we are only interested in the value
                return input.getValue();
            }
        }));
    }

}
