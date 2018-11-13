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
package eu.clarin.cmdi.vlo.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.request.resource.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class VloConfigWicketResource extends AbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(VloConfigWicketResource.class);

    private final URI vloConfigLocation;

    public VloConfigWicketResource(URI vloConfigLocation) {
        this.vloConfigLocation = vloConfigLocation;
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        if (vloConfigLocation == null) {
            logger.error("Cannot serve VloConfig, location unknown");
            return new ResourceResponse().setError(500, "VLO configuration location unknown");
        }

        return new ResourceResponse()
                .setTextEncoding("UTF-8")
                .setFileName("VloConfig.xml")
                .setContentType("application/xml")
                .setWriteCallback(new VloConfigWriteCallback());
    }

    private class VloConfigWriteCallback extends WriteCallback {

        @Override
        public void writeData(Attributes attributes) throws IOException {
            try (OutputStream os = attributes.getResponse().getOutputStream()) {
                try (InputStream is = vloConfigLocation.toURL().openStream()) {
                    IOUtils.copy(is, os);
                }
            } catch (IOException ex) {
                logger.error("Could not read or write VloConfig", ex);
                throw new RuntimeException(ex);
            }
        }
    }

}
