/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.cluster.lookup;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberLookup;
import com.alibaba.nacos.core.cluster.NacosMemberManager;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 成员寻址策略抽象基类：注入 {@link NacosMemberManager} 并在寻址完成后回调成员变更。
 * Addressable pattern base class.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class AbstractMemberLookup implements MemberLookup {
    
    /** 集群成员管理器，用于回写寻址结果。 */
    protected NacosMemberManager memberManager;
    
    /** 寻址器是否已启动。 */
    protected AtomicBoolean start = new AtomicBoolean(false);
    
    @Override
    public void injectMemberManager(NacosMemberManager memberManager) {
        this.memberManager = memberManager;
    }
    
    @Override
    public void afterLookup(Collection<Member> members) {
        this.memberManager.memberChange(members);
    }
    
    @Override
    public void destroy() throws NacosException {
        if (start.compareAndSet(true, false)) {
            doDestroy();
        }
    }
    
    @Override
    public void start() throws NacosException {
        if (start.compareAndSet(false, true)) {
            doStart();
        }
    }
    
    /**
     * 子类实现启动逻辑（如拉取 serverlist、监听文件）。
     * @throws NacosException 启动失败时抛出
     */
    protected abstract void doStart() throws NacosException;
    
    /**
     * 子类实现销毁逻辑（停止定时任务、注销监听器等）。
     * @throws NacosException 销毁失败时抛出
     */
    protected abstract void doDestroy() throws NacosException;
}
