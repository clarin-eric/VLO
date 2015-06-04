package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class IdPostProcessor implements PostProcessor {

    /**
     * Return normalized String
     *
     * @param value String that will be normalized
     * @return normalized version of value
     */
    @Override
    public List<String> process(String value) {
        List<String> resultList = new ArrayList<String>();
        resultList.add(StringUtils.normalizeIdString(value));
        return resultList;
    }
}
