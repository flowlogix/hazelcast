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

import com.hazelcast.cluster.Address.Context;
import com.hazelcast.cluster.impl.MemberImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAddressProviderConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.internal.cluster.impl.MemberMap;
import com.hazelcast.internal.server.ServerConnection;
import com.hazelcast.internal.util.UuidUtil;
import com.hazelcast.spi.MemberAddressProvider;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import static com.hazelcast.test.TestEnvironment.HAZELCAST_TEST_USE_NETWORK;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        config.setProperty(ClusterProperty.WAIT_SECONDS_BEFORE_JOIN.getName(), "0");
        config.setProperty(ClusterProperty.MAX_WAIT_SECONDS_BEFORE_JOIN.getName(), "0");
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
    @Ignore
    public void wildcardAddressTest() {
        HazelcastInstance hz1 = fact.newHazelcastInstance(getConfig(null));
//        HazelcastInstance hz2 = fact.newHazelcastInstance(getConfig("127.0.0.1"));
//        HazelcastInstance hz3 = fact.newHazelcastInstance(getConfig("127.0.0.1"));
        HazelcastInstance hz2 = fact.newHazelcastInstance(getConfig("10.0.1.3"));
        HazelcastInstance hz3 = fact.newHazelcastInstance(getConfig("10.0.1.2"));
        waitUntilClusterState(hz3, ClusterState.ACTIVE, 1);
        assertEquals("cluster not correct size", 3, hz1.getCluster().getMembers().size());
        System.out.println("clusters joined");
        hz1.getMap("map").put("hello", "world");
        hz2.getMap("map2").put("hello2", "world2");
        assertEquals("world", hz3.getMap("map").get("hello"));
        assertEquals("world2", hz1.getMap("map2").get("hello2"));
        System.out.println("!!!!! all done!");
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

    @Test
    public void testOverrideAddress() throws UnknownHostException {
        Address address = new Address("1.1.1.1", 1);

        ServerConnection conn1 = mock(ServerConnection.class);
        when(conn1.getRemoteAddress()).thenReturn(new Address("2.2.2.2", 1));
        when(conn1.getInetAddress()).thenReturn(Inet4Address.getByName("2.2.2.2"));

        try (Context outer = Address.setContext(new Address("1.1.1.1", 1), conn1)) {
            assertEquals("2.2.2.2", address.dynamicHost());
            try (Context inner = Address.setContext(new Address("3.3.3.3", 1), conn1)) {
                assertEquals("1.1.1.1", address.getHost());
            }
            assertNotNull(Address.getCurrentContext());
            assertEquals("2.2.2.2", address.dynamicHost());
        }
        assertEquals("1.1.1.1", address.getHost());
        assertNull(Address.getCurrentContext());
    }

    @Test
    public void testOverrideConnection() throws UnknownHostException {
        assertNull(Address.getCurrentContext());
        ServerConnection conn1 = mock(ServerConnection.class);
        ServerConnection conn2 = mock(ServerConnection.class);
        when(conn1.getRemoteAddress()).thenReturn(new Address("2.2.2.2", 1));
        when(conn1.getInetAddress()).thenReturn(Inet4Address.getByName("2.2.2.2"));

        when(conn2.getRemoteAddress()).thenReturn(new Address("3.3.3.3", 1));
        when(conn2.getInetAddress()).thenReturn(Inet4Address.getByName("3.3.3.3"));

        assertNotEquals(conn1, conn2);
        assertNotSame(conn1, conn2);
        Address address = new Address("1.1.1.1", 1);
        try (Context outer = Address.setContext(new Address("1.1.1.1", 1), conn1)) {
            assertEquals("2.2.2.2", address.dynamicHost());
            try (Context inner = Address.overrideConnection(conn2)) {
                assertEquals("3.3.3.3", address.dynamicHost());
            }
            assertEquals("2.2.2.2", address.dynamicHost());
        }
        assertEquals("1.1.1.1", address.getHost());
    }

    @Test(expected = IllegalStateException.class)
    public void testOverrideConnection_whenNoContext() throws UnknownHostException
    {
        ServerConnection conn1 = mock(ServerConnection.class);
        Address.overrideConnection(conn1);
    }

    @Test
    public void testNullConnection() throws UnknownHostException {
        try (Context outer = Address.setContext(new Address("1.1.1.1", 1), null)) {
            assertNotNull(Address.getCurrentContext());
            try (Context inner = Address.overrideConnection(null)) {
                assertNotNull(Address.getCurrentContext());
            }
            assertNotNull(Address.getCurrentContext());
        }
        Address.overrideConnection(null);
        assertNull(Address.getCurrentContext());
    }

    @Test(expected = IllegalStateException.class)
    public void testOverrideConnection_whenNoConnection() throws UnknownHostException
    {
        Address address = new Address("1.1.1.1", 1);
        try (Context outer = Address.setContext(new Address("1.1.1.1", 1), null)) {
            address.dynamicHost();
        }
    }

    @Test
    public void testMemberMapWithDynamic() throws UnknownHostException {
        Address address = new Address("1.1.1.1", 1).withDynamicHost("2.2.2.2");

        MemberImpl member = mock(MemberImpl.class);
        UUID uuid = UuidUtil.newUnsecureUUID();
        when(member.getUuid()).thenReturn(uuid);
        when(member.getAddress()).thenReturn(new Address(address));
        MemberMap memberMap = MemberMap.createNew(1, member);

        assertNotNull(memberMap.getMember(address));
        assertNotNull(memberMap.getMember(address.toDynamic()));
    }

    public static void main(String[] args) {
        MultiHomedJoinerTest.init();
        MultiHomedJoinerTest test = new MultiHomedJoinerTest();
        test.beforeRun();
        test.wildcardAddressTest();
        test.afterRun();
    }
}
