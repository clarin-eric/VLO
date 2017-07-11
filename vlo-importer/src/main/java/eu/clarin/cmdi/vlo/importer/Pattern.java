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
package eu.clarin.cmdi.vlo.importer;

/**
 *
 * @author Menzo Windhouwer &lt;menzo.windhouwer@meertens.knaw.nl&gt;
 */
public class Pattern {
    
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
        return (this.vocab!=null);
    }
    
    public Vocabulary getVocabulary() {
        return this.vocab;
    }
    
    @Override
    public int hashCode() {
        if (this.pattern!=null)
            return this.pattern.hashCode();
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Pattern)
            return ((Pattern)o).getPattern().equals(this.pattern);
        return false;
    }
    
    @Override
    public String toString() {
        if (this.pattern!=null)
            return this.pattern;
        return "null";
    }
    
}
