/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.jcache;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;


/**
 * JSR-107 TCK 兼容的 {@link MBeanServerBuilder}。
 * <p>
 * 包装 {@link MBeanServerDelegate}，使 {@link #getMBeanServerId()} 返回
 * TCK 要求的 {@code org.jsr107.tck.management.agentId} 系统属性。
 *
 * @author Nikita Koksharov
 *
 */
public final class JCacheMBeanServerBuilder extends MBeanServerBuilder {

    /** 创建带 TCK 兼容 Delegate 的 MBeanServer。 */
    @Override
    public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer,
                                      MBeanServerDelegate delegate) {
        MBeanServerDelegate wrappedDelegate = new JCacheMBeanServerDelegate(delegate);
        MBeanServerBuilder builder = new MBeanServerBuilder();
        return builder.newMBeanServer(defaultDomain, outer, wrappedDelegate);
    }

    /** 委托包装：除 MBeanServerId 外均转发至原始 delegate。 */
    public final class JCacheMBeanServerDelegate extends MBeanServerDelegate {

        /** 被包装的平台 MBeanServerDelegate。 */
        private final MBeanServerDelegate delegate;

        /** 构造 TCK 用 Delegate 包装器。 */
        public JCacheMBeanServerDelegate(MBeanServerDelegate delegate) {
            this.delegate = delegate;
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo() {
            return delegate.getNotificationInfo();
        }

        @Override
        public String getSpecificationName() {
            return delegate.getSpecificationName();
        }

        @Override
        public String getSpecificationVersion() {
            return delegate.getSpecificationVersion();
        }

        @Override
        public String getSpecificationVendor() {
            return delegate.getSpecificationVendor();
        }

        @Override
        public String getImplementationName() {
            return delegate.getImplementationName();
        }

        @Override
        public String getImplementationVersion() {
            return delegate.getImplementationVersion();
        }

        @Override
        public String getImplementationVendor() {
            return delegate.getImplementationVendor();
        }
        
        @Override
        public synchronized void addNotificationListener(
                NotificationListener listener, NotificationFilter filter, Object handback) 
            throws IllegalArgumentException {
            delegate.addNotificationListener(listener, filter, handback);
        }

        @Override
        public synchronized void removeNotificationListener(
                NotificationListener listener,
                NotificationFilter filter,
                                                            Object handback) throws
            ListenerNotFoundException {
            delegate.removeNotificationListener(listener, filter, handback);
        }

        @Override
        public synchronized void removeNotificationListener(NotificationListener
                                                                listener) throws
            ListenerNotFoundException {
            delegate.removeNotificationListener(listener);
        }

        @Override
        public void sendNotification(Notification notification) {
            delegate.sendNotification(notification);
        }

        /** TCK 要求从系统属性读取 agentId 作为 ServerId。 */
        @Override
        public synchronized String getMBeanServerId() {
            return System.getProperty("org.jsr107.tck.management.agentId");
        }
    }

    
}
