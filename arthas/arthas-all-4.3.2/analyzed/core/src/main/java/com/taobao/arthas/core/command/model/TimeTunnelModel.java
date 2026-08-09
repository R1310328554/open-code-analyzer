package com.taobao.arthas.core.command.model;

import java.util.List;
import java.util.Map;

/**
 * tt（TimeTunnel）命令的结构化结果：列表浏览、单条查看、重放与 watch 表达式输出。
 * <p>
 * 各字段按子命令互斥使用——列表模式填充 {@link #timeFragmentList}，
 * {@code tt -i} 填充 {@link #timeFragment}，重放填充 {@link #replayResult} / {@link #replayNo}；
 * {@link #isFirst} 控制客户端是否在列表首包渲染表头。
 *
 * @author gongdewei 2020/4/27
 */
public class TimeTunnelModel extends ResultModel {

    /** tt 列表模式：按索引排列的时间片摘要 */
    private List<TimeFragmentVO> timeFragmentList;

    /** 是否为该次输出的首包（Web/TTY 据此决定是否打印表头） */
    private Boolean isFirst;

    /** 单条记录详情（tt -i index） */
    private TimeFragmentVO timeFragment;

    /** 重放（tt -p）执行后的新时间片结果 */
    private TimeFragmentVO replayResult;

    /** 当前重放序号（多次 -p 时递增） */
    private Integer replayNo;

    /** watch 表达式对单条记录求值结果 */
    private ObjectVO watchValue;

    /** search 模式：{@code tt -s} 配合 {@code -w} 时 index → watch 结果 */
    private Map<Integer, ObjectVO> watchResults;

    /** 对象展开深度限制（与 tt 命令 expand 参数一致） */
    private Integer expand;

    /** 序列化字段数量上限，防止超大对象拖垮通道 */
    private Integer sizeLimit;


    @Override
    public String getType() {
        return "tt";
    }

    public List<TimeFragmentVO> getTimeFragmentList() {
        return timeFragmentList;
    }

    public TimeTunnelModel setTimeFragmentList(List<TimeFragmentVO> timeFragmentList) {
        this.timeFragmentList = timeFragmentList;
        return this;
    }

    public TimeFragmentVO getTimeFragment() {
        return timeFragment;
    }

    public TimeTunnelModel setTimeFragment(TimeFragmentVO timeFragment) {
        this.timeFragment = timeFragment;
        return this;
    }

    public Integer getExpand() {
        return expand;
    }

    public TimeTunnelModel setExpand(Integer expand) {
        this.expand = expand;
        return this;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public TimeTunnelModel setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
        return this;
    }

    public ObjectVO getWatchValue() {
        return watchValue;
    }

    public TimeTunnelModel setWatchValue(ObjectVO watchValue) {
        this.watchValue = watchValue;
        return this;
    }

    public Map<Integer, ObjectVO> getWatchResults() {
        return watchResults;
    }

    public TimeTunnelModel setWatchResults(Map<Integer, ObjectVO> watchResults) {
        this.watchResults = watchResults;
        return this;
    }

    public TimeFragmentVO getReplayResult() {
        return replayResult;
    }

    public TimeTunnelModel setReplayResult(TimeFragmentVO replayResult) {
        this.replayResult = replayResult;
        return this;
    }

    public Integer getReplayNo() {
        return replayNo;
    }

    public TimeTunnelModel setReplayNo(Integer replayNo) {
        this.replayNo = replayNo;
        return this;
    }

    public Boolean getFirst() {
        return isFirst;
    }

    public TimeTunnelModel setFirst(Boolean first) {
        isFirst = first;
        return this;
    }
}
