package com.taobao.arthas.core.command.basic1000;

import com.taobao.arthas.core.command.model.SessionModel;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.util.UserStatUtil;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

import com.alibaba.arthas.tunnel.client.TunnelClient;

/**
 * 查看当前 Arthas 会话状态命令，汇总 PID、会话 ID、Tunnel 连接与用户标识等信息。
 *
 * @author vlinux on 15/5/3.
 */
@Name("session")
@Summary("Display current session information")
public class SessionCommand extends AnnotatedCommand {

    @Override
    public void process(CommandProcess process) {
        SessionModel result = new SessionModel();
        Session session = process.session();
        // 填充目标 JVM 进程号与 shell 会话标识
        result.setJavaPid(session.getPid());
        result.setSessionId(session.getSessionId());

        // 若已启用 Tunnel 客户端，附加 Agent ID、服务端地址与连接状态
        TunnelClient tunnelClient = ArthasBootstrap.getInstance().getTunnelClient();
        if (tunnelClient != null) {
            String id = tunnelClient.getId();
            if (id != null) {
                result.setAgentId(id);
            }
            result.setTunnelServer(tunnelClient.getTunnelServerUrl());
            result.setTunnelConnected(tunnelClient.isConnected());
        }

        // 统计上报 URL（若配置了用户统计）
        String statUrl = UserStatUtil.getStatUrl();
        result.setStatUrl(statUrl);

        // 当前登录用户 ID
        String userId = session.getUserId();
        result.setUserId(userId);

        process.appendResult(result);
        process.end();
    }

}
