/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.remote;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * 客户端连接事件监听器抽象基类，在连接建立/断开时接收回调。
 * ClientConnectionEventListener.
 *
 * @author liuzunfei
 * @version $Id: ClientConnectionEventListener.java, v 0.1 2020年07月16日 3:06 PM liuzunfei Exp $
 */
public abstract class ClientConnectionEventListener {
    
    /**
     * 监听器名称，用于日志与排查。
     * listener name.
     */
    private String name;
    
    /** 客户端连接事件监听器注册表。 */
    @Autowired
    protected ClientConnectionEventListenerRegistry clientConnectionEventListenerRegistry;
    
    /** 启动时将本监听器注册到全局注册表。 */
    @PostConstruct
    public void init() {
        clientConnectionEventListenerRegistry.registerClientConnectionEventListener(this);
    }
    
    /**
     * 获取监听器名称。
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置监听器名称。
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 客户端建立连接时的回调。
     * notified when a client connected.
     *
     * @param connect connect.
     */
    public abstract void clientConnected(Connection connect);
    
    /**
     * 客户端断开连接时的回调。
     * notified when a client disconnected.
     *
     * @param connect connect.
     */
    public abstract void clientDisConnected(Connection connect);
    
}
