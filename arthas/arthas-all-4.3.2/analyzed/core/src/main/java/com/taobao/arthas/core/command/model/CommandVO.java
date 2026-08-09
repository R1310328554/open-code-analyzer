package com.taobao.arthas.core.command.model;

import com.taobao.middleware.cli.CLI;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条 Arthas 命令的元数据视图，聚合名称、用法、选项与位置参数。
 * <p>
 * 用于 help 命令的列表与详情输出；{@link #cli} 为 transient，仅服务端解析用，
 * 不参与 JSON 序列化（TODO 后续计划移除对 CLI 对象的直接持有）。
 *
 * @author gongdewei 2020/4/3
 */
public class CommandVO {
    //TODO remove cli
    /** 底层 CLI 定义，transient 避免序列化到客户端 */
    private transient CLI cli;
    /** 命令名，如 watch、trace */
    private String name;
    /** 命令功能简述 */
    private String description;
    /** 完整用法字符串（含选项占位） */
    private String usage;
    /** 一行摘要，用于命令列表 */
    private String summary;
    /** 可选参数列表 */
    private List<CommandOptionVO> options = new ArrayList<CommandOptionVO>();
    /** 位置参数列表 */
    private List<ArgumentVO> arguments = new ArrayList<ArgumentVO>();

    public CommandVO() {
    }

    public CommandVO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** 链式追加选项，便于构建 help 元数据 */
    public CommandVO addOption(CommandOptionVO optionVO){
        this.options.add(optionVO);
        return this;
    }

    /** 链式追加位置参数 */
    public CommandVO addArgument(ArgumentVO argumentVO){
        this.arguments.add(argumentVO);
        return this;
    }

    public CLI cli() {
        return cli;
    }

    public void setCli(CLI cli) {
        this.cli = cli;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<CommandOptionVO> getOptions() {
        return options;
    }

    public void setOptions(List<CommandOptionVO> options) {
        this.options = options;
    }

    public List<ArgumentVO> getArguments() {
        return arguments;
    }

    public void setArguments(List<ArgumentVO> arguments) {
        this.arguments = arguments;
    }
}
