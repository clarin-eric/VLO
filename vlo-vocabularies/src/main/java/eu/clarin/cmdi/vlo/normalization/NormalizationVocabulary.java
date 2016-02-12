package eu.clarin.cmdi.vlo.normalization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author dostojic
 *
 */
public class NormalizationVocabulary implements NormalizationService {

	private Map<String, Integer> normalizationMap = null;
	private List<Integer> regExEntries = null;
	private VocabularyEntry[] entries = null;

	// false is default because it is a special case when patterns are used in maps
	private boolean searchInRegExes = false; 

	public NormalizationVocabulary(VocabularyEntry[] entries, boolean searchInRegExes) {
		this(entries);
		this.searchInRegExes = searchInRegExes;
	}

	public NormalizationVocabulary(VocabularyEntry[] entries) {
		this.entries = entries;

		for (int i = 0; i < entries.length; i++) {
			if (entries[i].isRegEx()) {
				if (regExEntries == null)
					regExEntries = new LinkedList<Integer>();
				regExEntries.add(i);
			} else {
				if (normalizationMap == null)
					normalizationMap = new HashMap<String, Integer>();
				normalizationMap.put(entries[i].getOriginalVal(), i);
			}
		}
	}
	
	public List<String> normalize(String value) {
		VocabularyEntry hit = getEntry(value);
		return (hit != null) ? hit.getNormalizedValue() : null;
	}

	public Map<String, String> getCrossMappings(String value) {
		VocabularyEntry hit = getEntry(value);
		return (hit != null) ? hit.getCrossMap() : null;
	}

	public VocabularyEntry getEntry(String value) {
		Integer index = null;

		index = normalizationMap.get(value);

		// no hit -> check in patterns if option set
		if (index == null && searchInRegExes)
			for (Integer regExIndex : regExEntries) {
				if (Pattern.compile(entries[regExIndex].getOriginalVal()).matcher(value).find()) {
					index = regExIndex;
					break;
				}
			}

		return (index != null) ? entries[index] : null;
	}

	private boolean exists(VocabularyEntry entry) {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getOriginalVal().equals(entry.getOriginalVal()))
				return true;
		}

		return false;
	}
	

	public VocabularyEntry[] getEntries(){
		return entries;
	}

}
