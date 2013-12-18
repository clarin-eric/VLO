
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;

/**
 *
 * @author keeloo
 *
 * A theme is composed from a page title, a CSS file, two image files, and a
 * partner link map relating coordinates in the right image to partner links
 */
public class Theme {

    public String name, pageTitle, topLeftImage, topRightImage, cssFile,
            partnerLinkMap;

    /**
     * Compose a theme<br><br>
     *
     * @param name the name of the theme to be composed
     */
    public Theme(String themeName) {

        String prefix = VloConfig.getReverseProxyPrefix();

        if (themeName.matches("CLARIN-D")) {
            // select the CLARIN-D theme's components

            pageTitle = "CLARIN-D Virtual Language Observatory - Resources";
            topLeftImage = "Images/topleft-clarin-d.png";
            topRightImage = "Images/topright-clarin-d.png";
            if (prefix.length() == 0) {
                cssFile = "css/clarin-d.css";
            } else {
                cssFile = prefix + "css/clarin-d.css";
            }
            partnerLinkMap = getClarinDPartnerLinkMap();
            name = "CLARIN-D";
        } else {
            // select the default theme elements
            pageTitle = "CLARIN Virtual Language Observatory - Resources";
            topLeftImage = "Images/topleftvlo.gif";
            topRightImage = "Images/toprightvlo.gif";

            if (prefix.length() == 0) {
                cssFile = "css/main.css";
            } else {
                cssFile = prefix + "css/main.css";
            }
            partnerLinkMap = getDefaultPartnerLinkMap();
            name = "defaultTheme";
        }
        // remember the theme as a persistent parameter
        // getPersistentParameters.put("theme", name);
    }

    /**
     * Compose a map to be included in the HTML document, designating the
     * positions of the links to partner web sites
     * 
     * @return the map
     */
    private String getDefaultPartnerLinkMap() {
        String map;

        map = "<map name=\"partnerLinks\">\n";
        map = appendToPartnerLinkMap(map,
                "114.00,65,167.50,104",
                "http://www.clarin.eu",
                "clarin link");
        map = appendToPartnerLinkMap(map,
                "177.00,65,214,104",
                "http://wals.info",
                "wals link");
        map = appendToPartnerLinkMap(map,
                "229,65,279,104",
                "http://linguistlist.org",
                "linguistlist link");
        map = appendToPartnerLinkMap(map,
                "290,65,320,104",
                "http://www.elra.info",
                "elra link");
        map = appendToPartnerLinkMap(map,
                "328,65,370,104",
                "http://www.mpi.nl/dobes",
                "dobes link");
        map = appendToPartnerLinkMap(map,
                "379,65,428,104",
                "http://www.dfki.de/web",
                "dfki link");
        map = appendToPartnerLinkMap(map,
                "434,65,484,104",
                "http://www.delaman.org",
                "deleman link");

        map = map + "</map>";

        return map;
    }

    /**
     * Compose a map to be included in the HTML document, designating the
     * positions of the links to partner web sites
     *
     * @return the map
     */
    private String getClarinDPartnerLinkMap() {
        String map;

        map = "<map name=\"partnerLinks\">\n";
        map = map + "</map>";

        return map;
    }

    /**
     * Add a link location to the map indicating the partner links
     *
     * @param map
     * @return
     */
    private String appendToPartnerLinkMap(String map, String coordinates,
            String URL, String alt) {

        if (map == null) {
            map = "<map name=\"partnerLinks\">\n";
        } else if (map.equals("")) {
            map = "<map name=\"partnerLinks\">\n";
        }

        map = map + "<AREA SHAPE=\"rect\" COORDS=\"" + coordinates + "\" HREF=\""
                + URL + "\" alt=\"" + alt + "\"/>\n";

        return map;
    }
}
