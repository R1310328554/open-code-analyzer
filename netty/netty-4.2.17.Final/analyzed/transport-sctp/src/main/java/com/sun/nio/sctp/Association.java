/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sun.nio.sctp;

/**
 * 表示一条 SCTP 关联（association）：两端点间的逻辑连接， 可含多条流与多个 IP 地址。
 * <p>本仓库中为非 SCTP 平台的占位 stub；真实 JDK 实现在支持 SCTP 的 OS 上提供。</p>
 */
public class Association {
    /** 非 SCTP 平台加载时立即失败，避免误用 stub API */
    static {
        UnsupportedOperatingSystemException.raise();
    }
}
