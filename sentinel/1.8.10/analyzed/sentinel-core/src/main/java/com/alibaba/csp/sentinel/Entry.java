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

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.util.function.BiConsumer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.context.Context;

/**
 * 每次 {@link SphU}#entry() 返回一个 {@link Entry}。本类保存当前调用的信息：<br/>
 *
 * <ul>
 * <li>createTime，Entry 创建时间，用于 RT 统计。</li>
 * <li>current {@link Node}，即当前上下文中该资源的统计节点。</li>
 * <li>origin {@link Node}，即特定来源的统计节点。来源通常是服务消费者的应用名，见
 * {@link ContextUtil#enter(String name, String origin)} </li>
 * <li>{@link ResourceWrapper}，即资源名称。</li>
 * <br/>
 * </ul>
 *
 * <p>
 * 在同一 {@link Context} 中多次调用 SphU#entry() 会形成调用树，
 * 本类通过 parent 或 child 引用构成该树。由于 {@link Context} 始终持有调用树中的当前 Entry，
 * 每次 {@link Entry#exit()} 都应将 {@link Context#setCurEntry(Entry)} 更新为本 Entry 的父 Entry。
 * </p>
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 * @see SphU
 * @see Context
 * @see ContextUtil
 */
public abstract class Entry implements AutoCloseable {

    protected static final Object[] OBJECTS0 = new Object[0];

    private final long createTimestamp;
    private long completeTimestamp;

    private Node curNode;
    /**
     * 特定来源的 {@link Node}，通常来源为服务消费者。
     */
    private Node originNode;

    private Throwable error;
    private BlockException blockError;

    protected final ResourceWrapper resourceWrapper;

    protected final int count;

    protected final Object[] args;

    public Entry(ResourceWrapper resourceWrapper) {
        this(resourceWrapper, 1, OBJECTS0);
    }

    public Entry(ResourceWrapper resourceWrapper, int count, Object[] args) {
        this.resourceWrapper = resourceWrapper;
        this.createTimestamp = TimeUtil.currentTimeMillis();
        this.count = count;
        this.args = args;
    }

    public ResourceWrapper getResourceWrapper() {
        return resourceWrapper;
    }

    /**
     * 完成当前资源 Entry 并恢复上下文中的 Entry 栈。
     * 无需传入 count 或 args 参数，初始化时已携带。
     * @throws ErrorEntryFreeException 若当前上下文中的 Entry 与当前 Entry 不匹配
     */
    public void exit() throws ErrorEntryFreeException {
        exit(count, args);
    }

    public void exit(int count) throws ErrorEntryFreeException {
        exit(count, args);
    }

    /**
     * 等价于 {@link #exit()}。自 JDK 1.7 起支持 try-with-resources。
     *
     * @since 1.5.0
     */
    @Override
    public void close() {
        exit();
    }

    /**
     * 退出本 Entry。应在资源保护结束时且仅调用一次。
     *
     * @param count 要释放的令牌数
     * @param args 额外参数
     * @throws ErrorEntryFreeException 若 {@link Context#getCurEntry()} 不是本 Entry
     */
    public abstract void exit(int count, Object... args) throws ErrorEntryFreeException;

    /**
     * 退出本 Entry。
     *
     * @param count 要释放的令牌数
     * @param args 额外参数
     * @return exit 后下一个可用 Entry，即父 Entry
     * @throws ErrorEntryFreeException 若 {@link Context#getCurEntry()} 不是本 Entry
     */
    protected abstract Entry trueExit(int count, Object... args) throws ErrorEntryFreeException;

    /**
     * 获取父 {@link Entry} 关联的 {@link Node}。
     *
     * @return 父 Entry 的 Node
     */
    public abstract Node getLastNode();

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public long getCompleteTimestamp() {
        return completeTimestamp;
    }

    public Entry setCompleteTimestamp(long completeTimestamp) {
        this.completeTimestamp = completeTimestamp;
        return this;
    }

    public Node getCurNode() {
        return curNode;
    }

    public void setCurNode(Node node) {
        this.curNode = node;
    }

    public BlockException getBlockError() {
        return blockError;
    }

    public Entry setBlockError(BlockException blockError) {
        this.blockError = blockError;
        return this;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    /**
     * 获取本 {@link Entry} 的来源 {@link Node}。
     *
     * @return 本 {@link Entry} 的来源 {@link Node}；若未通过
     * {@link ContextUtil#enter(String name, String origin)} 指定来源则可能为 null
     */
    public Node getOriginNode() {
        return originNode;
    }

    public void setOriginNode(Node originNode) {
        this.originNode = originNode;
    }

    /**
     * 类似 JDK 8 的 {@code CompletableFuture}，保证在本 Entry 终止（exit）时调用指定处理器，
     * 无论被阻断还是放行。
     * 在 Entry 上执行有状态操作时使用。
     * 
     * @param handler 调用终止时的处理函数
     * @since 1.8.0
     */
    public abstract void whenTerminate(BiConsumer<Context, Entry> handler);
    
}
