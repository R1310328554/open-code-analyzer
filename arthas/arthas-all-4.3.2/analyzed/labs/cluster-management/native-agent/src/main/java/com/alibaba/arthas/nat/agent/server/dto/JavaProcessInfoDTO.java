package com.alibaba.arthas.nat.agent.server.dto;

/**
 * 本机 Java 进程信息的数据传输对象，供 HTTP 接口返回进程列表。
 *
 * @description: Java Process DTO
 * @author：flzjkl
 * @date: 2024-09-06 21:31
 */
public class JavaProcessInfoDTO {
    /** 进程/应用名称 */
    private String processName;
    /** 进程 ID */
    private Integer pid;


    public JavaProcessInfoDTO() {

    }

    public JavaProcessInfoDTO(String applicationName, Integer pid) {
        this.processName = applicationName;
        this.pid = pid;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getPid() {
        return pid;
    }

    public String getProcessName() {
        return processName;
    }

}
