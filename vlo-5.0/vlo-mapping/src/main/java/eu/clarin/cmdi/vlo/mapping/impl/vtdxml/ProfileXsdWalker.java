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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.util.LinkedList;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Walks a CMDI profile schema
 * @author Twan Goosen <twan@clarin.eu>
 */
@Slf4j
public class ProfileXsdWalker<R> {

    private final VTDProfileParser profileParser;

    private final Supplier<R> newResultObjectSupplier;

    private final VTDNavProcessor<R> attributeProcessor;

    private final VTDNavProcessor<R> elementProcessor;

    public ProfileXsdWalker(VTDProfileParser profileParser, Supplier<R> newResultObjectSupplier, VTDNavProcessor<R> attributeProcessor, VTDNavProcessor<R> elementProcessor) {
        this.profileParser = profileParser;
        this.newResultObjectSupplier = newResultObjectSupplier;
        this.attributeProcessor = attributeProcessor;
        this.elementProcessor = elementProcessor;
    }

    protected R walkProfile(String profileId) throws NavException {
        final R result = newResultObjectSupplier.get();
        final VTDNav vn = profileParser.parse(profileId);
        if (vn == null) {
            log.error("Cannot create ConceptLink Map from xsd (xsd is probably not reachable): " + profileId + ". All metadata instances that use this xsd will not be imported correctly.");
            return result; //return empty map, so the incorrect xsd is not tried for all metadata instances that specify it.
        }

        AutoPilot ap = new AutoPilot(vn);
        ap.selectElement("xs:element");
        LinkedList<Token> elementPath = new LinkedList<>();
        while (ap.iterate()) {
            int i = vn.getAttrVal("name");
            if (i != -1) {
                String elementName = vn.toNormalizedString(i);
                updateElementPath(vn, elementPath, elementName);
                elementProcessor.process(vn, elementPath, result);
                // look for associated attributes with concept links
                vn.push();
                processAttributes(vn, elementPath, result, elementName);
                // returning to normal element-based workflow
                vn.pop();
            }
        }
        return result;
    }

    protected void processAttributes(VTDNav vn, LinkedList<Token> elementPath, R result, String elementName) {
        AutoPilot attributeAutopilot = new AutoPilot(vn);
        attributeAutopilot.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

        try {
            attributeAutopilot.selectXPath("./xs:complexType/xs:simpleContent/xs:extension/xs:attribute | ./xs:complexType/xs:attribute");
            while (attributeAutopilot.evalXPath() != -1) {
                attributeProcessor.process(vn, elementPath, result);
            }
        } catch (XPathParseException | XPathEvalException | NavException e) {
            log.error("Cannot extract attributes for element " + elementName + ". Will continue anyway...", e);
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

    public static class Token {

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

    public static interface VTDNavProcessor<R> {

        void process(VTDNav vn, LinkedList<Token> elementPath, R result) throws NavException;

    }

}
