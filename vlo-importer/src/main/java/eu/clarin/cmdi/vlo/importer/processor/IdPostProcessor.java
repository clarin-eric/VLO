package eu.clarin.cmdi.vlo.importer.processor;

import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.importer.CMDIData;

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
    public List<String> process(String value, CMDIData cmdiData) {
        List<String> resultList = new ArrayList<String>();
        resultList.add(StringUtils.normalizeIdString(value));
        return resultList;
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
