/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import java.net.URL;
import javax.xml.transform.TransformerException;
import org.apache.wicket.model.IModel;
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
public class XsltModel extends LoadableDetachableModel<String> {

    private final static Logger logger = LoggerFactory.getLogger(XsltModel.class);
    private final IModel<URL> metadataUrl;

    /**
     *
     * @param metadataUrl URL of the metadata file to be presented
     */
    public XsltModel(IModel<URL> metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    /**
     * Creates the XHTML representation to be shown
     *
     * @return
     */
    @Override
    protected String load() {
        final URL object = metadataUrl.getObject();
        if (object == null) {
            return "";
        }
        try {
            return getTransformationService().transformXml(object);
        } catch (TransformerException ex) {
            logger.error("Could not transform {}", object, ex);
            return ("<b>Could not load complete CMDI metadata</b>");
        }

    }

    protected XmlTransformationService getTransformationService() {
        return VloWicketApplication.get().getCmdiTransformationService();
    }

}
