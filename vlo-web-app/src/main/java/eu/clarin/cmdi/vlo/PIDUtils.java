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
package eu.clarin.cmdi.vlo;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDUtils {

    public static boolean isPid(String uri) {
        return isHandle(uri);
        //|| isDoi(uri)
        //|| isUrn(uri)
    }

    public static boolean isHandle(String uri) {
        if (uri == null) {
            return false;
        } else {
            final String lcValue = uri.toLowerCase();
            return (lcValue.startsWith(FacetConstants.HANDLE_PREFIX))
                    || (lcValue.startsWith(FacetConstants.HANDLE_PROXY))
                    || (lcValue.startsWith(FacetConstants.HANDLE_PROXY_HTTPS));
        }
    }

}
