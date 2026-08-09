package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * line 命令「列举可用行号」阶段的结构化结果。
 * <p>
 * 在用户对某方法设置断点/观测行前，先返回该方法在源码中可插桩的行号列表，
 * 供 CLI 或 Web Console 展示可选行。
 */
public class LineListModel extends ResultModel {
    /** 目标类的全限定名 */
    private String className;
    /** 对应源文件名（可能为 Unknown Source） */
    private String sourceFile;
    /** 目标方法名 */
    private String methodName;
    /** 方法 JVM 描述符（参数与返回类型签名） */
    private String methodDesc;
    /** 该方法体内可用于 line 观测的行号集合 */
    private List<Integer> lines;

    @Override
    public String getType() {
        return "line-list";
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public void setLines(List<Integer> lines) {
        this.lines = lines;
    }
}
