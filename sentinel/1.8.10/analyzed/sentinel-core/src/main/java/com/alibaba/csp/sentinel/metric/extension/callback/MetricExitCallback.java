/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.metric.extension.AdvancedMetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 指标扩展的出口（exit）回调。
 * <p>在资源调用退出时上报 RT、成功、异常及线程数等指标。</p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 * @since 1.6.1
 */
public class MetricExitCallback implements ProcessorSlotExitCallback {

    @Override
    public void onExit(Context context, ResourceWrapper rw, int acquireCount, Object... args) {
        Entry curEntry = context.getCurEntry();
        if (curEntry == null) {
            return;
        }
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            if (curEntry.getBlockError() != null) {
                continue;
            }
            String resource = rw.getName();
            Throwable ex = curEntry.getError();
            long completeTime = curEntry.getCompleteTimestamp();
            if (completeTime <= 0) {
                completeTime = TimeUtil.currentTimeMillis();
            }
            long rt = completeTime - curEntry.getCreateTimestamp();

            if (m instanceof AdvancedMetricExtension) {
                // 自 1.8.0 起（兼容 AdvancedMetricExtension 的临时方案）
                ((AdvancedMetricExtension) m).onComplete(rw, rt, acquireCount, args);
                if (ex != null) {
                    ((AdvancedMetricExtension) m).onError(rw, ex, acquireCount, args);
                }
            } else {
                m.addRt(resource, rt, args);
                m.addSuccess(resource, acquireCount, args);
                m.decreaseThreadNum(resource, args);
                if (null != ex) {
                    m.addException(resource, acquireCount, ex);
                }
            }
        }
    }
}
