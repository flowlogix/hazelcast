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

package com.hazelcast.cp.internal.operation.integration;

import com.hazelcast.cp.CPGroupId;
import com.hazelcast.cp.internal.RaftService;
import com.hazelcast.cp.internal.RaftServiceDataSerializerHook;
import com.hazelcast.cp.internal.raft.impl.dto.PreVoteRequest;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;

/**
 * Carries a {@link PreVoteRequest} RPC from a Raft candidate to a follower
 */
public class PreVoteRequestOp extends AsyncRaftOp {

    private PreVoteRequest voteRequest;

    public PreVoteRequestOp() {
    }

    public PreVoteRequestOp(CPGroupId groupId, PreVoteRequest voteRequest) {
        super(groupId);
        this.voteRequest = voteRequest;
    }

    @Override
    public void run() {
        RaftService service = getService();
        service.handlePreVoteRequest(groupId, voteRequest, target);
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeObject(voteRequest);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        voteRequest = in.readObject();
    }

    @Override
    public int getId() {
        return RaftServiceDataSerializerHook.PRE_VOTE_REQUEST_OP;
    }
}
