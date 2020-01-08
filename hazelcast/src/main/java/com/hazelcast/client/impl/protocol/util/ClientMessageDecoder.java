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

package com.hazelcast.client.impl.protocol.util;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.internal.networking.HandlerStatus;
import com.hazelcast.internal.networking.nio.InboundHandlerWithCounters;
import com.hazelcast.nio.Connection;
import com.hazelcast.util.collection.Long2ObjectHashMap;
import com.hazelcast.util.function.Consumer;

import java.nio.ByteBuffer;

import static com.hazelcast.client.impl.protocol.ClientMessage.BEGIN_AND_END_FLAGS;
import static com.hazelcast.client.impl.protocol.ClientMessage.BEGIN_FLAG;
import static com.hazelcast.client.impl.protocol.ClientMessage.END_FLAG;
import static com.hazelcast.internal.networking.HandlerStatus.CLEAN;
import static com.hazelcast.nio.IOUtil.compactOrClear;

/**
 * Builds {@link ClientMessage}s from byte chunks.
 *
 * Fragmented messages are merged into single messages before processed.
 */
public class ClientMessageDecoder extends InboundHandlerWithCounters<ByteBuffer, Consumer<ClientMessage>> {

    private final Long2ObjectHashMap<BufferBuilder> builderBySessionIdMap = new Long2ObjectHashMap<BufferBuilder>();
    private final Connection connection;
    private ClientMessage message = ClientMessage.create();

    public ClientMessageDecoder(Connection connection, Consumer<ClientMessage> dst) {
        dst(dst);
        this.connection = connection;
    }

    @Override
    public void handlerAdded() {
        initSrcBuffer();
    }

    @Override
    public HandlerStatus onRead() {
        src.flip();
        try {
            while (src.hasRemaining()) {
                boolean complete = message.readFrom(src);
                if (!complete) {
                    break;
                }

                //MESSAGE IS COMPLETE HERE
                if (message.isFlagSet(BEGIN_AND_END_FLAGS)) {
                    //HANDLE-MESSAGE
                    handleMessage(message);
                    message = ClientMessage.create();
                    continue;
                }

                // first fragment
                if (message.isFlagSet(BEGIN_FLAG)) {
                    BufferBuilder builder = new BufferBuilder();
                    builderBySessionIdMap.put(message.getCorrelationId(), builder);
                    builder.append(message.buffer(), 0, message.getFrameLength());
                } else {
                    BufferBuilder builder = builderBySessionIdMap.get(message.getCorrelationId());
                    if (builder.position() == 0) {
                        throw new IllegalStateException();
                    }

                    builder.append(message.buffer(), message.getDataOffset(), message.getFrameLength() - message.getDataOffset());

                    if (message.isFlagSet(END_FLAG)) {
                        int msgLength = builder.position();
                        ClientMessage cm = ClientMessage.createForDecode(builder.buffer(), 0);
                        cm.setFrameLength(msgLength);
                        //HANDLE-MESSAGE
                        handleMessage(cm);
                        builderBySessionIdMap.remove(message.getCorrelationId());
                    }
                }

                message = ClientMessage.create();
            }

            return CLEAN;
        } finally {
            compactOrClear(src);
        }
    }

    private void handleMessage(ClientMessage message) {
        message.index(message.getDataOffset());
        message.setConnection(connection);
        normalPacketsRead.inc();
        dst.accept(message);
    }
}
