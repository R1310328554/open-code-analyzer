package com.taobao.arthas.core.command.model;

import com.taobao.arthas.core.command.monitor200.MonitorData;

import java.util.List;

/**
 * monitor 命令的结构化结果：周期性采样的方法调用统计快照列表。
 * <p>
 * 每条 {@link MonitorData} 对应一个被监控方法在采样窗口内的调用次数、失败次数、
 * 平均耗时等聚合指标；Web Console 据此渲染表格或图表。
 *
 * @author gongdewei 2020/4/28
 */
public class MonitorModel extends ResultModel {

    /** 各被监控方法在当前采样周期内的统计数据 */
    private List<MonitorData> monitorDataList;

    public MonitorModel() {
    }

    public MonitorModel(List<MonitorData> monitorDataList) {
        this.monitorDataList = monitorDataList;
    }

    @Override
    public String getType() {
        return "monitor";
    }

    public List<MonitorData> getMonitorDataList() {
        return monitorDataList;
    }

    public void setMonitorDataList(List<MonitorData> monitorDataList) {
        this.monitorDataList = monitorDataList;
    }
}
