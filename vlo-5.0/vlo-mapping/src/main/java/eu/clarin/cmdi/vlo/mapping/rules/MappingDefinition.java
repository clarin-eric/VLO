/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.rules;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MappingDefinition {

    @XmlElement(name = "contextAssertionBasedRule")
    private List<ContextAssertionBasedRule> rules;

    public MappingDefinition() {
    }

    public List<ContextAssertionBasedRule> getRules() {
        return rules;
    }

    public void setRules(List<ContextAssertionBasedRule> rules) {
        this.rules = rules;
    }

}
