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

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.string.StringValue;

/**
 * Extension of {@link MountedMapper} that adds support for a special page
 * parameter {@link #FRAGMENT_PAGE_PARAMETER #} that translates into a fragment
 * in the URL rather than a query parameter
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @see PageParameters
 * @see
 * https://cwiki.apache.org/confluence/display/WICKET/FAQs#FAQs-Howtoadd#-anchor(opaque)topageurl?
 */
public class FragmentEncodingMountedMapper extends MountedMapper {

    public static final String FRAGMENT_PAGE_PARAMETER = "#";

    public FragmentEncodingMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
        super(mountPath, pageClass);
    }

    public FragmentEncodingMountedMapper(String mountPath, IProvider<Class<? extends IRequestablePage>> pageClassProvider) {
        super(mountPath, pageClassProvider);
    }

    public FragmentEncodingMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass, IPageParametersEncoder pageParametersEncoder) {
        super(mountPath, pageClass, pageParametersEncoder);
    }

    public FragmentEncodingMountedMapper(String mountPath, IProvider<Class<? extends IRequestablePage>> pageClassProvider, IPageParametersEncoder pageParametersEncoder) {
        super(mountPath, pageClassProvider, pageParametersEncoder);
    }

    @Override
    protected Url encodePageParameters(Url url, PageParameters pageParameters, IPageParametersEncoder encoder) {
        //get special page parameter that encodes the page fragment/anchor
        final StringValue fragment = pageParameters.get(FRAGMENT_PAGE_PARAMETER);

        if (fragment.isEmpty()) {
            //business as usual
            return super.encodePageParameters(url, pageParameters, encoder);
        } else {
            //we have a fragment parameter, remove from normal parameters and do post-processing
            pageParameters.remove(FRAGMENT_PAGE_PARAMETER);
            
            final Url result = super.encodePageParameters(url, pageParameters, encoder);
            
            //set the fragment on the resulting URL
            result.setFragment(fragment.toString());
            return result;
        }
    }
}
