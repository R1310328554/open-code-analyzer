/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.models.sessions.infinispan.listeners;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;

import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;
import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.util.concurrent.BlockingManager;

/**
 * 远程 Infinispan 用户会话缓存的过期监听器。
 * <p>
 * 监听 {@link ClientCacheEntryExpired} 事件；因客户端以原始字节接收事件，需手动反序列化
 * {@link RemoteUserSessionEntity}。
 */
@ClientListener(converterFactoryName = "___eager-key-value-version-converter", useRawData = true)
public class RemoteUserSessionExpirationListener extends BaseUserSessionExpirationListener {

    /** 用于从原始事件字节中反序列化会话实体。 */
    private final Marshaller marshaller;

    public RemoteUserSessionExpirationListener(KeycloakSessionFactory factory, BlockingManager blockingManager, Marshaller marshaller) {
        super(factory, blockingManager);
        this.marshaller = marshaller;
    }

    /** 远程缓存条目过期时反序列化实体并发送过期事件。 */
    @ClientCacheEntryExpired
    public void onSessionExpired(ClientCacheEntryCustomEvent<byte[]> entryExpired) {
        try {
            RemoteUserSessionEntity entity = extractRemoteUserSessionEntity(entryExpired);
            if (entity == null) {
                return;
            }
            sendExpirationEvent(entity.getUserSessionId(), entity.getUserId(), entity.getRealmId());
        } catch (Exception e) {
            logger.error("Error handling an expired entry", e);
        }
    }

    /** 从 Hot Rod 原始事件中跳过键并读取 {@link RemoteUserSessionEntity} 值。 */
    private RemoteUserSessionEntity extractRemoteUserSessionEntity(ClientCacheEntryCustomEvent<byte[]> event) throws IOException, ClassNotFoundException {
        byte[] data = event.getEventData();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // 跳过键部分，过期处理只需值
        int length = UnsignedNumeric.readUnsignedInt(buffer);
        buffer.position(buffer.position() + length);

        // 读取并反序列化值
        length = UnsignedNumeric.readUnsignedInt(buffer);
        return (RemoteUserSessionEntity) marshaller.objectFromByteBuffer(data, buffer.position(), length);
    }
}
