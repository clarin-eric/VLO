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
package eu.clarin.cmdi.vlo.wicket.components;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SmartLinkFieldValueLabelTest {

    @Test
    public void testRegex() {
        Pattern pattern = Pattern.compile(SmartLinkFieldValueLabel.URL_PATTERN);

        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("Treebanks and other CoNLL-U files (https://lindat.mff.cuni.cz/services/udpipe2)", "https://lindat.mff.cuni.cz/services/udpipe2")
                .put("Treebanks and other CoNLL-U files <https://lindat.mff.cuni.cz/services/udpipe2>", "https://lindat.mff.cuni.cz/services/udpipe2")
                .put("Treebanks and other CoNLL-U files: https://lindat.mff.cuni.cz/services/udpipe2.", "https://lindat.mff.cuni.cz/services/udpipe2")
                .put("This should be matched https://lindat.mff.cuni.cz/services/udpipe", "https://lindat.mff.cuni.cz/services/udpipe")
                .put("This should be matched https://lindat.mff.cuni.cz/services/udpipe too", "https://lindat.mff.cuni.cz/services/udpipe")
                .put("http://hdl.handle.net/abc-123@format=cmdi", "http://hdl.handle.net/abc-123@format=cmdi")
                .put("No url content", "")
                .put("No url content even in www.google.com", "")
                .put("No url content even inhttps://www.google.com", "")
                .build();

        map.forEach((text, target) -> {
            final Matcher matcher = pattern.matcher(text);
            if (target.isEmpty()) {
                assertFalse("Expected no match in '" + text + "'", matcher.find());
            } else {
                matcher.find();
                assertEquals("Expected match in '" + text + "'", target, matcher.group());
            }
        });
    }

}
