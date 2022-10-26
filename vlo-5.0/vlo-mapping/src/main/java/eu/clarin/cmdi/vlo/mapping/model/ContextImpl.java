/*
 * Copyright (C) 2022 CLARIN
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

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ContextImpl implements Context {

    private final String xpath;
    private final Collection<String> conceptPath;
    private final Vocabulary vocabulary;

    public ContextImpl(String xpath, Collection<String> conceptPath, Vocabulary vocabulary) {
        this.xpath = xpath;
        this.conceptPath = conceptPath;
        this.vocabulary = vocabulary;
    }

    @Override
    public String getXpath() {
        return xpath;
    }

    @Override
    public Collection<String> getConceptPath() {
        return conceptPath;
    }

    @Override
    public Vocabulary getVocabulary() {
        return vocabulary;
    }

}
