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
package eu.clarin.cmdi.vlo.mapping;

import org.apache.xerces.util.XMLChar;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class XPathUtils {

    /**
     * Adapted from
     * {@literal  org.apache.xerces.impl.xs.identity.Selector (xerces:xercesImpl:2.12.2)}
     *
     * @param xpath
     * @return normalized xpath
     */
    public static String normalize(String xpath) {
        if (xpath == null) {
            return null;
        }
        xpath = XMLChar.trim(xpath);
        
        // NOTE: We have to prefix the selector XPath with "./" in
        //       order to handle selectors such as "." that select
        //       the element container because the fields could be
        //       relative to that element. -Ac
        //       Unless xpath starts with a descendant node -Achille Fokoue
        //      ... or a '.' or a '/' - NG
        //  And we also need to prefix exprs to the right of | with ./ - NG
        StringBuilder modifiedXPath = new StringBuilder(xpath.length() + 5);
        int unionIndex;
        do {
            if (!(XMLChar.trim(xpath).startsWith("/") || XMLChar.trim(xpath).startsWith("."))) {
                modifiedXPath.append("./");
            }
            unionIndex = xpath.indexOf('|');
            if (unionIndex == -1) {
                modifiedXPath.append(xpath);
                break;
            }
            modifiedXPath.append(xpath.substring(0, unionIndex + 1));
            xpath = XMLChar.trim(xpath.substring(unionIndex + 1, xpath.length()));
        } while (true);
        return modifiedXPath.toString();
    }
}
