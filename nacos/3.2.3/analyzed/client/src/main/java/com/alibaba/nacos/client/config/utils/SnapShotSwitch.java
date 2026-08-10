/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.config.utils;

import com.alibaba.nacos.client.config.impl.LocalConfigInfoProcessor;

/**
 * 本地配置快照功能开关。
 *
 * <p>关闭快照时会清空 {@link LocalConfigInfoProcessor} 下所有已持久化的 snapshot 文件。</p>
 *
 * @author Nacos
 */
public class SnapShotSwitch {
    
    /** 是否启用本地快照持久化，默认 true。 */
    private static Boolean isSnapShot = true;
    
    /**
     * 查询快照功能是否开启。
     *
     * @return 开启返回 true
     */
    public static Boolean getIsSnapShot() {
        return isSnapShot;
    }
    
    /**
     * 设置快照开关；关闭时会清理全部本地 snapshot。
     *
     * @param isSnapShot 是否启用快照
     */
    public static void setIsSnapShot(Boolean isSnapShot) {
        SnapShotSwitch.isSnapShot = isSnapShot;
        LocalConfigInfoProcessor.cleanAllSnapshot();
    }
    
}
