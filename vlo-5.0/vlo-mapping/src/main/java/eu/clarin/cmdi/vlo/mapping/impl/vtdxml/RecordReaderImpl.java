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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.CachingProfileFactory;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.mapping.ProfileFactory;
import eu.clarin.cmdi.vlo.mapping.RecordReader;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingException;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContextImpl;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
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

    public static final String ENGLISH_LANGUAGE = "code:eng";
    public static final String DEFAULT_LANGUAGE = "code:und";
    private final ProfileFactory profileFactory;

    public RecordReaderImpl(VloMappingConfiguration mappingConfig) {
        this(new CachingProfileFactory(new ProfileReaderImpl(mappingConfig)));
    }

    public RecordReaderImpl(ProfileFactory profileFactory) {
        this.profileFactory = profileFactory;
    }

    @Override
    public CmdRecord readRecord(File file) throws IOException, VloMappingException {
        try {
            // prepare
            final VTDNav nav = openFile(file);
            final String profileId = extractProfileId(nav, file.getAbsolutePath());
            if (profileId == null) {
                throw new VloMappingException("Profile could not be determined, mapping skipped for record " + file.getAbsolutePath());
            } else {
                // we can now dive into the file and construct a record object
                final CmdRecord.CmdRecordBuilder recordBuilder = CmdRecord.builder();

                final VTDNav rootNav = nav.cloneNav();
                rootNav.toElement(VTDNav.ROOT);
                parse(file, rootNav, recordBuilder, profileId);
                return recordBuilder.build();
            }
        } catch (VTDException ex) {
            throw new VloMappingException("Exception while parsing record from file: " + String.valueOf(file), ex);
        }
    }

    private VTDNav openFile(File file) throws ParseException, IOException {
        final VTDGen vg = new VTDGen();
        try ( FileInputStream fileInputStream = new FileInputStream(file)) {
            vg.setDoc(IOUtils.toByteArray(fileInputStream));
            vg.parse(true);
        }
        final VTDNav nav = vg.getNav();
        return nav;
    }

    private void parse(File file, VTDNav nav, final CmdRecord.CmdRecordBuilder record, final String profileId) throws IOException, VloMappingException, VTDException {
        //read the profile
        final CmdProfile profile = profileFactory.getProfile(profileId);
        record.profile(profile);

        ImmutableList.Builder<ValueContext> contexts = ImmutableList.builder();

        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, profileId);
        profile.getXpathContextMap().forEach((path, context) -> {
            try {
                ap.selectXPath(path);
                final int index = ap.evalXPath();
                final ImmutableList<ValueLanguagePair> values = getValues(nav, index, ap);
                if (!values.isEmpty()) {
                    contexts.add(new ValueContextImpl(context, values));
                }
            } catch (VTDException ex) {
                log.error("Processing exception in {} at path {}", file, path, ex);
            }
        });

        // value contexts
        record.contexts(contexts.build());
        // resources
        // anything else?
    }

    private ImmutableList<ValueLanguagePair> getValues(VTDNav nav, int index, AutoPilot ap) throws VTDException {
        final ImmutableList.Builder<ValueLanguagePair> valuesBuilder = ImmutableList.builder();
        boolean matchedPattern = false;
        while (index != -1) {
            matchedPattern = true;

            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }

            final String value = nav.toString(index);
            final String language = extractLanguageCode(nav);
            // TODO: extract vocab item URI

            valuesBuilder.add(new ValueLanguagePair(value, language));

            index = ap.evalXPath();
        }

        final ImmutableList<ValueLanguagePair> values = valuesBuilder.build();
        return values;
    }

    private String extractLanguageCode(VTDNav nav) throws NavException {
        // extract language code in xml:lang if available
        Integer langAttrIndex = nav.getAttrVal("xml:lang");
        String languageCode;
        if (langAttrIndex != -1) {
            languageCode = nav.toString(langAttrIndex).trim();
        } else {
            return DEFAULT_LANGUAGE;
        }

        return languageCode;
    }

    private static String extractProfileId(VTDNav nav, String context) throws VTDException {
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
    private static void setNameSpace(AutoPilot ap, String profileId) {
        ap.declareXPathNameSpace("cmd", CmdConstants.CMD_NAMESPACE);
        if (profileId != null) {
            ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/" + profileId);
        }
    }

}
