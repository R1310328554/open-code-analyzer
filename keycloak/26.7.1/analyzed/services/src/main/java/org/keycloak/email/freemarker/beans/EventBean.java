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

package org.keycloak.email.freemarker.beans;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.keycloak.events.Event;

/**
 * 用户事件 FreeMarker Bean：将 {@link Event} 暴露为模板可读属性。
 * <p>供事件通知邮件模板渲染事件类型、客户端、IP 与详情等信息。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EventBean {
    /** 底层用户事件对象。 */
    private Event event;

    /** @param event 待包装的用户事件 */
    public EventBean(Event event) {
        this.event = event;
    }

    /** @return 事件发生时间 */
    public Date getDate() {
        return new Date(event.getTime());
    }

    /** @return 事件类型（小写、空格分隔） */
    public String getEvent() {
        return event.getType().toString().toLowerCase().replace("_", " ");
    }

    /** @return 触发事件的客户端 ID */
    public String getClient() {
        return event.getClientId();
    }

    /**
     * 注意：反向代理未提供有效地址时返回值可能不是真实 IP。
     *
     * @return 客户端 IP 地址
     */
    public String getIpAddress() {
        return event.getIpAddress();
    }

    /** @return 事件详情键值对列表，供模板遍历 */
    public List<DetailBean> getDetails() {
        List<DetailBean> details = new LinkedList<DetailBean>();
        for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
            details.add(new DetailBean(e));
        }
        return details;
    }

    /** @param name 详情键名 @return 对应详情值，不存在时返回 {@code null} */
    public String getDetail(String name) {
        return event.getDetails() != null
                ? event.getDetails().get(name)
                : null;
    }

    /** 单条事件详情的 FreeMarker Bean 包装。 */
    public static class DetailBean {

        /** 详情键值对条目。 */
        private Map.Entry<String, String> entry;

        /** @param entry 详情键值对条目 */
        public DetailBean(Map.Entry<String, String> entry) {
            this.entry = entry;
        }

        /** @return 详情键名 */
        public String getKey() {
            return entry.getKey();
        }

        /** @return 详情值（下划线替换为空格） */
        public String getValue() {
            return entry.getValue().replace("_", " ");
        }

    }
}
