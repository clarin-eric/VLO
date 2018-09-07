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
package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ProfileXsdWalkerTestImpl extends ProfileXsdWalker<Map<String, Object>> {

    public ProfileXsdWalkerTestImpl(VloConfig vloConfig, String xsd, Boolean useLocalXSDCache) {
        super(vloConfig, xsd, useLocalXSDCache);
    }

    @Override
    protected Map<String, Object> createResultObject() {
        return new HashMap<>();
    }

    @Override
    protected void processElement(VTDNav vn, LinkedList<Token> elementPath, Map<String, Object> result) throws NavException, URISyntaxException {
        final Pattern pattern = createXpath(elementPath, null);
        result.put(pattern.getPattern(), pattern);
    }

    @Override
    protected void processAttribute(VTDNav vn, LinkedList<Token> elementPath, Map<String, Object> result) throws URISyntaxException, NavException {
        int attributeNameIndex = vn.getAttrVal("name");
        if (attributeNameIndex != -1) {
            String attributeName = vn.toNormalizedString(attributeNameIndex);
            final Pattern pattern = createXpath(elementPath, attributeName);
            result.put(pattern.getPattern(), pattern);
        }
    }

    public static void main(String[] args) throws Exception {
        VloConfig config = new DefaultVloConfigFactory().newConfig();
        String xsd = "clarin.eu:cr1:p_1297242111880";
        ProfileXsdWalkerTestImpl walker = new ProfileXsdWalkerTestImpl(config, xsd, false);
        Map<String, Object> result = walker.walkProfile();
        result.entrySet().forEach((s)
                -> System.out.println(s.getKey())
        );
    }

}
