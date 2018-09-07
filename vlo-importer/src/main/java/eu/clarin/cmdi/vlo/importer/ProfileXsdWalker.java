/*
 * Copyright (C) 2018 CLARIN
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

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class ProfileXsdWalker<R> {

    private final static Logger LOG = LoggerFactory.getLogger(ProfileXsdWalker.class);

    private final VloConfig vloConfig;
    protected final String xsd;
    protected final Boolean useLocalXSDCache;

    public ProfileXsdWalker(VloConfig vloConfig, String xsd, Boolean useLocalXSDCache) {
        this.vloConfig = vloConfig;
        this.xsd = xsd;
        this.useLocalXSDCache = useLocalXSDCache;
    }
    
    protected abstract R createResultObject();

    protected R walkProfile() throws URISyntaxException, NavException {
        R result = createResultObject();
        VTDGen vg = new VTDGen();
        boolean parseSuccess;
        if (useLocalXSDCache) {
            parseSuccess = vg.parseFile(Thread.currentThread().getContextClassLoader().getResource("testProfiles/" + xsd + ".xsd").getPath(), true);
        } else {
            parseSuccess = vg.parseHttpUrl(vloConfig.getComponentRegistryProfileSchema(xsd), true);
        }
        if (!parseSuccess) {
            LOG.error("Cannot create ConceptLink Map from xsd (xsd is probably not reachable): " + xsd + ". All metadata instances that use this xsd will not be imported correctly.");
            return result; //return empty map, so the incorrect xsd is not tried for all metadata instances that specify it.
        }
        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectElement("xs:element");
        LinkedList<Token> elementPath = new LinkedList<>();
        while (ap.iterate()) {
            int i = vn.getAttrVal("name");
            if (i != -1) {
                String elementName = vn.toNormalizedString(i);
                updateElementPath(vn, elementPath, elementName);
                processElement(vn, elementPath, result);
                // look for associated attributes with concept links
                vn.push();
                processAttributes(vn, elementPath, result, elementName);
                // returning to normal element-based workflow
                vn.pop();
            }
        }
        return result;
    }

    protected void processAttributes(VTDNav vn, LinkedList<Token> elementPath, R result, String elementName) throws URISyntaxException {
        AutoPilot attributeAutopilot = new AutoPilot(vn);
        attributeAutopilot.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

        try {
            attributeAutopilot.selectXPath("./xs:complexType/xs:simpleContent/xs:extension/xs:attribute | ./xs:complexType/xs:attribute");
            while (attributeAutopilot.evalXPath() != -1) {
                processAttribute(vn, elementPath, result);
            }
        } catch (XPathParseException | XPathEvalException | NavException e) {
            LOG.error("Cannot extract attributes for element " + elementName + ". Will continue anyway...", e);
        }
    }
    
    protected abstract void processAttribute(VTDNav vn, LinkedList<Token> elementPath, R result) throws URISyntaxException, NavException;
    
    protected abstract void processElement(VTDNav vn, LinkedList<Token> elementPath, R result) throws NavException, URISyntaxException;

    /**
     * Given an xml-token path thingy create an xpath.
     *
     * @param elementPath
     * @param attributeName will be appended as attribute to XPath expression if
     * not null
     * @return
     */
    protected Pattern createXpath(List<Token> elementPath, String attributeName) {
        StringBuilder xpath = new StringBuilder("/cmd:CMD/cmd:Components/");
        for (Token token : elementPath) {
            xpath.append("cmdp:").append(token.name).append("/");
        }
        if (attributeName != null) {
            return new Pattern(xpath.append("@").append(attributeName).toString());
        } else {
            return new Pattern(xpath.append("text()").toString());
        }
    }

    /**
     * does some updating after a step. To keep the path proper and path-y.
     *
     * @param vn
     * @param elementPath
     * @param elementName
     */
    private void updateElementPath(VTDNav vn, LinkedList<Token> elementPath, String elementName) {
        int previousDepth = elementPath.isEmpty() ? -1 : elementPath.peekLast().depth;
        int currentDepth = vn.getCurrentDepth();
        if (currentDepth == previousDepth) {
            elementPath.removeLast();
        } else if (currentDepth < previousDepth) {
            while (currentDepth <= previousDepth) {
                elementPath.removeLast();
                previousDepth = elementPath.peekLast().depth;
            }
        }
        elementPath.offerLast(new Token(currentDepth, elementName));
    }

    protected class Token {

        final String name;
        final int depth;

        public Token(int depth, String name) {
            this.depth = depth;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + ":" + depth;
        }
    }
}
