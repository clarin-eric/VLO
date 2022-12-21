/*
 * Copyright (C) 2022 twagoo
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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import eu.clarin.cmdi.vlo.mapping.model.ValueContextImpl;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import eu.clarin.cmdi.vlo.mapping.model.XPathAware;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author twagoo
 */
@ToString(exclude = {"nav"})
@Slf4j
public class VTDValueContext extends ValueContextImpl implements XPathAware {

    private final VTDNav nav;
    private final String profileId;

    public VTDValueContext(VTDValueContext context) {
        super(context);
        this.nav = context.getNav();
        this.profileId = context.getProfileId();
    }

    public VTDValueContext(VTDValueContext context, Iterable<ValueLanguagePair> values) {
        super(context, values);
        this.nav = context.getNav();
        this.profileId = context.getProfileId();
    }

    public VTDValueContext(Context context, Iterable<ValueLanguagePair> values, String profileId, VTDNav nav) {
        super(context, values);
        this.nav = nav;
        this.profileId = profileId;
    }

    protected VTDNav getNav() {
        return nav;
    }

    protected String getProfileId() {
        return profileId;
    }

    @Override
    public boolean matchesXPath(String xPath) {
        try {
            final AutoPilot ap = new AutoPilot(nav.cloneNav());
            RecordReaderImpl.setNameSpace(ap, profileId);

            // determine index for context xpath (TODO: memoize this?)
            ap.selectXPath(getXpath());
            final int contextIdx = ap.evalXPath();

            // look for matching index for target xPath
            ap.resetXPath();
            ap.selectXPath(xPath);
            int targetIdx = -1;
            while ((targetIdx = ap.evalXPath()) != -1) {
                if (targetIdx == contextIdx) {
                    return true;
                }
            }
            return false;
        } catch (VTDException ex) {
            log.error("Error while evaluating xpath for context: {}, {}", xPath, this, ex);
            return false;
        }
    }

}
