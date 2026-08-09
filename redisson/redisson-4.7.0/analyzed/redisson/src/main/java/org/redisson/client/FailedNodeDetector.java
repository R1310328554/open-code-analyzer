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
package org.redisson.client;

/**
 * Redis 节点故障检测器接口。
 * <p>
 * 通过连接、PING 与命令执行等事件回调收集状态，由 {@link #isNodeFailed()} 判定节点是否故障。
 *
 * @author Nikita Koksharov
 *
 */
public interface FailedNodeDetector {

    /** 连接建立成功时回调。 */
    void onConnectSuccessful();

    @Deprecated
    void onConnectFailed();

    default void onConnectFailed(Throwable cause) {
        onConnectFailed();
    }

    /** PING 成功时回调。 */
    void onPingSuccessful();

    @Deprecated
    void onPingFailed();

    default void onPingFailed(Throwable cause) {
        onPingFailed();
    }

    /** 命令执行成功时回调。 */
    void onCommandSuccessful();

    /** 命令执行失败时回调。 */
    void onCommandFailed(Throwable cause);

    /** 根据已收集的事件判断节点当前是否应视为故障。 */
    boolean isNodeFailed();

    /**
     * 返回配置相同但运行时状态独立的新检测器实例。
     *
     * @return 检测器副本
     */
    default FailedNodeDetector copy() {
        return this;
    }

}
