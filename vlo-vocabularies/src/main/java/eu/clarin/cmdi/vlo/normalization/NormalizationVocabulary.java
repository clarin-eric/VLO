package eu.clarin.cmdi.vlo.normalization;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Collection;
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

    private ImmutableMap<String, Integer> normalizationMap = null;
    private ImmutableList<Integer> regExEntries = null;
    private ImmutableList<VocabularyEntry> entries = null;

    // false is default because it is a special case when patterns are used in maps
    private boolean searchInRegExes = false;

    public NormalizationVocabulary(List<VocabularyEntry> entries, boolean searchInRegExes) {
        this(entries);
        this.searchInRegExes = searchInRegExes;
    }

    public NormalizationVocabulary(List<VocabularyEntry> entries) {
        // collect regex and mapping entries
        final Map<String, Integer> map = new HashMap<>();
        final List<Integer> list = new LinkedList<>();

        for (int i = 0; i < entries.size(); i++) {
            final VocabularyEntry entry = entries.get(i);
            if (entry.isRegEx()) {
                list.add(i);
            } else {
                map.put(entry.getOriginalVal(), i);
            }
        }

        // store as immutable copies
        this.entries = ImmutableList.copyOf(entries);
        this.regExEntries = ImmutableList.copyOf(list);
        this.normalizationMap = ImmutableMap.copyOf(map);
    }

    @Override
    public String normalize(String value) {
        VocabularyEntry hit = getEntry(value);
        return (hit != null) ? hit.getNormalizedValue() : null;
    }

    @Override
    public Map<String, String> getCrossMappings(String value) {
        VocabularyEntry hit = getEntry(value);
        return (hit != null) ? hit.getCrossMap() : null;
    }

    public VocabularyEntry getEntry(String value) {
        Integer index = normalizationMap.get(value);

        // no hit -> check in patterns if option set
        if (index == null && searchInRegExes) {
            final Optional<Integer> regexIndex = Iterables.tryFind(regExEntries, (regExIndex) -> {
                return Pattern.compile(entries.get(regExIndex).getOriginalVal()).matcher(value).find();
            });
            if (regexIndex.isPresent()) {
                index = regexIndex.get();
            }
        }

        return (index != null) ? entries.get(index) : null;
    }

    public Collection<VocabularyEntry> getEntries() {
        return entries;
    }

}
