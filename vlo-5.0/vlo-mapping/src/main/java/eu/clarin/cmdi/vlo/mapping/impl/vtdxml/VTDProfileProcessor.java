/*
 * Copyright (C) 2022 twagoo
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

import com.google.common.collect.Lists;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import static eu.clarin.cmdi.vlo.util.CmdConstants.CMD_NAMESPACE;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twagoo
 */
public class VTDProfileProcessor {

    private final VloMappingConfiguration config;

    public VTDProfileProcessor(VloMappingConfiguration config) {
        this.config = config;
    }

    /**
     * Given an xml-token path thingy create an xpath.
     *
     * @param elementPath
     * @param attributeName will be appended as attribute to XPath expression if
     * not null
     * @return
     */
    public String createXpath(List<ProfileXsdWalker.Token> elementPath, String attributeName) {
        final StringBuilder xpath = new StringBuilder("/cmd:CMD/cmd:Components/");
        for (ProfileXsdWalker.Token token : elementPath) {
            xpath.append("cmdp:").append(token.name).append("/");
        }
        if (attributeName != null) {
            return xpath.append("@").append(attributeName).toString();
        } else {
            return xpath.append("text()").toString();
        }
    }

    /**
     * Creates the concept path for the current node
     *
     * @param vn navigator
     * @param elementPath current element path
     * @param result result object
     * @return list representing the concept path
     * @throws NavException
     */
    public List<String> getConceptPath(VTDNav vn, LinkedList<ProfileXsdWalker.Token> elementPath, Map<String, Context> result) throws NavException {
        // determine the concept link
        final String conceptLink;
        final int attributeConceptLinkIndex = getConceptLinkIndex(vn);
        if (attributeConceptLinkIndex < 0) {
            conceptLink = "";
        } else {
            conceptLink = vn.toNormalizedString(attributeConceptLinkIndex);
        }

        // consturct the complete context path
        return constructContextPath(elementPath, result, conceptLink);
    }

    private List<String> constructContextPath(LinkedList<ProfileXsdWalker.Token> elementPath, Map<String, Context> result, final String conceptLink) {
        // look up parent's context path
        final Context parentContext = getParentContextPath(elementPath, result);

        final LinkedList<String> conceptPath;
        if (parentContext == null) {
            // no parent context path - we start from scratch
            conceptPath = Lists.newLinkedList();
        } else {
            // we can extend the parent's context path
            conceptPath = Lists.newLinkedList(parentContext.getConceptPath());
        }
        // first element is current concept link (may be null)
        conceptPath.addFirst(conceptLink);
        return conceptPath;
    }

    private Context getParentContextPath(LinkedList<ProfileXsdWalker.Token> elementPath, Map<String, Context> result) {
        final List<ProfileXsdWalker.Token> parentElementPath = getParentElementPath(elementPath);
        return result.get(createXpath(parentElementPath, null));
    }

    private List<ProfileXsdWalker.Token> getParentElementPath(List<ProfileXsdWalker.Token> elementPath) {
        if (elementPath.size() <= 1) {
            return Collections.emptyList();
        } else {
            return elementPath.subList(0, elementPath.size() - 1);
        }
    }

    public Vocabulary getVocabulary(VTDNav vn) throws NavException {
        int vocabIndex = getVocabIndex(vn);
        if (vocabIndex < 0) {
            return null;
        } else {
            final String uri = vn.toNormalizedString(vocabIndex);
            final Vocabulary vocab = new Vocabulary(config.getVocabularyRegistryUrl(), URI.create(uri));
            int propIndex = getVocabPropIndex(vn);
            if (propIndex != -1) {
                String prop = vn.toNormalizedString(propIndex);
                vocab.setProperty(prop);
            }
            int langIndex = getVocabLangIndex(vn);
            if (langIndex != -1) {
                String lang = vn.toNormalizedString(langIndex);
                vocab.setLanguage(lang);
            }
            return vocab;
        }
    }

    /**
     * Goal is to get the "ConceptLink" attribute. Tries a number of different
     * favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getConceptLinkIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ConceptLink");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ConceptLink");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary URI" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "Vocabulary");
        if (result == -1) {
            result = vn.getAttrVal("cmd:Vocabulary");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary Property" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabPropIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ValueProperty");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ValueProperty");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary Language" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabLangIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ValueLanguage");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ValueLanguage");
        }
        return result;
    }
}
