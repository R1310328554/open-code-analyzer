package com.taobao.arthas.core.command.model;

/**
 * 命令执行结果的抽象基类，所有具体结果模型均继承此类。
 * <p>
 * 子类通过 {@link #getType()} 返回命令类型标识，{@link #jobId} 关联后台 Job。
 *
 * @author gongdewei 2020-03-26
 */
public abstract class ResultModel {

    /** 关联的后台 Job ID。 */
    private int jobId;

    /**
     * 返回命令类型（名称），供 JSON 序列化区分不同结果模型。
     *
     * @return 类型字符串，如 "session"、"status"
     */
    public abstract String getType();


    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
