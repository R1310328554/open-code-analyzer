/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.consistency.entity.GetRequest;
import com.alibaba.nacos.consistency.entity.Log;
import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.consistency.exception.ConsistencyException;
import com.google.protobuf.Message;

/**
 * Protobuf 消息解析与新旧实体兼容转换工具。
 * protobuf message utils.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ProtoMessageUtil {
    
    /**
     * 请求类型字段 Tag，需与 ReadRequest/WriteRequest 的 field tag 区分。
     * should be different from field tags of ReadRequest or WriteQuest.
     */
    public static final int REQUEST_TYPE_FIELD_TAG = 7 << 3;
    
    /** 读请求类型标识字节 */
    public static final int REQUEST_TYPE_READ = 1;
    
    /** 写请求类型标识字节 */
    public static final int REQUEST_TYPE_WRITE = 2;
    
    /**
     * 将字节数组反序列化为 Protobuf {@link Message}，兼容新旧 Read/Write 及已废弃的 GetRequest/Log。
     * Converts the byte array to a specific Protobuf object.
     * Internally, the protobuf new and old objects are compatible.
     *
     * @param bytes An array of bytes
     * @return Message
     */
    public static Message parse(byte[] bytes) {
        Message result;
        try {
            if (bytes[0] == REQUEST_TYPE_FIELD_TAG) {
                if (bytes[1] == REQUEST_TYPE_READ) {
                    result = ReadRequest.parseFrom(bytes);
                } else {
                    result = WriteRequest.parseFrom(bytes);
                }
                return result;
            }
        } catch (Throwable ignore) {
        }
        
        // 旧版一致性实体，后续将废弃
        // old consistency entity, will be @Deprecated in future
        try {
            GetRequest request = GetRequest.parseFrom(bytes);
            return convertToReadRequest(request);
        } catch (Throwable ignore) {
        }
        
        try {
            Log log = Log.parseFrom(bytes);
            return convertToWriteRequest(log);
        } catch (Throwable ignore) {
        }
        
        throw new ConsistencyException(
            "The current array cannot be serialized to the corresponding object");
    }
    
    /**
     * 将旧版 {@link Log} 转换为 {@link WriteRequest}。
     * convert Log to WriteRequest.
     *
     * @param log log
     * @return {@link WriteRequest}
     */
    public static WriteRequest convertToWriteRequest(Log log) {
        return WriteRequest.newBuilder().setKey(log.getKey()).setGroup(log.getGroup())
            .setData(log.getData())
            .setType(log.getType())
            .setOperation(log.getOperation())
            .putAllExtendInfo(log.getExtendInfoMap())
            .build();
    }
    
    /**
     * 将旧版 {@link GetRequest} 转换为 {@link ReadRequest}。
     * convert Log to ReadRequest.
     *
     * @param request request
     * @return {@link ReadRequest}
     */
    public static ReadRequest convertToReadRequest(GetRequest request) {
        return ReadRequest.newBuilder()
            .setGroup(request.getGroup())
            .setData(request.getData())
            .putAllExtendInfo(request.getExtendInfoMap())
            .build();
    }
}
