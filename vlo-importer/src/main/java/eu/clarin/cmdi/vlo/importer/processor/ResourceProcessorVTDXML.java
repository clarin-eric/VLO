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
package eu.clarin.cmdi.vlo.importer.processor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;

/**
 * Processes resource from a CMDI file. Not to be reused!
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceProcessorVTDXML implements ResourceProcessor {

    private final VTDNav nav;

    public ResourceProcessorVTDXML(VTDNav nav) {
        this.nav = nav;
    }

    /**
     * Extract ResourceProxies from ResourceProxyList
     *
     * @param cmdiData representation of the CMDI document
     * @param resourceStructureGraph
     */
    @Override
    public void processResources(CMDIData cmdiData, ResourceStructureGraph resourceStructureGraph) throws CMDIParsingException {
        try {
            doProcess(cmdiData, resourceStructureGraph);
        } catch (VTDException ex) {
            throw new CMDIParsingException("VTD parsing exception while processing resources", ex);
        }
    }

    /**
     *
     * @param cmdiData
     * @param resourceStructureGraph
     * @throws XPathParseException
     */
    private void doProcess(CMDIData cmdiData, ResourceStructureGraph resourceStructureGraph) throws VTDException {
        AutoPilot mdSelfLink = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(mdSelfLink, null);
        mdSelfLink.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink");
        String mdSelfLinkString = mdSelfLink.evalXPathToString();
        if (resourceStructureGraph != null) {
            resourceStructureGraph.addResource(mdSelfLinkString);
        }

        AutoPilot resourceProxy = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceProxy, null);
        resourceProxy.selectXPath("/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy");

        AutoPilot resourceRef = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceRef, null);
        resourceRef.selectXPath("cmd:ResourceRef");

        AutoPilot resourceType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceType, null);
        resourceType.selectXPath("cmd:ResourceType");

        AutoPilot resourceMimeType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceMimeType, null);
        resourceMimeType.selectXPath("cmd:ResourceType/@mimetype");

        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();

            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                cmdiData.addResource(ref, type, mimeType);
            }

            // resource hierarchy information?
            if (resourceStructureGraph != null && type.toLowerCase().equals("metadata")) {
                resourceStructureGraph.addEdge(ref, mdSelfLinkString);
            }
        }
    }
}
