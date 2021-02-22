/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.config;

import com.hazelcast.config.DurableExecutorConfig;

public class DurableExecutorConfigReadOnly extends DurableExecutorConfig {

    public DurableExecutorConfigReadOnly(DurableExecutorConfig config) {
        super(config);
    }

    @Override
    public DurableExecutorConfig setName(String name) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }

    @Override
    public DurableExecutorConfig setPoolSize(int poolSize) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }

    @Override
    public DurableExecutorConfig setCapacity(int capacity) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }

    @Override
    public DurableExecutorConfig setDurability(int durability) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }

    @Override
    public DurableExecutorConfig setSplitBrainProtectionName(String splitBrainProtectionName) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }

    @Override
    public DurableExecutorConfig setStatisticsEnabled(boolean statisticsEnabled) {
        throw new UnsupportedOperationException("This config is read-only durable executor: " + getName());
    }
}
