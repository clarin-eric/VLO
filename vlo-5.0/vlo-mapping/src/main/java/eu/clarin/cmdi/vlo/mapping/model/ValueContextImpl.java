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
package eu.clarin.cmdi.vlo.mapping.model;

import eu.clarin.cmdi.vlo.mapping.impl.vtdxml.Vocabulary;
import java.util.Collection;
import lombok.ToString;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ToString
public class ValueContextImpl implements ValueContext {

    private final Context context;

    private Collection<ValueLanguagePair> values;

    public ValueContextImpl(Context context) {
        this.context = context;
    }

    public ValueContextImpl(Context context, Collection<ValueLanguagePair> values) {
        this.context = context;
        this.values = values;
    }

    @Override
    public Collection<ValueLanguagePair> getValues() {
        return values;
    }

    public void setValues(Collection<ValueLanguagePair> values) {
        this.values = values;
    }

    @Override
    public Collection<String> getConceptPath() {
        return context.getConceptPath();
    }

    @Override
    public Vocabulary getVocabulary() {
        return context.getVocabulary();
    }

    @Override
    public String getXpath() {
        return context.getXpath();
    }

    public static ValueContext fromContext(Context base, Collection<ValueLanguagePair> values) {
        return new ValueContextImpl(base, values);
    }

}
