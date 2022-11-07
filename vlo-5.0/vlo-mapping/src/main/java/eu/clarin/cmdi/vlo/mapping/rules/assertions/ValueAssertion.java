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
package eu.clarin.cmdi.vlo.mapping.rules.assertions;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlRootElement
public class ValueAssertion extends ContextAssertion {

    @XmlValue
    @Getter
    private String target;

    @XmlAttribute
    @Getter
    private Boolean regex;

    private Boolean caseSensitive;

    @XmlAttribute(name = "lang")
    @Getter
    private String language;

    @XmlTransient
    private Supplier<Pattern> pattern;
    @XmlTransient
    private BiPredicate<String, String> valueEqualityCheck;

    public ValueAssertion() {
        // for deserialization
    }
    
    public ValueAssertion(String target, Boolean regex, Boolean caseSenstive) {
        this(target, regex, caseSenstive, null);
    }

    public ValueAssertion(String target, Boolean regex, Boolean caseSenstive, String language) {
        this.target = target;
        this.regex = regex;
        this.language = language;

        setCaseSensitive(caseSenstive);
    }

    @Override
    public Boolean evaluate(ValueContext context) {
        return Iterables.tryFind(context.getValues(), this::evaluateValue).isPresent();
    }

    private boolean evaluateValue(ValueLanguagePair value) {
        if (value == null) {
            // we cannot match null; see Iterables.tryFind
            return false;
        } else {
            return valueMatch(value) && languageMatch(value);
        }
    }

    private boolean valueMatch(ValueLanguagePair value) {
        if (regex) {
            return regexMatch(value);
        } else {
            return valueEqualityCheck.test(target, value.getValue());
        }
    }

    private boolean regexMatch(ValueLanguagePair value) {
        return pattern.get().matcher(value.getValue()).matches();
    }

    private boolean languageMatch(ValueLanguagePair value) {
        if (language == null) {
            // language is optional; if not set this means no check is needed
            return true;
        } else {
            return language.equalsIgnoreCase(value.getLanguage());
        }
    }

    @XmlAttribute(name="caseSensitive")
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public final void setCaseSensitive(boolean caseSenstive) {
        this.caseSensitive = caseSenstive;
        if (caseSenstive) {
            this.valueEqualityCheck = (s, v) -> s.equals(v);
            this.pattern = Suppliers.memoize(() -> Pattern.compile(target));
        } else {
            this.valueEqualityCheck = (s, v) -> s.equalsIgnoreCase(v);
            this.pattern = Suppliers.memoize(() -> Pattern.compile(target, Pattern.CASE_INSENSITIVE));
        }
    }

}
