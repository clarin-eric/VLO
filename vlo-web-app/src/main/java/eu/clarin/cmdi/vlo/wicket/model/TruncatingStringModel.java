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

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * A read-only string model that provides the object of an inner string model if
 * it does not exceed a specified length, otherwise a truncated version (cut off
 * at a specified truncate point) of it
 *
 * @author twagoo
 */
public class TruncatingStringModel extends AbstractReadOnlyModel<String> {

    private final static String ELLIPSIS = "\u2026";
    private final IModel<String> model;
    private final int maxLength;
    private final int truncatePoint;

    /**
     *
     * @param model model that provides string to truncate if too long
     * @param maxLength maximum length to allow
     * @param truncatePoint point to truncate if string is too long
     */
    public TruncatingStringModel(IModel<String> model, int maxLength, int truncatePoint) {
        this.model = model;
        this.maxLength = maxLength;
        this.truncatePoint = truncatePoint;
    }

    /**
     * 
     * @return the inner model object or a truncated version of it; or null
     * in case of a null inner model object
     */
    @Override
    public String getObject() {
        final String object = model.getObject();
        if (object == null) {
            // null objects remain null
            return null;
        } else if (object.length() <= maxLength) {
            // short enough, return as it is
            return object;
        } else {
            // too long, truncate at truncate point and append ellipsis character
            return object.substring(0, truncatePoint) + ELLIPSIS;
        }
    }

}
