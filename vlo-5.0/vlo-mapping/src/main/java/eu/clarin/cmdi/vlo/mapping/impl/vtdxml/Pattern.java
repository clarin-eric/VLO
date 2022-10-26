/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Menzo Windhouwer &lt;menzo.windhouwer@meertens.knaw.nl&gt;
 */
public class Pattern implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String pattern;

    //CLAVAS open/closed vocab
    // resolve @cmd:ValueConceptLink in <URI> get english prefLabel
    private Vocabulary vocab;
    //CMDI closed vocab
    // lookup value, resolve <URI> get english prefLabel
    //private Map<String,URI> vocabulary;

    public Pattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setVocabulary(Vocabulary vocab) {
        this.vocab = vocab;
    }

    public boolean hasVocabulary() {
        return (this.vocab != null);
    }

    public Vocabulary getVocabulary() {
        return this.vocab;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.pattern);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pattern other = (Pattern) obj;
        return Objects.equals(this.pattern, other.pattern);
    }

    @Override
    public String toString() {
        return String.valueOf(pattern);
    }

}
