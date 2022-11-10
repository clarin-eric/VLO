/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.rules.transformation;

import com.google.common.base.Functions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlAccessorType(XmlAccessType.NONE)
@Slf4j
public class ValueMapTransformer extends BaseTransformer {

    @XmlTransient
    private final Supplier<Function<ValueLanguagePair, ValueLanguagePair>> mapperSupplier;
    @XmlTransient
    private final Supplier<Function<String, String>> keyNormalizerSupplier;

    @XmlAttribute
    @Getter
    @Setter
    private boolean regex = false;

    @XmlAttribute
    @Getter
    @Setter
    private boolean caseSensitive = false;

    @XmlAttribute
    @Getter
    @Setter
    private String targetLang = null;

    @XmlAttribute
    @Getter
    @Setter
    private String defaultValue = null;

    @XmlElement(name = "map")
    @Getter
    @Setter
    private Map<String, String> map;

    public ValueMapTransformer() {
        this(null);
    }

    public ValueMapTransformer(String field) {
        super(field);
        this.mapperSupplier = Suppliers.memoize(this::createMapperFunction);
        this.keyNormalizerSupplier = Suppliers.memoize(this::createKeyNormalizer);
    }

    @Override
    public Stream<ValueLanguagePair> apply(ValueContext valueContext, VloMappingConfiguration mappingConfig) {
        final Function<ValueLanguagePair, ValueLanguagePair> mapper = mapperSupplier.get();
        return Streams.stream(valueContext.getValues())
                // apply mapper
                .map(mapper::apply);
    }

    /**
     * Creates the function that maps value/language pairs to target
     * value/language pairs, depending on the settings on this transformer
     *
     * @return Function that can be applied to input
     */
    private Function<ValueLanguagePair, ValueLanguagePair> createMapperFunction() {
        log.debug("Creating a mapper function for a value map transformer for field {}", field);
        if (map != null) {
            if (regex) {
                return createRegexMapperFunction();
            } else {
                return createNonRegexMapperFunction();
            }
        } else {
            //TODO: load map from elsewhere??
            throw new UnsupportedOperationException("Cannot create mapper function without map set");
        }
    }

    /**
     * Creates a map based function for looking up values and their targets
     *
     * @return Function that can be applied to input
     */
    private Function<ValueLanguagePair, ValueLanguagePair> createNonRegexMapperFunction() {
        log.debug("Creating mapper function for simple (non-regex) lookup");

        // normalization
        final Function<String, String> normalizer = keyNormalizerSupplier.get();
        final ImmutableMap<String, String> normalizedMap = getNormalizedMap(map, normalizer);

        // base lookup function
        final Function<ValueLanguagePair, Optional<ValueLanguagePair>> lookup
                = (vlp) -> Optional.ofNullable(normalizedMap.get(normalizer.apply(vlp.getValue())))
                        .map(s -> new ValueLanguagePair(s, targetLang));

        // complete function depending on default result
        if (defaultValue == null) {
            // function returning original if no match
            return vlp -> lookup.apply(vlp).orElse(vlp);
        } else {
            // function returning default value language pair if no match
            final ValueLanguagePair defaultVlp = new ValueLanguagePair(defaultValue, targetLang);
            return vlp -> lookup.apply(vlp).orElse(defaultVlp);
        }
    }

    private Function<String, String> createKeyNormalizer() {
        if (caseSensitive) {
            return Functions.identity();
        } else {
            return String::toLowerCase;
        }
    }

    /**
     * Normalizes the keys of a map with a provided normalizer
     *
     * @param <K> key type
     * @param <V> value type
     * @param map map to normalize
     * @param keyNormalizer normalizer function to be applied to keys
     * @return Immutable, materialized map with normalized keys
     */
    private static <K, V> ImmutableMap<K, V> getNormalizedMap(Map<K, V> map, Function<K, K> keyNormalizer) {
        return ImmutableMap.copyOf(
                Iterables.transform(
                        map.entrySet(),
                        e -> Maps.immutableEntry(
                                keyNormalizer.apply(e.getKey()),
                                e.getValue())));
    }

    private Function<ValueLanguagePair, ValueLanguagePair> createRegexMapperFunction() {
        log.debug("Creating mapper function for regex based lookup");

        final int patternFlags = getRegexFlags();

        // convert map to list of entries of compiled Patterns - target value
        final List<Entry<Pattern, String>> regexRules
                = ImmutableList.copyOf(
                        Iterables.transform(map.entrySet(),
                                entry -> Maps.immutableEntry(
                                        Pattern.compile(entry.getKey(), patternFlags),
                                        entry.getValue())));

        // match finder function
        final Function<ValueLanguagePair, Optional<ValueLanguagePair>> matchFinder = vlp -> {
            return regexRules.stream() // iterate over all expressions
                    // actual matching
                    .filter(e -> e.getKey().matcher(vlp.getValue()).matches())
                    // we only need one match
                    .findAny()
                    // if found, map to value language pair
                    .map(e -> new ValueLanguagePair(e.getValue(), targetLang));
        };

        // complete function depending on default result
        if (defaultValue == null) {
            // function returning original if there is no match
            return vlp -> matchFinder.apply(vlp).orElse(vlp);
        } else {
            // function returning default value language pair if there is no match
            final ValueLanguagePair defaultVlp = new ValueLanguagePair(defaultValue, targetLang);
            return vlp -> matchFinder.apply(vlp).orElse(defaultVlp);
        }
    }

    private int getRegexFlags() {
        int patternFlags = 0;
        if (!caseSensitive) {
            patternFlags |= Pattern.CASE_INSENSITIVE;
        }
        return patternFlags;
    }

}
