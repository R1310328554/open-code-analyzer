/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 启动与关闭生命周期抽象基类：按注册顺序启动、逆序预关闭与关闭子组件。
 */
public abstract class AbstractStartAndShutdown implements StartAndShutdown {

    /** 待统一管理的启动/关闭组件列表。 */
    protected List<StartAndShutdown> startAndShutdownList = new CopyOnWriteArrayList<>();

    protected void appendStartAndShutdown(StartAndShutdown startAndShutdown) {
        this.startAndShutdownList.add(startAndShutdown);
    }

    @Override
    public void start() throws Exception {
        for (StartAndShutdown startAndShutdown : startAndShutdownList) {
            startAndShutdown.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        int index = startAndShutdownList.size() - 1;
        for (; index >= 0; index--) {
            startAndShutdownList.get(index).shutdown();
        }
    }

    @Override
    public void preShutdown() throws Exception {
        int index = startAndShutdownList.size() - 1;
        for (; index >= 0; index--) {
            startAndShutdownList.get(index).preShutdown();
        }
    }

    /** 追加仅含启动逻辑的组件。 */
    public void appendStart(Start start) {
        this.appendStartAndShutdown(new StartAndShutdown() {
            @Override
            public void shutdown() throws Exception {

            }

            @Override
            public void start() throws Exception {
                start.start();
            }
        });
    }

    /** 追加仅含关闭逻辑的组件。 */
    public void appendShutdown(Shutdown shutdown) {
        this.appendStartAndShutdown(new StartAndShutdown() {
            @Override
            public void shutdown() throws Exception {
                shutdown.shutdown();
            }

            @Override
            public void start() throws Exception {

            }
        });
    }
}
