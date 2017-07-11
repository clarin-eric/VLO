/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.pages;

import org.apache.wicket.markup.html.WebComponent;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
class PiwikTracker extends WebComponent {

    private final String siteId;
    private final String piwikHost;
    private final String domains;

    public PiwikTracker(String id, String siteId, String piwikHost, String domains) {
        super(id);
        this.siteId = siteId;
        this.piwikHost = piwikHost;
        this.domains = domains;
    }

    @Override
    protected void onRender() {
        getResponse().write("\n"
                + "<!-- Piwik -->\n"
                + "<script type=\"text/javascript\">\n"
                + "  var canonicalLink = $('link[rel=\"canonical\"]');\n"
                + "  if(canonicalLink.length > 0) {\n"
                + "     var canonicalUrl = canonicalLink[0].getAttribute('href');\n"
                + "  }\n"
                + "  var _paq = _paq || [];\n"
                + "  _paq.push([\"setDomains\", [\"" + domains + "\"]]);\n"
                + "  if(canonicalUrl)\n"
                + "  {\n"
                + "    _paq.push(['setCustomUrl', canonicalUrl]);\n"
                + "  }\n"
                + "  _paq.push(['trackPageView']);\n"
                + "  _paq.push(['enableLinkTracking']);\n"
                + "  (function() {\n"
                + "        var u=\"" + piwikHost + "\";\n"
                + "        _paq.push(['setTrackerUrl', u+'piwik.php']);\n"
                + "        _paq.push(['setSiteId', " + siteId + "]);\n"
                + "        var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0]; g.type='text/javascript';\n"
                + "        g.defer=true; g.async=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g, s);\n" + "  })();\n"
                + "</script>\n"
                + "<noscript><p><img src=\"" + piwikHost + "piwik.php?idsite=" + siteId + "\" style=\"border: 0;\" alt=\"\" /></p></noscript>\n"
                + "<!-- End Piwik Code -->");
    }

}
