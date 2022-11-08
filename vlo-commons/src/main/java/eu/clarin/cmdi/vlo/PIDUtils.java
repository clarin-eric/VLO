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
package eu.clarin.cmdi.vlo;

import static eu.clarin.cmdi.vlo.FacetConstants.DOI_RESOLVER_URL;
import static eu.clarin.cmdi.vlo.FacetConstants.HANDLE_PROXY;
import static eu.clarin.cmdi.vlo.FacetConstants.URN_NBN_RESOLVER_URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils for detecting and processing persistent identifiers such as Handles,
 * DOIs and URN-NBNs
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDUtils {

    public final static String HANDLE_PATTERN_STRING = "(hdl:|https?:\\/\\/hdl.handle.net\\/)(.+)";
    public final static int HANDLE_SCHEME_SPECIFIC_PART_GROUP = 2;
    public final static Pattern HANDLE_PATTERN = Pattern.compile(HANDLE_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

    public final static String DOI_PATTERN_STRING = "(doi:|https?:\\/\\/(dx.)?doi.org\\/)(.+)";
    public final static int DOI_SCHEME_SPECIFIC_PART_GROUP = 3;
    public final static Pattern DOI_PATTERN = Pattern.compile(DOI_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

    public final static String URN_NBN_PATTERN_STRING = "(https?:\\/\\/(urn.fi|nbn-resolving.de)\\/)?urn:nbn:(.+)";
    public final static int URN_NBN_SCHEME_SPECIFIC_PART_GROUP = 3;
    public final static Pattern URN_NBN_PATTERN = Pattern.compile(URN_NBN_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

    //PID: HDL or DOI
    public final static Pattern PID_PATTERN = Pattern.compile("^(" + HANDLE_PATTERN_STRING + "|" + DOI_PATTERN_STRING + "|" + URN_NBN_PATTERN_STRING + ")$", Pattern.CASE_INSENSITIVE);

    private final static Pattern ACTIONABLE_LINK_PATTERN = Pattern.compile("https?:\\/\\/.+", Pattern.CASE_INSENSITIVE);
    
    /**
     *
     * @param uri
     * @return whether the provided URI is a PID of any type
     */
    public static boolean isPid(String uri) {
        if (uri == null) {
            return false;
        } else {
            return PID_PATTERN.matcher(uri).matches();
        }
    }

    /**
     *
     * @param uri
     * @return whether the provided URI is a handle
     */
    public static boolean isHandle(String uri) {
        if (uri == null) {
            return false;
        } else {
            return HANDLE_PATTERN.matcher(uri).matches();
        }
    }

    /**
     *
     * @param uri
     * @return whether the provided URI is a handle
     */
    public static boolean isDoi(String uri) {
        if (uri == null) {
            return false;
        } else {
            return DOI_PATTERN.matcher(uri).matches();
        }
    }

    /**
     *
     * @param uri
     * @return whether the provided URI is a handle
     */
    public static boolean isUrnNbn(String uri) {
        if (uri == null) {
            return false;
        } else {
            return URN_NBN_PATTERN.matcher(uri).matches();
        }
    }

    /**
     *
     * @param uri
     * @return whether the provided URI is actionable
     */
    public static boolean isActionableLink(String uri) {
        if (uri == null) {
            return false;
        } else {
            return ACTIONABLE_LINK_PATTERN.matcher(uri).matches();
        }
    }

    public static Optional<PIDType> getType(String uri) {
        if (isPid(uri)) {
            if (isHandle(uri)) {
                return Optional.of(PIDType.HANDLE);
            }
            if (isDoi(uri)) {
                return Optional.of(PIDType.DOI);
            }
            if (isUrnNbn(uri)) {
                return Optional.of(PIDType.URN_NBN);
            }
            return Optional.of(PIDType.OTHER);
        } else {
            return Optional.empty();
        }
    }

    /**
     *
     * @param uri
     * @return the scheme specific part of the URI, for example '1234/56' for
     * 'hdl:1234/56' or 'http://dx.doi.org/1234/56'; if no scheme specific part
     * is found, null will be returned
     */
    public static String getSchemeSpecificId(String uri) {
        if (uri != null) {
            //Handle?
            {
                final Matcher matcher = HANDLE_PATTERN.matcher(uri);
                if (matcher.matches() && matcher.groupCount() >= HANDLE_SCHEME_SPECIFIC_PART_GROUP) {
                    return matcher.group(HANDLE_SCHEME_SPECIFIC_PART_GROUP);
                }
            }
            //DOI?
            {
                final Matcher matcher = DOI_PATTERN.matcher(uri);
                if (matcher.matches() && matcher.groupCount() >= DOI_SCHEME_SPECIFIC_PART_GROUP) {
                    return matcher.group(DOI_SCHEME_SPECIFIC_PART_GROUP);
                }
            }
            //URN-NBN?
            {
                final Matcher matcher = URN_NBN_PATTERN.matcher(uri);
                if (matcher.matches() && matcher.groupCount() >= URN_NBN_SCHEME_SPECIFIC_PART_GROUP) {
                    return matcher.group(URN_NBN_SCHEME_SPECIFIC_PART_GROUP);
                }
            }
        }
        return null;
    }

    public static String getActionableLinkForPid(final String link) {
        if (link != null && !isActionableLink(link)) {
            if (PIDUtils.isHandle(link)) {
                return HANDLE_PROXY + PIDUtils.getSchemeSpecificId(link);
            }
            if (PIDUtils.isDoi(link)) {
                return DOI_RESOLVER_URL + PIDUtils.getSchemeSpecificId(link);
            }
            if (PIDUtils.isUrnNbn(link)) {
                return URN_NBN_RESOLVER_URL + PIDUtils.getSchemeSpecificId(link);
            }
        }
        return link;
    }

}
