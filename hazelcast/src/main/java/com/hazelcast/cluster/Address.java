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

import com.hazelcast.internal.cluster.impl.ClusterDataSerializerHook;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.internal.server.ServerConnection;
import com.hazelcast.internal.util.AddressUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static com.hazelcast.internal.util.Preconditions.checkNotNull;
import java.util.Objects;

/**
 * Represents an address of a member in the cluster.
 */
public final class Address implements IdentifiedDataSerializable {
    public static class Context implements AutoCloseable {
        private final Address address;
        private final Connection connection;
        private final Context context;

        Context(Address address, Connection connection) {
            this.address = address;
            this.connection = connection;
            this.context = currentContext.get();
        }

        @Override
        public void close() {
            if (context != null) {
                currentContext.set(context);
            } else {
                currentContext.remove();
            }
        }
    }

    interface AutoCloseable extends java.lang.AutoCloseable {
        @Override
        public void close();
    }

    private static final byte IPV4 = 4;
    private static final byte IPV6 = 6;

    private static final ThreadLocal<Context> currentContext = new ThreadLocal<>();

    private int port = -1;
    private String host;
    private byte type;

    private String scopeId;
    private boolean hostSet;

    public Address() {
    }

    public Address(String host, int port) throws UnknownHostException {
        this(host, InetAddress.getByName(host), port);
    }

    public Address(InetAddress inetAddress, int port) {
        this(null, inetAddress, port);
        hostSet = false;
    }

    /**
     * Creates a new Address
     *
     * @param inetSocketAddress the InetSocketAddress to use
     * @throws java.lang.NullPointerException     if inetSocketAddress is null
     * @throws java.lang.IllegalArgumentException if the address can't be resolved.
     */
    public Address(InetSocketAddress inetSocketAddress) {
        this(resolve(inetSocketAddress), inetSocketAddress.getPort());
    }

    public Address(String hostname, InetAddress inetAddress, int port) {
        checkNotNull(inetAddress, "inetAddress can't be null");

        type = (inetAddress instanceof Inet4Address) ? IPV4 : IPV6;
        String[] addressArgs = inetAddress.getHostAddress().split("\\%");
        host = hostname != null ? hostname : addressArgs[0];
        if (addressArgs.length == 2) {
            scopeId = addressArgs[1];
        }
        this.port = port;
        hostSet = !AddressUtil.isIpAddress(host);
    }

    public Address(Address address) {
        this.host = address.host;
        this.port = address.port;
        this.type = address.type;
        this.scopeId = address.scopeId;
        this.hostSet = address.hostSet;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(getScopedHost());
    }

    public InetSocketAddress getInetSocketAddress() throws UnknownHostException {
        return new InetSocketAddress(getInetAddress(), port);
    }

    public boolean isIPv4() {
        return type == IPV4;
    }

    public boolean isIPv6() {
        return type == IPV6;
    }

    public String getScopeId() {
        return isIPv6() ? scopeId : null;
    }

    public void setScopeId(final String scopeId) {
        if (isIPv6()) {
            this.scopeId = scopeId;
        }
    }

    public String getScopedHost() {
        return (isIPv4() || hostSet || scopeId == null) ? getHost()
                : getHost() + '%' + scopeId;
    }

    @Override
    public int getFactoryId() {
        return ClusterDataSerializerHook.F_ID;
    }

    @Override
    public int getClassId() {
        return ClusterDataSerializerHook.ADDRESS;
    }

    public static Context setContext(Address address, ServerConnection connection) {
        Objects.requireNonNull(address);
        Context context = new Context(address, connection);
        currentContext.set(context);
        return context;
    }

    public static Context overrideConnection(ServerConnection connection) {
        Context context = currentContext.get();
        if (context == null && connection != null) {
            throw new IllegalStateException("No context to override");
        } else if (context != null) {
            Context newContext = new Context(context.address, connection);
            currentContext.set(newContext);
            return newContext;
        }
        // dummy context, not set
        return new Context(null, null) {
            @Override
            public void close() {
                // no-op
            }
        };
    }

    static Context getCurrentContext() {
        return currentContext.get();
    }

    String transformHost() {
        Context context = currentContext.get();
        if (context != null && context.connection != null
                && (context.address.equals(this))) {
            String dynamicHost = context.connection.getInetAddress().getHostAddress();
            if (!host.equals(dynamicHost)) {
                try {
                    System.out.format("***** Rewriting %s -> %s -.-. Sending %s -> %s\n",
                            new Address(host, port), new Address(dynamicHost, port),
                            context.address, context.connection.getRemoteAddress());
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return dynamicHost;
        } else if (context != null && context.connection == null) {
            throw new IllegalStateException("rewrite and connection is null");
        } else {
            return host;
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(port);
        out.write(type);
        String txHost = transformHost();
        out.writeUTF(txHost);
        if (currentContext.get() == null) {
            System.out.println("***** No Rewrite Context");
            Thread.dumpStack();
        } else if (getCurrentContext().address.equals(new Address(txHost, port))) {
            System.out.println("*!*!*!*!*!*!*!*localhost leak!!!!");
            Thread.dumpStack();
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        port = in.readInt();
        type = in.readByte();
        host = in.readUTF();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        final Address address = (Address) o;
        return port == address.port && this.type == address.type && this.host.equals(address.host);
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + host.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return '[' + host + "]:" + port;
    }

    private static InetAddress resolve(InetSocketAddress inetSocketAddress) {
        checkNotNull(inetSocketAddress, "inetSocketAddress can't be null");

        InetAddress address = inetSocketAddress.getAddress();
        if (address == null) {
            throw new IllegalArgumentException("Can't resolve address: " + inetSocketAddress);
        }
        return address;
    }
}
