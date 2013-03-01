package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryNamePostProcessor implements PostProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(CountryNamePostProcessor.class);

    private Map<String, String> countryCodeMap;

    /**
     * Returns the country name based on the mapping defined in the CMDI component:
     * http://catalog.clarin.eu/ds/ComponentRegistry/?item=clarin.eu:cr1:c_1271859438104 If no mapping is found the original value is
     * returned.
     */
    @Override
    public String process(String value) {
        String result = value;
        if (result != null) {
            Map<String, String> countryCodeMap = getCountryCodeMap();
            String name = countryCodeMap.get(value.toUpperCase());
            if (name != null) {
                result = name;
            }
        }
        return result;
    }

    private Map<String, String> getCountryCodeMap() {
        if (countryCodeMap == null) {
            countryCodeMap = createCountryCodeMap();
        }
        return countryCodeMap;
    }

    private Map<String, String> createCountryCodeMap() {
        LOG.debug("Creating country code map.");
        try {
            Map<String, String> result = CommonUtils.createCMDIComponentItemMap(VloConfig.get().getCountryComponentUrl());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

}
