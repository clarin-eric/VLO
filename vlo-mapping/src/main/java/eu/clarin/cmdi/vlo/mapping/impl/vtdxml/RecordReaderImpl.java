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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.mapping.RecordReader;
import eu.clarin.cmdi.vlo.util.CmdConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class RecordReaderImpl implements RecordReader {

    @Override
    public Record readRecord(File file) throws IOException {
        try {
            final VTDGen vg = new VTDGen();
            try ( FileInputStream fileInputStream = new FileInputStream(file)) {
                vg.setDoc(IOUtils.toByteArray(fileInputStream));
                vg.parse(true);
            }

            final VTDNav nav = vg.getNav();
            final String profileId = extractXsd(nav, file.getAbsolutePath());
        } catch (VTDException ex) {
            throw new IOException("Exception while parsing record from file: " + String.valueOf(file), ex);
        }
    }

    public static String extractXsd(VTDNav nav, String context) throws VTDException {
        String profileID = getProfileIdFromHeader(nav);
        if (profileID != null) {
            Matcher m = PROFILE_ID_PATTERN.matcher(profileID);
            if (m.matches()) {
                profileID = m.group(1);
            } else {
                log.warn("MdProfile header element in {} contains an invalid profile ID: {}", context, profileID);
                profileID = null;
            }
        }
        if (profileID == null) {
            profileID = getProfileIdFromSchemaLocation(nav);
        }
        return profileID;
    }

    /**
     * Extract XSD schema information from CMDI header (using element
     * //Header/MdProfile)
     *
     * @param nav VTD Navigator
     * @return ID of CMDI schema, or null if content of //Header/MdProfile
     * element could not be read
     * @throws XPathParseException
     * @throws XPathEvalException
     * @throws NavException
     */
    private static String getProfileIdFromHeader(VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, null);
        ap.selectXPath("/cmd:CMD/cmd:Header/cmd:MdProfile/text()");
        int index = ap.evalXPath();
        String profileId = null;
        if (index != -1) {
            profileId = nav.toString(index).trim();
        }
        return profileId;
    }
    private static final java.util.regex.Pattern PROFILE_ID_PATTERN = java.util.regex.Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");

    /**
     * Extract XSD schema information from schemaLocation or
     * noNamespaceSchemaLocation attributes
     *
     * @param nav VTD Navigator
     * @return ID of CMDI schema, or null if attributes don't exist
     * @throws NavException
     */
    private static String getProfileIdFromSchemaLocation(VTDNav nav) throws NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        int index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
        if (index != -1) {
            String schemaLocation = nav.toNormalizedString(index);
            String[] schemaLocationArray = schemaLocation.split(" ");
            result = schemaLocationArray[schemaLocationArray.length - 1];
        } else {
            index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
            if (index != -1) {
                result = nav.toNormalizedString(index);
            }
        }

        // extract profile ID
        if (result != null) {
            Matcher m = PROFILE_ID_PATTERN.matcher(result);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    /**
     * Setting namespace for Autopilot ap
     *
     * @param ap
     * @param profileId
     */
    public static void setNameSpace(AutoPilot ap, String profileId) {
        ap.declareXPathNameSpace("cmd", CmdConstants.CMD_NAMESPACE);
        if (profileId != null) {
            ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/" + profileId);
        }
    }

}
