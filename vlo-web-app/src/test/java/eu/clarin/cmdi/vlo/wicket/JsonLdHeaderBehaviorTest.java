/*
 * Copyright (C) 2020 CLARIN
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class JsonLdHeaderBehaviorTest extends AbstractWicketTest {
    
    protected JSONObject getJsonFromDoc(final String document) throws ParseException {
        assertTrue(document.contains("<script type=\"application/ld+json\">"));
        String scriptSection = document.substring(document.indexOf("<script type=\"application/ld+json\">"));
        scriptSection = scriptSection.substring(0, scriptSection.indexOf("</script>"));
        String jsonString = scriptSection.substring(scriptSection.indexOf("{"));
        jsonString = jsonString.substring(0, jsonString.lastIndexOf("}") + 1);
        assertTrue(jsonString.contains("\"url\":"));
        Object jsonObj = new JSONParser().parse(jsonString);
        assertTrue(jsonObj instanceof JSONObject);
        return (JSONObject) jsonObj;
    }

    
}
