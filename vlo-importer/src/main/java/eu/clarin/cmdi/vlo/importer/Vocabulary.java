/*
 * Copyright (C) 2017 CLARIN
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Menzo Windhouwer &lt;menzo.windhouwer@meertens.knaw.nl&gt;
 */
public class Vocabulary {
    
    private final static Logger LOG = LoggerFactory.getLogger(Vocabulary.class);

    static final String DEFAULT_PROP = "prefLabel";
    static final String DEFAULT_LANG = "en";
    
    private String endpoint = null;
    private URI uri;
    private String prop;
    private String lang;
    
    public Vocabulary(String endpoint, URI uri) {
        this.endpoint = endpoint;
        this.uri = uri;
    }
    
    public Vocabulary(String endpoint) {
        this(endpoint,null);
    }
    
    public boolean hasURI() {
        return (this.uri != null);
    }
    
    public URI getURI() {
        return this.uri;
    }
    
    public String getEndpoint() {
        return this.endpoint;
    }
    
    public void setProperty(String prop) {
        this.prop = prop;
    }
    
    public boolean hasProperty() {
        return (this.prop!=null);
    }
    
    public String getProperty() {
        if (hasProperty())
            return this.prop;
        return DEFAULT_PROP;
    }
    
    public void setLanguage(String lang) {
        this.lang = lang;
    }
    
    public boolean hasLanguage() {
        return (this.lang!=null);
    }
    
    public String getLanguage() {
        if (hasLanguage())
            return this.lang;
        return DEFAULT_LANG;
    }
    
    public ImmutablePair<String,String> getValue(URI item) throws URISyntaxException, XPathParseException, XPathEvalException, NavException, UnsupportedEncodingException {
        ImmutablePair<String,String> res = null;
        String cst = (hasURI()?String.format("&conceptScheme=%s",URLEncoder.encode(this.getURI().toString(),"UTF-8")):"");
        URI lookup = new URI(String.format(getEndpoint()+"?fl=%s&q=uri:%s%s",this.getProperty(),URLEncoder.encode(item.toString().replace(":","\\:"),"UTF-8"),cst));
        VTDGen g = new VTDGen();
        if (g.parseHttpUrl(lookup.toString(), true)) {
            VTDNav n = g.getNav();
            AutoPilot p = new AutoPilot(n);
            p.declareXPathNameSpace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            p.declareXPathNameSpace("skos", "http://www.w3.org/2004/02/skos/core#");
            p.declareXPathNameSpace("xml", "http://www.w3.org/XML/1998/namespace");
            String lt = (this.hasLanguage()?String.format("[@xml:lang='%s']",this.getLanguage()):"");
            String xp = String.format("/rdf:RDF/rdf:Description/skos:%s%s/text()",this.getProperty(),lt);
            p.selectXPath(xp);
            int i = p.evalXPath();
            if (i != -1) {
                final String v = n.toString(i);
                res = new ImmutablePair<String,String>(v,(this.hasLanguage()?this.getLanguage():null));
            }
        } else
            LOG.warn("Cannot lookup value ConceptLink: " + lookup + ".");
        return res;
    }
    
}
