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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    private Function<ValueLanguagePair, ValueLanguagePair> createMapperFunction() {
        log.debug("Creating a mapper function for a value map transformer for field {}", field);
        if (map != null) {
            if (regex) {
                //TODO: regex based lookup
                return Functions.identity();
            } else {
                return createNonRegexMapperFunction();
            }
        } else {
            return Functions.identity();
        }
    }

    private Function<ValueLanguagePair, ValueLanguagePair> createNonRegexMapperFunction() {
        // normalization
        final Function<String, String> normalizer = keyNormalizerSupplier.get();
        final ImmutableMap<String, String> normalizedMap = getNormalizedMap(map, normalizer);

        // base lookup function
        final Function<ValueLanguagePair, Optional<ValueLanguagePair>> lookup
                = (vlp) -> Optional.ofNullable(normalizedMap.get(normalizer.apply(vlp.getValue())))
                        .map(s -> new ValueLanguagePair(s, targetLang));

        // complete function depending on default behaviour
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

    private static <K, V> ImmutableMap<K, V> getNormalizedMap(Map<K, V> map, Function<K, K> keyNormalizer) {
        return ImmutableMap.copyOf(
                Iterables.transform(
                        map.entrySet(),
                        e -> Maps.immutableEntry(
                                keyNormalizer.apply(e.getKey()),
                                e.getValue())));
    }

}
