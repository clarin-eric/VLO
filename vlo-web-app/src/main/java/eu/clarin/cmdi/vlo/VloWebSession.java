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
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import java.io.Serializable;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.time.Time;

/**
 *
 * @author twagoo
 */
public class VloWebSession extends WebSession {

    private static final String SELECTION_TYPE_ATTRIBUTE_NAME = "selectionType";

    private final Time initTime;

    public VloWebSession(Request request) {
        super(request);
        this.initTime = Time.now();
    }

    public static VloWebSession get() {
        return (VloWebSession) WebSession.get();
    }

    public FacetSelectionType getFacetSelectionTypeMode() {
        final Serializable value = getAttribute(SELECTION_TYPE_ATTRIBUTE_NAME);
        if (value instanceof FacetSelectionType) {
            return (FacetSelectionType) value;
        } else {
            return null;
        }
    }

    public void setFacetSelectionTypeMode(FacetSelectionType value) {
        setAttribute(SELECTION_TYPE_ATTRIBUTE_NAME, value);
    }

    public Time getInitTime() {
        return initTime;
    }

}
