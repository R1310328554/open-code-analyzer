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
package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 * 保存当前调用的元数据：<br/>
 *
 * <ul>
 * <li>{@link EntranceNode}：当前调用树的根节点。</li>
 * <li>current {@link Entry}：当前调用点。</li>
 * <li>current {@link Node}：与 {@link Entry} 相关的统计节点。</li>
 * <li>origin：来源标识，用于分别控制不同调用方/消费者。
 * 通常来源可以是服务消费者的应用名或来源 IP。</li>
 * </ul>
 * <p>
 * 每次 {@link SphU}#entry() 或 {@link SphO}#entry() 都应在某个 {@link Context} 中执行；
 * 若未显式调用 {@link ContextUtil}#enter()，将使用 DEFAULT 上下文。
 * </p>
 * <p>
 * 在同一上下文中多次调用 {@link SphU}#entry() 会形成调用树。
 * </p>
 * <p>
 * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 * @see ContextUtil
 * @see NodeSelectorSlot
 */
public class Context {

    /**
     * 上下文名称。
     */
    private final String name;

    /**
     * 当前调用树的入口节点。
     */
    private DefaultNode entranceNode;

    /**
     * 当前正在处理的 Entry。
     */
    private Entry curEntry;

    /**
     * 本上下文的来源（通常标识不同调用方，例如服务消费者名称或来源 IP）。
     */
    private String origin = "";

    private final boolean async;

    /**
     * 创建新的异步上下文。
     *
     * @param entranceNode 上下文的入口节点
     * @param name 上下文名称
     * @return 新创建的上下文
     * @since 0.2.0
     */
    public static Context newAsyncContext(DefaultNode entranceNode, String name) {
        return new Context(name, entranceNode, true);
    }

    public Context(DefaultNode entranceNode, String name) {
        this(name, entranceNode, false);
    }

    public Context(String name, DefaultNode entranceNode, boolean async) {
        this.name = name;
        this.entranceNode = entranceNode;
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    public String getName() {
        return name;
    }

    public Node getCurNode() {
        return curEntry == null ? null : curEntry.getCurNode();
    }

    public Context setCurNode(Node node) {
        this.curEntry.setCurNode(node);
        return this;
    }

    public Entry getCurEntry() {
        return curEntry;
    }

    public Context setCurEntry(Entry curEntry) {
        this.curEntry = curEntry;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Context setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public double getOriginTotalQps() {
        return getOriginNode() == null ? 0 : getOriginNode().totalQps();
    }

    public double getOriginBlockQps() {
        return getOriginNode() == null ? 0 : getOriginNode().blockQps();
    }

    public double getOriginPassReqQps() {
        return getOriginNode() == null ? 0 : getOriginNode().successQps();
    }

    public double getOriginPassQps() {
        return getOriginNode() == null ? 0 : getOriginNode().passQps();
    }

    public long getOriginTotalRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().totalRequest();
    }

    public long getOriginBlockRequest() {
        return getOriginNode() == null ? 0 : getOriginNode().blockRequest();
    }

    public double getOriginAvgRt() {
        return getOriginNode() == null ? 0 : getOriginNode().avgRt();
    }

    public int getOriginCurThreadNum() {
        return getOriginNode() == null ? 0 : getOriginNode().curThreadNum();
    }

    public DefaultNode getEntranceNode() {
        return entranceNode;
    }

    /**
     * 获取当前节点的父 {@link Node}。
     *
     * @return 当前节点的父节点
     */
    public Node getLastNode() {
        if (curEntry != null && curEntry.getLastNode() != null) {
            return curEntry.getLastNode();
        } else {
            return entranceNode;
        }
    }

    public Node getOriginNode() {
        return curEntry == null ? null : curEntry.getOriginNode();
    }

    @Override
    public String toString() {
        return "Context{" +
            "name='" + name + '\'' +
            ", entranceNode=" + entranceNode +
            ", curEntry=" + curEntry +
            ", origin='" + origin + '\'' +
            ", async=" + async +
            '}';
    }
}
