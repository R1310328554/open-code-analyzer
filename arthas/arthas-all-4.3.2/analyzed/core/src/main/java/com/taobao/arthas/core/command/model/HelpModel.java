package com.taobao.arthas.core.command.model;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code help} 命令的结果模型，支持命令列表与单命令详情两种形态。
 * <p>
 * 无参数时填充 {@link #commands}；指定命令名时填充 {@link #detailCommand}。
 * 二者互斥使用，客户端根据非空字段决定渲染模式。
 *
 * @author gongdewei 2020/4/3
 */
public class HelpModel extends ResultModel {

    //list
    /** 全部（或过滤后）命令的摘要列表 */
    private List<CommandVO> commands;

    //details
    /** 单个命令的完整 help 详情（含 options、arguments） */
    private CommandVO detailCommand;

    public HelpModel() {
    }

    public HelpModel(List<CommandVO> commands) {
        this.commands = commands;
    }

    public HelpModel(CommandVO command) {
        this.detailCommand = command;
    }

    /** 懒初始化 commands 并追加一项，用于逐项构建 help 列表 */
    public void addCommandVO(CommandVO commandVO){
        if (commands == null) {
            commands = new ArrayList<CommandVO>();
        }
        this.commands.add(commandVO);
    }

    public List<CommandVO> getCommands() {
        return commands;
    }

    public void setCommands(List<CommandVO> commands) {
        this.commands = commands;
    }

    public CommandVO getDetailCommand() {
        return detailCommand;
    }

    @Override
    public String getType() {
        return "help";
    }
}
