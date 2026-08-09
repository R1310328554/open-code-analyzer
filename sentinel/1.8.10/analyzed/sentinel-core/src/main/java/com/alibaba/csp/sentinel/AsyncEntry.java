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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * 异步资源的 Entry。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class AsyncEntry extends CtEntry {

    private Context asyncContext;

    AsyncEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        super(resourceWrapper, chain, context);
    }

    AsyncEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context, int count, Object[] args) {
        super(resourceWrapper, chain, context, count, args);
    }

    /**
     * 从本地上下文中移除当前 Entry，但不执行 exit。
     */
    void cleanCurrentEntryInLocal() {
        if (context instanceof NullContext) {
            return;
        }
        Context originalContext = context;
        if (originalContext != null) {
            Entry curEntry = originalContext.getCurEntry();
            if (curEntry == this) {
                Entry parent = this.parent;
                originalContext.setCurEntry(parent);
                if (parent != null) {
                    ((CtEntry)parent).child = null;
                }
            } else {
                String curEntryName = curEntry == null ? "none"
                    : curEntry.resourceWrapper.getName() + "@" + curEntry.hashCode();
                String msg = String.format("Bad async context state, expected entry: %s, but actual: %s",
                    getResourceWrapper().getName() + "@" + hashCode(), curEntryName);
                throw new IllegalStateException(msg);
            }
        }
    }

    public Context getAsyncContext() {
        return asyncContext;
    }

    /**
     * 异步上下文不应在当前资源的 Node 已设置到当前 Entry 之前初始化。
     */
    void initAsyncContext() {
        if (asyncContext == null) {
            if (context instanceof NullContext) {
                asyncContext = context;
                return;
            }
            this.asyncContext = Context.newAsyncContext(context.getEntranceNode(), context.getName())
                .setOrigin(context.getOrigin())
                .setCurEntry(this);
        } else {
            RecordLog.warn(
                "[AsyncEntry] Duplicate initialize of async context for entry: " + resourceWrapper.getName());
        }
    }

    @Override
    protected void clearEntryContext() {
        super.clearEntryContext();
        this.asyncContext = null;
    }

    @Override
    protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
        exitForContext(asyncContext, count, args);

        return parent;
    }
}
