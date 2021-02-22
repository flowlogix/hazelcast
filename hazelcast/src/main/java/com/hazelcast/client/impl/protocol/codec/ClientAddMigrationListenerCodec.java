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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;
import com.hazelcast.logging.Logger;

import javax.annotation.Nullable;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Adds a migration listener to the cluster.
 */
@Generated("80b81dadb307fb9aed0f33182e0d3d84")
public final class ClientAddMigrationListenerCodec {
    //hex: 0x001100
    public static final int REQUEST_MESSAGE_TYPE = 4352;
    //hex: 0x001101
    public static final int RESPONSE_MESSAGE_TYPE = 4353;
    private static final int REQUEST_LOCAL_ONLY_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_LOCAL_ONLY_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int RESPONSE_RESPONSE_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_RESPONSE_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int EVENT_MIGRATION_TYPE_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_MIGRATION_INITIAL_FRAME_SIZE = EVENT_MIGRATION_TYPE_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    //hex: 0x001102
    private static final int EVENT_MIGRATION_MESSAGE_TYPE = 4354;
    private static final int EVENT_REPLICA_MIGRATION_PARTITION_ID_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_REPLICA_INDEX_FIELD_OFFSET = EVENT_REPLICA_MIGRATION_PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_SOURCE_UUID_FIELD_OFFSET = EVENT_REPLICA_MIGRATION_REPLICA_INDEX_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_DEST_UUID_FIELD_OFFSET = EVENT_REPLICA_MIGRATION_SOURCE_UUID_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_SUCCESS_FIELD_OFFSET = EVENT_REPLICA_MIGRATION_DEST_UUID_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_ELAPSED_TIME_FIELD_OFFSET = EVENT_REPLICA_MIGRATION_SUCCESS_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int EVENT_REPLICA_MIGRATION_INITIAL_FRAME_SIZE = EVENT_REPLICA_MIGRATION_ELAPSED_TIME_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    //hex: 0x001103
    private static final int EVENT_REPLICA_MIGRATION_MESSAGE_TYPE = 4355;

    private ClientAddMigrationListenerCodec() {
    }

    public static ClientMessage encodeRequest(boolean localOnly) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setOperationName("Client.AddMigrationListener");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeBoolean(initialFrame.content, REQUEST_LOCAL_ONLY_FIELD_OFFSET, localOnly);
        clientMessage.add(initialFrame);
        return clientMessage;
    }

    /**
     * If set to true, the server adds the listener only to itself, otherwise
     * the listener is added for all members in the cluster.
     */
    public static boolean decodeRequest(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ClientMessage.Frame initialFrame = iterator.next();
        return decodeBoolean(initialFrame.content, REQUEST_LOCAL_ONLY_FIELD_OFFSET);
    }

    public static ClientMessage encodeResponse(java.util.UUID response) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeUUID(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET, response);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    /**
     * The listener registration id.
     */
    public static java.util.UUID decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ClientMessage.Frame initialFrame = iterator.next();
        return decodeUUID(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET);
    }

    public static ClientMessage encodeMigrationEvent(com.hazelcast.partition.MigrationState migrationState, int type) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_MIGRATION_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_MIGRATION_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeInt(initialFrame.content, EVENT_MIGRATION_TYPE_FIELD_OFFSET, type);
        clientMessage.add(initialFrame);

        MigrationStateCodec.encode(clientMessage, migrationState);
        return clientMessage;
    }
    public static ClientMessage encodeReplicaMigrationEvent(com.hazelcast.partition.MigrationState migrationState, int partitionId, int replicaIndex, @Nullable java.util.UUID sourceUuid, @Nullable java.util.UUID destUuid, boolean success, long elapsedTime) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_REPLICA_MIGRATION_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_REPLICA_MIGRATION_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeInt(initialFrame.content, EVENT_REPLICA_MIGRATION_PARTITION_ID_FIELD_OFFSET, partitionId);
        encodeInt(initialFrame.content, EVENT_REPLICA_MIGRATION_REPLICA_INDEX_FIELD_OFFSET, replicaIndex);
        encodeUUID(initialFrame.content, EVENT_REPLICA_MIGRATION_SOURCE_UUID_FIELD_OFFSET, sourceUuid);
        encodeUUID(initialFrame.content, EVENT_REPLICA_MIGRATION_DEST_UUID_FIELD_OFFSET, destUuid);
        encodeBoolean(initialFrame.content, EVENT_REPLICA_MIGRATION_SUCCESS_FIELD_OFFSET, success);
        encodeLong(initialFrame.content, EVENT_REPLICA_MIGRATION_ELAPSED_TIME_FIELD_OFFSET, elapsedTime);
        clientMessage.add(initialFrame);

        MigrationStateCodec.encode(clientMessage, migrationState);
        return clientMessage;
    }

    public abstract static class AbstractEventHandler {

        public void handle(ClientMessage clientMessage) {
            int messageType = clientMessage.getMessageType();
            ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
            if (messageType == EVENT_MIGRATION_MESSAGE_TYPE) {
                ClientMessage.Frame initialFrame = iterator.next();
                int type = decodeInt(initialFrame.content, EVENT_MIGRATION_TYPE_FIELD_OFFSET);
                com.hazelcast.partition.MigrationState migrationState = MigrationStateCodec.decode(iterator);
                handleMigrationEvent(migrationState, type);
                return;
            }
            if (messageType == EVENT_REPLICA_MIGRATION_MESSAGE_TYPE) {
                ClientMessage.Frame initialFrame = iterator.next();
                int partitionId = decodeInt(initialFrame.content, EVENT_REPLICA_MIGRATION_PARTITION_ID_FIELD_OFFSET);
                int replicaIndex = decodeInt(initialFrame.content, EVENT_REPLICA_MIGRATION_REPLICA_INDEX_FIELD_OFFSET);
                java.util.UUID sourceUuid = decodeUUID(initialFrame.content, EVENT_REPLICA_MIGRATION_SOURCE_UUID_FIELD_OFFSET);
                java.util.UUID destUuid = decodeUUID(initialFrame.content, EVENT_REPLICA_MIGRATION_DEST_UUID_FIELD_OFFSET);
                boolean success = decodeBoolean(initialFrame.content, EVENT_REPLICA_MIGRATION_SUCCESS_FIELD_OFFSET);
                long elapsedTime = decodeLong(initialFrame.content, EVENT_REPLICA_MIGRATION_ELAPSED_TIME_FIELD_OFFSET);
                com.hazelcast.partition.MigrationState migrationState = MigrationStateCodec.decode(iterator);
                handleReplicaMigrationEvent(migrationState, partitionId, replicaIndex, sourceUuid, destUuid, success, elapsedTime);
                return;
            }
            Logger.getLogger(super.getClass()).finest("Unknown message type received on event handler :" + messageType);
        }

        /**
         * @param migrationState Migration state.
         * @param type Type of the event. It is either MIGRATION_STARTED(0) or MIGRATION_FINISHED(1).
         */
        public abstract void handleMigrationEvent(com.hazelcast.partition.MigrationState migrationState, int type);

        /**
         * @param migrationState The progress information of the overall migration.
         * @param partitionId The partition ID that the event is dispatched for.
         * @param replicaIndex The index of the partition replica.
         * @param sourceUuid The id of old owner of the migrating partition replica.
         * @param destUuid The id of new owner of the migrating partition replica.
         * @param success The result of the migration: completed or failed.
         * @param elapsedTime The elapsed the time of this migration in milliseconds.
         */
        public abstract void handleReplicaMigrationEvent(com.hazelcast.partition.MigrationState migrationState, int partitionId, int replicaIndex, @Nullable java.util.UUID sourceUuid, @Nullable java.util.UUID destUuid, boolean success, long elapsedTime);
    }
}
