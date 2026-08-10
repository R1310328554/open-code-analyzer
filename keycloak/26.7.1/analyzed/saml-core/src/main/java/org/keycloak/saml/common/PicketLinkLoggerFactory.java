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

package org.keycloak.saml.common;

/**
 * 用于创建 {@link PicketLinkLogger} 实例的工厂类。
 * <p>创建策略如下：</p>
 * <ul>
 *   <li>尝试加载与 {@link PicketLinkLogger} 全限定名加 "Impl" 后缀同名的类；</li>
 *   <li>若未找到实现类，则回退到 {@link DefaultPicketLinkLogger} 作为默认日志实现。</li>
 * </ul>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public final class PicketLinkLoggerFactory {

    /** 全局单例日志实例，在类加载时初始化。 */
    private static PicketLinkLogger LOGGER;

    static {
        try {
            LOGGER = (PicketLinkLogger) Class.forName(PicketLinkLogger.class.getName() + "Impl").newInstance();
        } catch (Exception e) {
            // 未找到自定义实现时使用默认实现
            LOGGER = new DefaultPicketLinkLogger();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.usingLoggerImplementation(LOGGER.getClass().getName());
        }
    }

    /**
     * 返回已初始化的 {@link PicketLinkLogger} 实例。
     *
     * @return 日志门面实例
     */
    public static PicketLinkLogger getLogger() {
        return LOGGER;
    }

}
