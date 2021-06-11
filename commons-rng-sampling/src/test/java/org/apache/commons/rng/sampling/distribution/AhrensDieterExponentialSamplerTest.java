/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.rng.sampling.distribution;

import java.util.concurrent.TimeUnit;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.apache.commons.rng.sampling.RandomAssert;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Test for the {@link AhrensDieterExponentialSampler}. The tests hit edge cases for the sampler.
 */
public class AhrensDieterExponentialSamplerTest {

    /** The global timeout for tests. Used to kill a test stuck in an infinite loop. */
    @Rule
    public Timeout globalTimeout = new Timeout(50, TimeUnit.MILLISECONDS);

    /**
     * Test the constructor with a bad mean.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsWithZeroMean() {
        final RestorableUniformRandomProvider rng =
            RandomSource.SPLIT_MIX_64.create(0L);
        final double mean = 0;
        AhrensDieterExponentialSampler.of(rng, mean);
    }

    /**
     * Test the SharedStateSampler implementation.
     */
    @Test
    public void testSharedStateSampler() {
        final UniformRandomProvider rng1 = RandomSource.SPLIT_MIX_64.create(0L);
        final UniformRandomProvider rng2 = RandomSource.SPLIT_MIX_64.create(0L);
        final double mean = 1.23;
        final SharedStateContinuousSampler sampler1 =
            AhrensDieterExponentialSampler.of(rng1, mean);
        final SharedStateContinuousSampler sampler2 = sampler1.withUniformRandomProvider(rng2);
        RandomAssert.assertProduceSameSequence(sampler1, sampler2);
    }

    /**
     * Test the sampler is robust to a generator that outputs zeros.
     * See RNG-144.
     */
    @Test
    public void testSamplerWithZeroFromRandomGenerator() {
        // A broken generator that returns zero.
        final UniformRandomProvider rng = new SplitMix64(0) {
            @Override
            public long nextLong() {
                return 0L;
            }
        };
        final SharedStateContinuousSampler sampler = AhrensDieterExponentialSampler.of(rng, 1);
        // This should not infinite loop
        sampler.sample();
    }
}
