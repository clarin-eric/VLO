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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.wicket.AbstractWicketTest;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.Model;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ContentSearchFormPanelTest extends AbstractWicketTest {

    @Inject
    private FieldNameService fieldNameService;
    @Inject
    private VloConfig vloConfig;

    @Override
    public void setUp() throws IOException {
        vloConfig.setFederatedContentSearchUrl("http://fcs.org/aggregator");
        super.setUp();
    }

    @Test
    public void testSearchFormPanel() {
        final SolrDocument document = new SolrDocument();
        document.setField(fieldNameService.getFieldName(FieldKey.SELF_LINK), "hdl:1234/selflink");

        final ContentSearchFormPanel panel = new ContentSearchFormPanel("panel", Model.of(document), Model.of("http://cqlEndPoint/"));
        getTester().startComponentInPage(panel);

        // form action should be aggregator search page
        getTester().assertContains("action=\"http://fcs.org/aggregator\"");
        //tester.assertContains("action=\"http://weblicht.sfs.uni-tuebingen.de/Aggregator/\"");
        // json hidden input should have the CQL endpoint and document handle, and should be encoded into entities
        getTester().assertContains(Pattern.quote("name=\"x-aggregation-context\" "
                + "value=\"{\n"
                + "  &quot;http://cqlEndPoint/&quot;: [\n"
                + "    &quot;hdl:1234/selflink&quot;\n"
                + "  ]\n"
                + "}\""));
    }

    /**
     * Overrides returned application configuration
     */
    @Configuration
    @Import({VloSolrSpringConfig.class, AbstractWicketTest.WicketBaseContextConfiguration.class})
    static class ContextConfiguration {
    }

}
