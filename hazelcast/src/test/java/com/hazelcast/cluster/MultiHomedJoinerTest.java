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
package com.hazelcast.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAddressProviderConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.spi.MemberAddressProvider;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import static com.hazelcast.test.TestEnvironment.HAZELCAST_TEST_USE_NETWORK;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import java.net.InetSocketAddress;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * tests multi-homed systems with wildcard addressing
 *
 * @author lprimak
 */
@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class MultiHomedJoinerTest extends HazelcastTestSupport {
    private TestHazelcastInstanceFactory fact;

    @BeforeClass
    public static void init() {
        System.setProperty(HAZELCAST_TEST_USE_NETWORK, Boolean.TRUE.toString());
    }

    @Before
    public void beforeRun() {
        fact = createHazelcastInstanceFactory(3);
    }

    @After
    public void afterRun() {
        fact.shutdownAll();
    }

    Config getConfig(String hostname) {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setMemberAddressProviderConfig(new MemberAddressProviderConfig().setEnabled(true)
                .setImplementation(new MemberAddressProviderImpl(hostname)));
        config.setProperty(ClusterProperty.WAIT_SECONDS_BEFORE_JOIN.getName(), "5");
        config.setProperty(ClusterProperty.MAX_WAIT_SECONDS_BEFORE_JOIN.getName(), "20");
        networkConfig.getMemberAddressProviderConfig().getProperties().setProperty("DynamicConfig", Boolean.TRUE.toString());
        config.getNetworkConfig().getJoin().setMulticastConfig(new MulticastConfig().setEnabled(false));

        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        if (hostname != null) {
            tcpIpConfig.addMember(hostname);
        }
        config.getNetworkConfig().getJoin().setTcpIpConfig(tcpIpConfig);
        return config;
    }

    @Test(timeout = 10 * 1000)
    public void wildcardAddressTest() {
        HazelcastInstance hz1 = fact.newHazelcastInstance(getConfig(null));
//        HazelcastInstance hz2 = fact.newHazelcastInstance(getConfig("127.0.0.1"));
//        HazelcastInstance hz3 = fact.newHazelcastInstance(getConfig("127.0.0.1"));
        HazelcastInstance hz2 = fact.newHazelcastInstance(getConfig("10.0.1.3"));
        HazelcastInstance hz3 = fact.newHazelcastInstance(getConfig("10.0.1.2"));
        waitUntilClusterState(hz3, ClusterState.ACTIVE, 1);
        assertEquals("cluster not correct size", 3, hz1.getCluster().getMembers().size());
        hz1.getMap("map").put("hello", "world");
        assertEquals("world", hz3.getMap("map").get("hello"));
    }

    private static class MemberAddressProviderImpl implements MemberAddressProvider {
        private final String myAddress;

        public MemberAddressProviderImpl(String myAddress) {
            this.myAddress = myAddress != null ? myAddress : "localhost";
        }

        @Override
        public InetSocketAddress getBindAddress() {
            return new InetSocketAddress(0);
        }

        @Override
        public InetSocketAddress getBindAddress(EndpointQualifier qualifier) {
            return getBindAddress();
        }

        @Override
        public InetSocketAddress getPublicAddress() {
            return new InetSocketAddress(myAddress, 0);
        }

        @Override
        public InetSocketAddress getPublicAddress(EndpointQualifier qualifier) {
            return getPublicAddress();
        }
    }
}
