package eu.clarin.cmdi.vlo.pojo;

import java.util.ArrayList;
import java.util.List;

public class Constants {
	
	//PATHS
	public static final String CSV_PATH = "maps/csv/";
	public static final String EXCEL_PATH = "maps/excel/";
	public static final String MAPS_PATH = "maps/uniform_maps/";
	
	public static final String TEST_CSV_PATH = "src/test/resources/csv";
	public static final String TEST_XML_PATH = "src/test/resources/xml";
	public static final String TEST_MAPS_PATH = "src/test/resources/maps";
	
	//NAMES OF THE MAPS
	public static final String CROSSMAPPING = "CrossMapping";
	public static final String LANGUAGE_CODE = "LanguageNameVariantsMap";
	public static final String LICENCE = "LicenseAvailabilityMap";
	public static final String RESOURCE_TYPE = "ACDH_resourceType";
	public static final String NATIONAL_PROJECT = "nationalProjectsMapping";
	public static final String ORGANISATION = "OrganisationControlledVocabulary";
	public static final String PROFILE = "profileName_resourceType_map";
	
	
	public static final List<String> maps = new ArrayList<String>();
	
	static{
//		maps.add(LANGUAGE_CODE);
//		maps.add(LICENCE);
//		maps.add(NATIONAL_PROJECT);
//		maps.add(ORGANISATION);
		maps.add(LICENCE);
	}
	
	
	//NAMES OF THE COLUMNS FOR THE CSV FILE
	public static final String REGULAR_EXPRESSION = "REGULAR_EXPRESSION";
	public static final String COUNT = "COUNT";
	public static final String NORMALIZED_VALUE = "NORMALIZED_VALUE";
	public static final String REMARKS = "REMARKS";
	
	
}
