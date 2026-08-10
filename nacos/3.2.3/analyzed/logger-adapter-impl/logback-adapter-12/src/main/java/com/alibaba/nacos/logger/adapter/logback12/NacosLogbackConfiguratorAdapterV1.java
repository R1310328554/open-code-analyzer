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

package com.alibaba.nacos.logger.adapter.logback12;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import com.alibaba.nacos.common.logging.NacosLoggingProperties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Logback 1.2 专用 Joran 配置器，避免 Nacos 配置污染用户 savepoint 与扫描 URL。
 *
 * <p>禁用 {@link #registerSafeConfiguration}、注册 {@link NacosClientPropertyAction}， 并兼容 1.1.10 以下旧版 {@code doConfigure} API。</p>
 *
 * @author <a href="mailto:hujun3@xiaomi.com">hujun</a>
 * @see <a href="https://github.com/alibaba/nacos/issues/6999">#6999</a>
 */
public class NacosLogbackConfiguratorAdapterV1 extends JoranConfigurator {
    
    /** 加载 XML 时使用的 Nacos 客户端属性。 */
    private NacosLoggingProperties loggingProperties;
    
    /** 注入属性供 {@link NacosClientPropertyAction} 读取。 */
    public void setLoggingProperties(NacosLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }
    
    /**
     * 空实现：阻止 Nacos 配置写入 Logback safe configuration savepoint。
     *
     * @param eventList safe data
     */
    @Override
    public void registerSafeConfiguration(List<SaxEvent> eventList) {
    }
    
    /** 在父类规则基础上注册 {@code nacosClientProperty} 解析规则。 */
    @Override
    public void addInstanceRules(RuleStore rs) {
        super.addInstanceRules(rs);
        rs.addRule(new ElementSelector("configuration/nacosClientProperty"),
            new NacosClientPropertyAction(loggingProperties));
    }
    
    /**
     * 从 URL 加载 Logback 配置，禁用 URLConnection 缓存并兼容旧版 API。
     *
     * @param url config url
     * @throws Exception e
     */
    public void configure(URL url) throws Exception {
        InputStream in = null;
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
            in = urlConnection.getInputStream();
            if (hasNewDoConfigureApi()) {
                doConfigure(in, url.toExternalForm());
            } else {
                // 兼容 Logback 1.1.10 以下仅支持 InputStream 的旧版 doConfigure API
                doConfigure(in);
            }
        } catch (IOException ioe) {
            String errMsg = "Could not open URL [" + url + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    String errMsg = "Could not close input stream";
                    addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }
    
    /**
     * 检测当前 Logback 是否提供带 systemId 的新版 {@code doConfigure(InputStream, String)} API。
     *
     * @return Logback 版本高于 1.1.10 时返回 {@code true}
     */
    private boolean hasNewDoConfigureApi() {
        try {
            this.getClass().getMethod("doConfigure", InputStream.class, String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
