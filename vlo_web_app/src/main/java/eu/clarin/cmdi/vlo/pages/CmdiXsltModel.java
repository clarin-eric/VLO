/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.vlo.pages;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that renders a CMDI in XHMTL by means of a stylesheet. This model
 * discards the result of the transformation on detach to prevent large XHTML
 * content from being cached.
 *
 * @author twagoo
 */
public class CmdiXsltModel extends LoadableDetachableModel<String> {

    private final static Logger LOG = LoggerFactory.getLogger(CmdiXsltModel.class);

    private final URL xslFile = getClass().getResource("/eu/clarin/cmdi/vlo/pages/cmdi2xhtml.xsl");
    private final URL metadataUrl;

    /**
     *
     * @param metadataUrl URL of the metadata file to be presented
     */
    public CmdiXsltModel(URL metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    /**
     * Creates the XHTML representation to be shown
     *
     * @return
     */
    @Override
    protected String load() {
        final Processor proc = new Processor(false);
        final XsltCompiler comp = proc.newXsltCompiler();

        final StringWriter strWriter = new StringWriter();
        try {
            final XsltExecutable exp = comp.compile(new StreamSource(xslFile.getFile()));
            final XdmNode source = proc.newDocumentBuilder().build(
                    new StreamSource(new InputStreamReader(metadataUrl.openStream())));
            final Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "html");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
            out.setOutputWriter(strWriter);
            final XsltTransformer trans = exp.load();

            trans.setInitialContextNode(source);
            trans.setDestination(out);
            trans.transform();
            return (strWriter.toString());
        } catch (SaxonApiException e) {
            LOG.error("Couldn't transform CMDI metadata at {}: ", metadataUrl, e);
            return ("<b>Could not load complete CMDI metadata</b>");
        } catch (IOException e) {
            LOG.error("Couldn't read CMDI metadata at {}: ", metadataUrl, e);
            return ("<b>Could not load complete CMDI metadata</b>");
        }

    }

}
