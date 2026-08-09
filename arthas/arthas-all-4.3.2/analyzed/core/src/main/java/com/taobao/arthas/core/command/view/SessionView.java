package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.SessionModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import static com.taobao.text.ui.Element.label;

/**
 * {@code session} 命令的终端渲染视图：以键值表展示当前会话元数据。
 * <p>
 * 固定输出 JAVA_PID、SESSION_ID；Agent/Tunnel/Stat/用户等字段仅在模型非空时追加行。
 *
 * @author gongdewei 2020/3/27
 */
public class SessionView extends ResultView<SessionModel> {

    @Override
    public void draw(CommandProcess process, SessionModel result) {
        // 会话详情：Name-Value 两列表格
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("Name").style(Decoration.bold.bold()), label("Value").style(Decoration.bold.bold()));
        table.row("JAVA_PID", "" + result.getJavaPid()).row("SESSION_ID", "" + result.getSessionId());
        if (result.getAgentId() != null) {
            table.row("AGENT_ID", "" + result.getAgentId());
        }
        if (result.getTunnelServer() != null) {
            table.row("TUNNEL_SERVER", "" + result.getTunnelServer());
            table.row("TUNNEL_CONNECTED", "" + result.isTunnelConnected());
        }
        if (result.getStatUrl() != null) {
            table.row("STAT_URL", result.getStatUrl());
        }
        if (result.getUserId() != null) {
            table.row("USER_ID", result.getUserId());
        }
        process.write(RenderUtil.render(table, process.width()));
    }

}
