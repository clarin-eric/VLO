package eu.clarin.cmdi.vlo.importer;

import java.util.Arrays;
import java.util.List;

public class LicensePostProcessor extends PostProcessorsWithVocabularyMap{

	@Override
	public List<String> process(String value) {
		return Arrays.asList(normalize(value, "--"));
	}

	@Override
	public String getNormalizationMapURL() {
		return MetadataImporter.config.getLicenseURIMapUrl();
	}
}
