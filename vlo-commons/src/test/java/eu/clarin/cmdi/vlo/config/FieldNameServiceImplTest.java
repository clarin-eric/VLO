/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.FieldKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FieldNameServiceImplTest {

    private VloConfig vloConfig;
    private HashMap<String, String> fieldsMap;

    @Before
    public void setUp() {
        vloConfig = new VloConfig();
        fieldsMap = new HashMap<>();
        vloConfig.setFields(fieldsMap);
    }
    
    /**
     * Test of getFieldName method, of class FieldNameServiceImpl.
     */
    @Test
    public void testGetFieldName() {
        fieldsMap.put(FieldKey.FORMAT.toString(), "testField");
        final FieldNameServiceImpl instance = new FieldNameServiceImpl(vloConfig);
        assertEquals("testField", instance.getFieldName(FieldKey.FORMAT));
    }

    private final static int BENCHMARK_RUNS = 5000;

    //@Test
    public void testPerformance() {
        final ImmutableMap.Builder<String, String> fieldKeysMap = ImmutableMap.builder();
        Stream.of(FieldKey.values())
                .forEach(k -> fieldKeysMap.put(k.name(), k.name()));
        vloConfig.setFields(fieldKeysMap.build());

        final List<List<FieldKey>> fieldKeyLists
                = IntStream.range(0, BENCHMARK_RUNS).boxed()
                        .map(n -> Lists.newArrayList(FieldKey.values()))
                        .collect(Collectors.toList());
        fieldKeyLists.forEach(Collections::shuffle);

        final FieldNameService classic = new FieldNameServiceClassic(vloConfig);
        final FieldNameService instance = new FieldNameServiceImpl(vloConfig);

        final int n = 100;
        double[] instancePerf = new double[n];
        double[] classicPerf = new double[n];
        for (int i = 0; i < n; i++) {
            classicPerf[i] = runPerformanceTest(classic, fieldKeyLists);
            System.out.println("");
            instancePerf[i] = runPerformanceTest(instance, fieldKeyLists);
        }

        double instanceAvg = DoubleStream.of(instancePerf).average().getAsDouble();
        double classicAvg = DoubleStream.of(classicPerf).average().getAsDouble();

        System.out.println("Difference: " + (instanceAvg - classicAvg) + " = " + ((instanceAvg - classicAvg) / (.01 * classicAvg)) + "%");
    }

    private double runPerformanceTest(FieldNameService instanz, final List<List<FieldKey>> fieldKeyLists) {

        final long start = System.currentTimeMillis();
        System.out.println("Starting test for" + instanz.getClass().getSimpleName() + ": " + start);

        fieldKeyLists
                .forEach(l -> l
                .forEach(k -> instanz.getFieldName(k)));

        final long end = System.currentTimeMillis();
        System.out.println("Done: " + end);

        final long total = end - start;
        System.out.println("Total time: " + total);
        final double avg = new Double(total) / BENCHMARK_RUNS;
        System.out.println("Average time: " + avg);
        return total;
    }

    public final class FieldNameServiceClassic implements FieldNameService {

        private VloConfig vloConfig;

        public FieldNameServiceClassic(VloConfig vloConfig) {
            this.vloConfig = vloConfig;
        }

        @Override
        public String getFieldName(FieldKey key) {
            return this.vloConfig.getFields().get(key.toString());
        }

        public String getDeprecatedFieldName(FieldKey key) {
            return this.vloConfig.getDeprecatedFields().get(key.toString());
        }
    }

}
