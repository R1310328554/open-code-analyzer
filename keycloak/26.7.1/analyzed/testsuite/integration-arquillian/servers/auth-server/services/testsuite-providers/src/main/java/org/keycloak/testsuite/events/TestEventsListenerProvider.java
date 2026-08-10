/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

/**
 * 测试用事件监听器，将用户事件与管理事件写入内存阻塞队列，供集成测试断言。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class TestEventsListenerProvider implements EventListenerProvider {

    /** 用户事件队列，跨测试实例共享。 */
    private static final BlockingQueue<Event> events = new LinkedBlockingQueue<Event>();
    /** 管理事件队列，跨测试实例共享。 */
    private static final BlockingQueue<AdminEvent> adminEvents = new LinkedBlockingQueue<>();
    /** 事务性事件收集器，在事务提交后入队。 */
    private final EventListenerTransaction tx = new EventListenerTransaction((event, includeRepre) -> adminEvents.add(event), events::add);

    /**
     * @param session Keycloak 会话，用于注册事务完成回调
     */
    public TestEventsListenerProvider(KeycloakSession session) {
        session.getTransactionManager().enlistAfterCompletion(tx);
    }

    /** {@inheritDoc} 将用户事件克隆后加入待提交队列。 */
    @Override
    public void onEvent(Event event) {
        tx.addEvent(event.clone());
    }

    /** {@inheritDoc} 将管理事件加入待提交队列。 */
    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        tx.addAdminEvent(new AdminEvent(event), includeRepresentation);
    }

    /** {@inheritDoc} 关闭监听器；当前实现无需额外清理。 */
    @Override
    public void close() {

    }

    /** 从用户事件队列中取出一条事件，无事件时返回 {@code null}。 */
    public static Event poll() {
        return events.poll();
    }

    /** 从管理事件队列中取出一条事件，无事件时返回 {@code null}。 */
    public static AdminEvent pollAdminEvent() {
        return adminEvents.poll();
    }

    /** 清空用户事件队列。 */
    public static void clear() {
        events.clear();
    }

    /** 清空管理事件队列。 */
    public static void clearAdminEvents() {
        adminEvents.clear();
    }
}
