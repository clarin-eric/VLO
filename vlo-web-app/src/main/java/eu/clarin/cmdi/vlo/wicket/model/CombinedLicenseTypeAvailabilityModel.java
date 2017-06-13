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
package eu.clarin.cmdi.vlo.wicket.model;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.Collection;
import java.util.Collections;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Model that combines information from the 'license type' and 'availability'
 * fields, excluding the license type information from the latter to prevent
 * an overlap or contradiction.
 *
 * Background information can be found in VLO issue #55 ({@link https://github.com/clarin-eric/VLO/issues/55})
 * 
 * @author twagoo
 * @see FacetConstants#FIELD_AVAILABILITY
 * @see FacetConstants#FIELD_LICENSE_TYPE
 */
public class CombinedLicenseTypeAvailabilityModel extends AbstractReadOnlyModel<Collection<String>> {

    private final IModel<Collection<String>> licenseTypeModel;
    private final IModel<Collection<String>> availabilityModel;

    public CombinedLicenseTypeAvailabilityModel(IModel<Collection<String>> licenseTypeModel, IModel<Collection<String>> availabilityModel) {
        this.licenseTypeModel = licenseTypeModel;
        this.availabilityModel = availabilityModel;
    }

    @Override
    public Collection<String> getObject() {
        final Collection<String> availability = availabilityModel.getObject();
        final Collection<String> licenseType = licenseTypeModel.getObject();
        return ImmutableList.copyOf(
                Iterables.concat(
                        //license type
                        (licenseType == null ? Collections.<String>emptyList() : licenseType),
                        //availability properties with license types filtered out
                        (availability == null ? Collections.<String>emptyList()
                                : Iterables.filter(availability, new Predicate<String>() {
                                    @Override
                                    public boolean apply(String input) {
                                        return !FacetConstants.LICENSE_TYPE_VALUES.contains(input);
                                    }
                                }))
                )
        );
    }

    @Override
    public void detach() {
        availabilityModel.detach();
        licenseTypeModel.detach();
    }

}
