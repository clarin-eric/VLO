/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.archiving.corpusstructure.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class URLConnections {

    private final static Logger logger = LoggerFactory.getLogger(URLConnections.class);

    public static final int DEFAULT_MAX_REDIRECTS = 5;
    private final int maxRedirects;

    public URLConnections() {
        this(DEFAULT_MAX_REDIRECTS);
    }

    public URLConnections(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    //As suggested by: http://download.java.net/jdk7/archive/b123/docs/technotes/guides/deployment/deployment-guide/upgrade-guide/article-17.html
    public URL getUrlCheckRedirects(URLConnection c) throws IOException {
        boolean redir;
        int redirects = 0;
        URL result = c.getURL();
        try {
            do {
                if (c instanceof HttpURLConnection) {
                    ((HttpURLConnection) c).setInstanceFollowRedirects(false);
                }
                // We want to open the input stream before getting headers
                // because getHeaderField() et al swallow IOExceptions.
                c.getInputStream();
                redir = false;
                if (c instanceof HttpURLConnection) {
                    final HttpURLConnection http = (HttpURLConnection) c;
                    int stat = http.getResponseCode();
                    if (stat >= 300 && stat <= 307 && stat != 306
                            && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                        final URL target = handleRedirect(http, redirects);
                        redir = true;
                        c = target.openConnection();
                        redirects++;
                        result = target;
                    }
                }
            } while (redir);
        } catch (IOException ex) {
            logger.error("IOException: {}", result);
            logger.debug("IOException", ex);
        }
        return result;
    }

    public InputStream openStreamCheckRedirects(URLConnection c) throws IOException {
        boolean redir;
        int redirects = 0;
        InputStream in = null;
        do {
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            // We want to open the input stream before getting headers
            // because getHeaderField() et al swallow IOExceptions.
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                final HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306
                        && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    final URL target = handleRedirect(http, redirects);
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        } while (redir);
        return in;
    }

    private URL handleRedirect(HttpURLConnection http, int redirectCount) throws SecurityException, MalformedURLException {
        final URL base = http.getURL();
        final String loc = http.getHeaderField("Location");

        final URL target;
        if (loc == null) {
            target = null;
        } else {
            target = new URL(base, loc);
        }

        http.disconnect();
        // Redirection should be allowed only for HTTP and HTTPS
        // and should be limited to N redirections at most.
        if (target == null
                || !(target.getProtocol().equals("http") || target.getProtocol().equals("https"))
                || redirectCount >= maxRedirects) {
            throw new SecurityException("illegal URL redirect");
        }
        return target;
    }
}
