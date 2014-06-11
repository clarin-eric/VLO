package eu.clarin.cmdi.vlo.importer;

import java.util.Arrays;
import java.util.List;


public class OrganisationPostProcessor implements PostProcessor {

    /**
     * Splits values for organisation facet at delimiter ';'
     * @param value extracted organisation name/names
     * @return List of organisation names (splitted at semicolon)
     */
    @Override
    public List<String> process(String value) {
        String[] splitArray = value.split(";");
        return Arrays.asList(splitArray);
    }
}
