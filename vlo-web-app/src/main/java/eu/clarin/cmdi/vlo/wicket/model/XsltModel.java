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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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
 * Multiple URLs can be provided. The model will provide the output of the first
 * of the URLs that results in a successful transformation. This provides the
 * basis for a fallback mechanism.
 *
 * @author twagoo
 */
public class XsltModel extends LoadableDetachableModel<String> {

    private final static Logger logger = LoggerFactory.getLogger(XsltModel.class);
    private final IModel<List<URL>> metadataUrlsModel;

    /**
     *
     * @param metadataUrl Model of URL(s) that provide a transformation source
     * location candidate (evaluated in order)
     */
    public XsltModel(IModel<List<URL>> metadataUrl) {
        this.metadataUrlsModel = metadataUrl;
    }

    /**
     * Creates the XHTML representation to be shown
     *
     * @return
     */
    @Override
    protected String load() {
        final List<URL> urls = metadataUrlsModel.getObject();
        if (urls == null || urls.isEmpty()) {
            return "";
        }

        //try the provided URLs in order
        return urls.stream()
                .filter(Objects::nonNull) // in cases null objects somehow ended up in the collection
                .flatMap(url -> {
                    return tryTransform(url, urls); // returns empty stream if transformation fails
                })
                .findFirst() // only one successful transformation is needed
                .orElse("<b>Could not load complete CMDI metadata</b>"); // in case none succeeded
    }

    private Stream<String> tryTransform(URL url, final List<URL> urls) {
        try {
            final String transformed = getTransformationService().transformXml(url);
            if (transformed != null && !transformed.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Successful transformation using URL {} out of {}: {}", 1 + urls.indexOf(url), urls.size(), url);
                }

                //transformation was successful, ignore remaining items if any
                return Stream.of(transformed);
            }
        } catch (TransformerException ex) {
            logger.warn("Could not transform {}", url, ex);
        }
        return Stream.empty();
    }

    protected XmlTransformationService getTransformationService() {
        return VloWicketApplication.get().getCmdiTransformationService();
    }

    @Override
    protected void onDetach() {
        metadataUrlsModel.detach();
    }

}
