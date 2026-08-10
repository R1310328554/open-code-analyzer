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

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.SessionTimeoutHelper;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;

/**
 * {@link AbstractLastSessionRefreshStore} 工厂基类，负责注册周期性检查任务。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractLastSessionRefreshStoreFactory {

    /** 定时器间隔：每 5 秒检查是否应发送累积的 lastSessionRefresh。 */
    public static final long DEFAULT_TIMER_INTERVAL_MS = 5000;

    /** 两次批量消息之间的最大间隔（秒），发往第二数据中心后至少间隔该时长再发。 */
    public static final int DEFAULT_MAX_INTERVAL_BETWEEN_MESSAGES_SECONDS = SessionTimeoutHelper.PERIODIC_TASK_INTERVAL_SECONDS;

    /** 待发送条目数上限，达到后立即触发批量发送。 */
    public static final int DEFAULT_MAX_COUNT = 100;

    protected void setupPeriodicTimer(KeycloakSession kcSession, AbstractLastSessionRefreshStore store, long timerIntervalMs, String eventKey) {
        TimerProvider timer = kcSession.getProvider(TimerProvider.class);
        timer.scheduleTask(new PropagateLastSessionRefreshTask(store), timerIntervalMs, eventKey);
    }

    /** 周期性调用 {@link AbstractLastSessionRefreshStore#checkSendingMessage} 的定时任务。 */
    public static class PropagateLastSessionRefreshTask implements ScheduledTask {

        private final AbstractLastSessionRefreshStore store;

        public PropagateLastSessionRefreshTask(AbstractLastSessionRefreshStore store) {
            this.store = store;
        }

        @Override
        public void run(KeycloakSession session) {
            store.checkSendingMessage(session, Time.currentTime());
        }
    }
}
