/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.auth.serveridentity;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * 服务端身份校验器 SPI 持有者（单例）。
 *
 * <p>启动时通过 {@link NacosServiceLoader} 加载 {@link ServerIdentityChecker} 实现类；
 * 未找到 SPI 实现时使用 {@link DefaultChecker}，多个实现时取第一个并打印警告。</p>
 *
 * @author xiweng.yy
 */
public class ServerIdentityCheckerHolder {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerIdentityCheckerHolder.class);
    
    /** 单例实例。 */
    private static final ServerIdentityCheckerHolder INSTANCE = new ServerIdentityCheckerHolder();
    
    /** SPI 发现的校验器实现类，供 {@link #newChecker()} 反射实例化。 */
    private Class<? extends ServerIdentityChecker> checkerClass;
    
    /** 私有构造，初始化时加载 SPI 实现。 */
    private ServerIdentityCheckerHolder() {
        tryGetCheckerBySpi();
    }
    
    /** 返回持有者单例。 */
    public static ServerIdentityCheckerHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * 创建新的校验器实例。
     *
     * <p>反射实例化 SPI 发现的实现类；失败时回退为 {@link DefaultChecker}。</p>
     *
     * @return 新的 {@link ServerIdentityChecker} 实例
     */
    public ServerIdentityChecker newChecker() {
        try {
            return checkerClass.getDeclaredConstructor(new Class[0]).newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
            | NoSuchMethodException e) {
            return new DefaultChecker();
        }
    }
    
    /** 通过 SPI 加载校验器实现并确定 checkerClass。 */
    private synchronized void tryGetCheckerBySpi() {
        Collection<ServerIdentityChecker> checkers =
            NacosServiceLoader.load(ServerIdentityChecker.class);
        if (checkers.isEmpty()) {
            checkerClass = DefaultChecker.class;
            LOGGER.info("Not found ServerIdentityChecker implementation from SPI, use default.");
            return;
        }
        if (checkers.size() > 1) {
            checkerClass = showAllImplementations(checkers);
            return;
        }
        checkerClass = checkers.iterator().next().getClass();
        LOGGER.info("Found ServerIdentityChecker implementation {}",
            checkerClass.getClass().getCanonicalName());
    }
    
    /** 多个 SPI 实现时打印全部候选并选用第一个。 */
    private Class<? extends ServerIdentityChecker> showAllImplementations(
        Collection<ServerIdentityChecker> checkers) {
        ServerIdentityChecker result = checkers.iterator().next();
        for (ServerIdentityChecker each : checkers) {
            LOGGER.warn("Found ServerIdentityChecker implementation {}",
                each.getClass().getCanonicalName());
        }
        LOGGER.warn(
            "Found more than one ServerIdentityChecker implementation from SPI, use the first one {}.",
            result.getClass().getCanonicalName());
        return result.getClass();
    }
}
