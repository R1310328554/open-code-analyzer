package com.taobao.arthas.core.command.model;


import com.taobao.arthas.core.util.StringUtils;

import java.util.List;

/**
 * trace 命令的运行时调用树构建器：在字节码增强回调中维护当前栈指针与节点计数。
 * <p>
 * 同一 {@link MethodNode}（类名+方法名+行号）在兄弟层只创建一次，递归调用会复用节点并
 * 再次 {@link #begin()}；追踪结束后 {@link #trim()} 统一规范化内部类名显示。
 *
 * @author gongdewei 2020/4/28
 */
public class TraceTree {
    /** 树根，固定为 {@link ThreadNode} */
    private TraceNode root;

    /** 当前栈顶节点，随 begin/end 在树中上下移动 */
    private TraceNode current;
    /** 已访问/创建的节点总数（含复用时的 begin 计数） */
    private int nodeCount = 0;

    public TraceTree(ThreadNode root) {
        this.root = root;
        this.current = root;
    }

    /**
     * 进入一次方法调用：查找或创建 {@link MethodNode}，下移 current。
     *
     * @param className 被调类名（可能为内部类 $ 形式，trim 前不规范化）
     * @param methodName 方法名
     * @param lineNumber 调用点行号
     * @param isInvoking 是否为跨类 invoke（影响 MethodNode 展示）
     */
    public void begin(String className, String methodName, int lineNumber, boolean isInvoking) {
        TraceNode child = findChild(current, className, methodName, lineNumber);
        if (child == null) {
            child = new MethodNode(className, methodName, lineNumber, isInvoking);
            current.addChild(child);
        }
        child.begin();
        current = child;
        nodeCount += 1;
    }

    /** 在当前节点的直接子节点中按三元组匹配已有 MethodNode */
    private TraceNode findChild(TraceNode node, String className, String methodName, int lineNumber) {
        List<TraceNode> childList = node.getChildren();
        if (childList != null) {
            // 索引 for 循环比 foreach 少分配 Iterator，热路径上略省内存
            for (int i = 0; i < childList.size(); i++) {
                TraceNode child = childList.get(i);
                if (matchNode(child, className, methodName, lineNumber)) {
                    return child;
                }
            }
        }
        return null;
    }

    private boolean matchNode(TraceNode node, String className, String methodName, int lineNumber) {
        if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            if (lineNumber != methodNode.getLineNumber()) return false;
            if (className != null ? !className.equals(methodNode.getClassName()) : methodNode.getClassName() != null) return false;
            return methodName != null ? methodName.equals(methodNode.getMethodName()) : methodNode.getMethodName() == null;
        }
        return false;
    }

    /** 正常返回：结束当前节点并回退到父节点 */
    public void end() {
        current.end();
        if (current.parent() != null) {
            // end 次数多于 begin 时 parent 可能已为 null，此处静默忽略
            current = current.parent();
        }
    }

    /** 异常路径：挂 {@link ThrowNode} 后按 throw 语义结束当前方法帧 */
    public void end(Throwable throwable, int lineNumber) {
        ThrowNode throwNode = new ThrowNode();
        throwNode.setException(throwable.getClass().getName());
        throwNode.setMessage(throwable.getMessage());
        throwNode.setLineNumber(lineNumber);
        current.addChild(throwNode);
        this.end(true);
    }

    /** isThrow 为 true 时标记当前 MethodNode 并设置 throw 标志 */
    public void end(boolean isThrow) {
        if (isThrow) {
            current.setMark("throws Exception");
            if (current instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) current;
                methodNode.setThrow(true);
            }
        }
        this.end();
    }

    /** 追踪结束后修整树：递归规范化 MethodNode 的类名 */
    public void trim() {
        this.normalizeClassName(root);
    }

    /**
     * 将内部类 $ 等形式转为点分展示名；在 trace 结束统一执行，避免热路径重复转换。
     *
     * @param node 当前遍历节点
     */
    private void normalizeClassName(TraceNode node) {
        if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            String nodeClassName = methodNode.getClassName();
            String normalizeClassName = StringUtils.normalizeClassName(nodeClassName);
            methodNode.setClassName(normalizeClassName);
        }
        List<TraceNode> children = node.getChildren();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                TraceNode child = children.get(i);
                normalizeClassName(child);
            }
        }
    }

    public TraceNode getRoot() {
        return root;
    }

    public TraceNode current() {
        return current;
    }

    public int getNodeCount() {
        return nodeCount;
    }
}
