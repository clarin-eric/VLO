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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;
import org.apache.wicket.model.IModel;

/**
 * Model that wraps a model providing a file location and returns a URL for this
 * location via {@link #getObject() }
 *
 * @author twagoo
 */
public class UrlFromStringModel implements IModel<URL> {
    
    private final static Pattern URL_PATTERN = Pattern.compile("^(https?:\\/\\/|file:\\/).*");
    private final IModel<String> model;

    /**
     *
     * @param model model that provides a file location
     */
    public UrlFromStringModel(IModel<String> model) {
        this.model = model;
    }
    
    @Override
    public URL getObject() {
        final String object = model.getObject();
        if (object == null) {
            return null;
        } else {
            try {
                if (URL_PATTERN.matcher(object).matches()) {
                    return new URL(object);
                } else {
                    return new File(object).toURI().toURL();
                }
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    @Override
    public void setObject(URL object) {
        if (object == null) {
            model.setObject(null);
        } else {
            final String scheme = object.getProtocol().toLowerCase();
            if (scheme.equals("http") || scheme.equals("https")) {
                model.setObject(object.toString());
            } else {
                try {
                    model.setObject(new File(object.toURI()).getAbsolutePath());
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    @Override
    public void detach() {
        model.detach();
    }
    
}
