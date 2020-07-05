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

package com.hazelcast.spi.tenantcontrol;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.annotation.Beta;

/**
 * Hook to decouple Hazelcast object from the tenant
 */
@Beta
public interface DestroyEventContext {

    /**
     * Called to decouple Hazelcast object from the tenant
     *
     * @param instance to use to decouple the Hazelcast object from tenant
     */
    void tenantUnavailable(HazelcastInstance instance);

    /**
     *
     * @return the name of the distributed object
     */
    String getDistributedObjectName();

    /**
     *
     * @return the service name
     */
    String getServiceName();
}
