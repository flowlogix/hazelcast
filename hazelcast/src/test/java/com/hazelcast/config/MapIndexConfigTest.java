/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.hazelcast.config.MapIndexConfig.validateIndexAttribute;
import static com.hazelcast.test.HazelcastTestSupport.assumeDifferentHashCodes;
import static org.junit.Assert.assertEquals;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class MapIndexConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void testValidation() {
        validateIndexAttribute(null);
    }

    @Test
    public void testValidation_withKeyNonKeyword() {
        assertEquals("__key.this", validateIndexAttribute("__key.this"));
    }

    @Test
    public void testValidation_withKeyKeyword() {
        assertEquals("__key#value", validateIndexAttribute("__key#value"));
    }

    @Test
    public void testEqualsAndHashCode() {
        assumeDifferentHashCodes();
        EqualsVerifier.forClass(MapIndexConfig.class)
                .allFieldsShouldBeUsedExcept("readOnly")
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(MapIndexConfigReadOnly.class,
                        new MapIndexConfigReadOnly(new MapIndexConfig("red", false)),
                        new MapIndexConfigReadOnly(new MapIndexConfig("black", true)))
                .verify();
    }
}
