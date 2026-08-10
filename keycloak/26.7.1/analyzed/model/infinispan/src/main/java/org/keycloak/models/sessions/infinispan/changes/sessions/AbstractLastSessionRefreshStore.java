/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.changes.sessions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;

/**
 * 批量传播 {@code lastSessionRefresh} 更新的抽象存储。
 * <p>
 * 在内存中累积用户会话的最后刷新时间，达到数量或时间阈值后通过 {@link #sendMessage} 批量下发。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractLastSessionRefreshStore {

    /** 两次批量发送之间的最大间隔（秒）。 */
    private final int maxIntervalBetweenMessagesSeconds;
    /** 触发发送的待刷新会话数量上限。 */
    private final int maxCount;

    /** 会话 ID 到 {@link SessionData} 的待发送映射。 */
    private volatile Map<String, SessionData> lastSessionRefreshes = new ConcurrentHashMap<>();

    /** 上次批量发送时的 Unix 时间戳。 */
    private volatile int lastRun = Time.currentTime();


    protected AbstractLastSessionRefreshStore(int maxIntervalBetweenMessagesSeconds, int maxCount) {
        this.maxIntervalBetweenMessagesSeconds = maxIntervalBetweenMessagesSeconds;
        this.maxCount = maxCount;
    }


    public void putLastSessionRefresh(KeycloakSession kcSession, String sessionId, String realmId, int lastSessionRefresh) {
        lastSessionRefreshes.put(sessionId, new SessionData(realmId, lastSessionRefresh));

        // lastSessionRefresh 通常接近当前时间，用于判断是否应触发发送
        checkSendingMessage(kcSession, lastSessionRefresh);
    }


    void checkSendingMessage(KeycloakSession kcSession, int currentTime) {
        if (lastSessionRefreshes.size() >= maxCount || lastRun + maxIntervalBetweenMessagesSeconds <= currentTime) {
            Map<String, SessionData> refreshesToSend = prepareSendingMessage();

            // 发送消息本身无需同步，prepareSendingMessage 已交换内部映射
            if (refreshesToSend != null) {
                sendMessage(kcSession, refreshesToSend);
            }
        }
    }


    // 同步交换内部映射；若满足发送条件则返回待发送快照，否则返回 null
    private synchronized Map<String, SessionData> prepareSendingMessage() {
        // 重新读取当前时间，避免测试中的竞态误判
        int currentTime = Time.currentTime();
        if (lastSessionRefreshes.size() >= maxCount || lastRun + maxIntervalBetweenMessagesSeconds <= currentTime) {
            // 换新 ConcurrentHashMap，使并发写入使用新容器
            Map<String, SessionData> copiedRefreshesToSend = lastSessionRefreshes;
            lastSessionRefreshes = new ConcurrentHashMap<>();
            lastRun = currentTime;

            return copiedRefreshesToSend;
        } else {
            return null;
        }
    }


    public synchronized void reset() {
        lastRun = Time.currentTime();
        lastSessionRefreshes = new ConcurrentHashMap<>();
    }


    /**
     * 将自上次调用以来累积的用户会话刷新信息批量写入底层存储。
     *
     * @param kcSession Keycloak 会话
     * @param refreshesToSend 键为用户会话 ID，值为 {@link SessionData}
     */
    protected abstract void sendMessage(KeycloakSession kcSession, Map<String, SessionData> refreshesToSend);
}
