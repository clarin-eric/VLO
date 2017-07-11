/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class PreferredExplicitOrdering<T> extends Ordering<T> implements Serializable {

    private final List<T> preferredItems;

    public PreferredExplicitOrdering(List<T> preferredItems) {
        this.preferredItems = preferredItems;
    }

    @Override
    public int compare(T left, T right) {
        final int leftIndex = preferredItems.indexOf(left);
        final int rightIndex = preferredItems.indexOf(right);
        if (leftIndex >= 0) {
            if (rightIndex >= 0) {
                //both left and right are in the preferred list, compare position
                return leftIndex - rightIndex;
            } else {
                //only left in the preferred list, so left comes first
                return -1;
            }
        } else if (rightIndex >= 0) {
            //only right in the preferred list, so right comes first
            return 1;
        } else {
            //neither in the preferred list, do not change order
            return 0;
        }
    }

}
