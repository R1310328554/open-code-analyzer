package com.taobao.arthas.core.command.model;

/**
 * trace 命令的结构化结果：以 {@link TraceNode} 为根的调用树及节点总数。
 * <p>
 * {@link #nodeCount} 与 {@link TraceTree#getNodeCount()} 一致，供客户端限制渲染深度或
 * 提示「节点过多已截断」；根节点通常为 {@link ThreadNode}。
 *
 * @author gongdewei 2020/4/29
 */
public class TraceModel extends ResultModel {
    /** 调用树根节点（thread → method → …） */
    private TraceNode root;
    /** 树中节点总数（含 thread / method / throw 等） */
    private int nodeCount;

    public TraceModel() {
    }

    public TraceModel(TraceNode root, int nodeCount) {
        this.root = root;
        this.nodeCount = nodeCount;
    }

    @Override
    public String getType() {
        return "trace";
    }

    public TraceNode getRoot() {
        return root;
    }

    public void setRoot(TraceNode root) {
        this.root = root;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }
}
