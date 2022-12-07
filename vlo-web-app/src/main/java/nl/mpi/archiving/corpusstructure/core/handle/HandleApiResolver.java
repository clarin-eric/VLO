/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.archiving.corpusstructure.core.handle;

import java.net.URI;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;

/**
 *
 * @author wilelb
 */
public class HandleApiResolver implements HandleResolver {

    private final static String GLOBAL_RESOLVER = "hdl.handle.net";
    
    //Intialization with handle-client 7.0, currently not compatible with corpusstructure-api
    //protected final HSAdapter api = HSAdapterFactory.newInstance();
    
    //Initialization with handle.jar 6.0
    protected final net.handle.hdllib.HandleResolver api = new net.handle.hdllib.HandleResolver();
    
    
    @Override
    public URI resolve(URI uri) throws InvalidHandleException {
        try {
            if(uri.getScheme() == null) {
                throw new InvalidHandleException("Scheme is required");
            }
            if(uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                if(uri.getHost().equals(GLOBAL_RESOLVER)) {
                    String hdl = uri.getPath();
                    if(hdl.startsWith("/")) {
                        hdl = hdl.substring(1, hdl.length());
                    }
                    if(hdl.startsWith("hdl:")) {
                        hdl = hdl.substring(4,hdl.length());
                    }
                    return URI.create(resolve(hdl));
                } else {
                    throw new InvalidHandleException("http(s) handle uri did not start with "+GLOBAL_RESOLVER);
                }
            } else if(uri.getScheme().equalsIgnoreCase("hdl")) {
                return URI.create(resolve(uri.getRawSchemeSpecificPart()));
            } else {
                throw new InvalidHandleException("Handle uri scheme ["+uri.getScheme()+"] is invalid.");
            } 
        } catch(HandleException ex) {
            throw new InvalidHandleException("Handle resolution error", ex);
        } 
    }
    
    protected String resolve(String handle) throws HandleException {
        String[] types = new String[] {"URL"};
        HandleValue[] values = api.resolveHandle(handle, types, null);
        return values[0].getDataAsString();
    }
}
