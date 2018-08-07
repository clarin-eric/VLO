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

import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface ResourceProcessor {

    /**
     * Extract ResourceProxies from ResourceProxyList
     *
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param resourceStructureGraph
     * @throws VTDException
     */
    void processResources(CMDIData cmdiData, VTDNav nav, ResourceStructureGraph resourceStructureGraph) throws VTDException;
    
}
