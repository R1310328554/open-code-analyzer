package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.model.TraceModel;
import com.taobao.arthas.core.command.model.TraceTree;
import com.taobao.arthas.core.util.ThreadUtil;

/**
 * {@code trace} 命令的线程局部上下文：持有调用树 {@link TraceTree} 与当前嵌套深度。
 * <p>
 * 每个线程在首次进入被 trace 方法时懒创建；根调用返回时通过 {@link #getModel()}
 * 裁剪树并封装为 {@link TraceModel} 供输出。
 *
 * @author ralf0131 2017-01-05 14:05.
 */
public class TraceEntity {

    /** 当前线程的方法调用树 */
    protected TraceTree tree;
    /** 根方法到当前位置的嵌套深度，用于判断根调用是否结束 */
    protected int deep;

    /** 以当前线程为根节点初始化 TraceTree */
    public TraceEntity(ClassLoader loader) {
        this.tree = createTraceTree(loader);
        this.deep = 0;
    }

    private TraceTree createTraceTree(ClassLoader loader) {
        return new TraceTree(ThreadUtil.getThreadNode(loader, Thread.currentThread()));
    }

    /** 裁剪空分支后构造 TraceModel，供 AbstractTraceAdviceListener 输出 */
    public TraceModel getModel() {
        tree.trim();
        return new TraceModel(tree.getRoot(), tree.getNodeCount());
    }
}
