package com.taobao.arthas.core.command.model;

/**
 * pwd 命令的结构化结果：Arthas 会话当前工作目录。
 * <p>
 * 工作目录影响类路径扫描、文件输出（如 profiler dump、jad 导出）的相对路径解析；
 * 可通过 cd 命令变更，本模型仅反映执行 pwd 时刻的路径快照。
 *
 * @author gongdewei 2020/5/11
 */
public class PwdModel extends ResultModel {
    /** 当前会话工作目录的绝对或规范化路径 */
    private String workingDir;

    public PwdModel() {
    }

    public PwdModel(String workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public String getType() {
        return "pwd";
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
}
