package com.taobao.arthas.core.command.model;

import java.util.ArrayList;
import java.util.List;

/**
 * trace 调用树的抽象节点：维护父子关系、节点类型与可选备注标记。
 * <p>
 * 子类包括 {@link ThreadNode}（根）、{@link MethodNode}（方法帧）、{@link ThrowNode}（异常）；
 * {@link #begin()} / {@link #end()} 由 {@link TraceTree} 在进出方法时回调，子类可覆写计时逻辑。
 *
 * @author gongdewei 2020/4/28
 */
public abstract class TraceNode {

    /** 父节点；根节点的 parent 为 null */
    protected TraceNode parent;
    /** 子节点列表，懒创建以节省无分支调用时的内存 */
    protected List<TraceNode> children;

    /** 节点类型：thread / method / throw 等，供 JSON 反序列化区分 */
    private String type;

    /** 备注文本（如异常路径上的 "throws Exception"） */
    private String mark;
    /** setMark 调用次数；历史字段，可能与多次标记场景相关 */
    private int marks = 0;

    public TraceNode(String type) {
        this.type = type;
    }

    /** 追加子节点并建立双向 parent 链接 */
    public void addChild(TraceNode child) {
        if (children == null) {
            children = new ArrayList<TraceNode>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    /** 设置备注并递增 marks 计数 */
    public void setMark(String mark) {
        this.mark = mark;
        marks++;
    }

    public String getMark() {
        return mark;
    }

    public Integer marks() {
        return marks;
    }

    /** 进入该节点时回调（MethodNode 可在此记录开始时间） */
    public void begin() {
    }

    /** 离开该节点时回调 */
    public void end() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TraceNode parent() {
        return parent;
    }

    public void setParent(TraceNode parent) {
        this.parent = parent;
    }

    public List<TraceNode> getChildren() {
        return children;
    }
}
