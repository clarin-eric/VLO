package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LicensePostProcessor extends PostProcessorsWithVocabularyMap{

	@Override
	public List<String> process(String value) {		
		String normalizedVal = normalize(value);
    	return normalizedVal != null? Arrays.asList(normalizedVal) : new ArrayList<>();
	}

	@Override
	public String getNormalizationMapURL() {
		return MetadataImporter.config.getLicenseURIMapUrl();
	}
}
