package com.alibaba.arthas.tunnel.server.app.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * arthas agent数据回报的演示接口
 * @author hengyunabc 2019-09-24
 *
 */
@Controller
public class StatController {
    private final static Logger logger = LoggerFactory.getLogger(StatController.class);

    /**
     * 接收 agent 上报的统计/遥测数据（演示用途，仅记录日志并返回 success）。
     *
     * @param ip agent 所在主机 IP
     * @param version Arthas 版本
     * @param agentId agent 标识（可选）
     * @param command 执行的命令
     * @param arguments 命令参数
     */
    @RequestMapping(value = "/api/stat")
    @ResponseBody
    public Map<String, Object> execute(@RequestParam(value = "ip", required = true) String ip,
            @RequestParam(value = "version", required = true) String version,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "command", required = true) String command,
            @RequestParam(value = "arguments", required = false, defaultValue = "") String arguments) {

        logger.info("arthas stat, ip: {}, version: {}, agentId: {}, command: {}, arguments: {}", ip, version, agentId, command, arguments);

        Map<String, Object> result = new HashMap<>();

        result.put("success", true);

        return result;
    }
}
