package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.command.model.TimeFragmentVO;
import com.taobao.arthas.core.command.model.TimeTunnelModel;
import com.taobao.arthas.core.command.monitor200.TimeTunnelTable;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import static com.taobao.arthas.core.command.monitor200.TimeTunnelTable.*;
import static java.lang.String.format;

/**
 * {@code tt}（TimeTunnel）命令的终端渲染视图。
 * <p>
 * 支持列表、单条详情、表达式 watch、条件搜索 watch 与方法重放等多种输出形态，
 * 对象展示长度受 {@link ObjectView#normalizeMaxObjectLength(int)} 约束。
 *
 * @author gongdewei 2020/4/27
 */
public class TimeTunnelView extends ResultView<TimeTunnelModel> {

    @Override
    public void draw(CommandProcess process, TimeTunnelModel timeTunnelModel) {
        int sizeLimitValue = ObjectView.normalizeMaxObjectLength(timeTunnelModel.getSizeLimit());

        if (timeTunnelModel.getTimeFragmentList() != null) {
            // 列表模式：tt -l / tt -t 展示索引表
            Element table = drawTimeTunnelTable(timeTunnelModel.getTimeFragmentList(), timeTunnelModel.getFirst());
            process.write(RenderUtil.render(table, process.width()));

        } else if (timeTunnelModel.getTimeFragment() != null) {
            // 单条详情：tt -i INDEX 展示参数/返回值/异常
            TimeFragmentVO tf = timeTunnelModel.getTimeFragment();
            TableElement table = TimeTunnelTable.createDefaultTable();
            TimeTunnelTable.drawTimeTunnel(table, tf);
            TimeTunnelTable.drawParameters(table, tf.getParams());
            TimeTunnelTable.drawReturnObj(table, tf, sizeLimitValue);
            TimeTunnelTable.drawThrowException(table, tf);
            process.write(RenderUtil.render(table, process.width()));

        } else if (timeTunnelModel.getWatchValue() != null) {
            // 单条 watch：对指定片段执行 OGNL 表达式
            ObjectVO valueVO = timeTunnelModel.getWatchValue();
            // 复杂对象需展开为树形结构
            if (valueVO.needExpand()) {
                process.write(new ObjectView(sizeLimitValue, valueVO).draw()).write("\n");
            } else {
                process.write(StringUtils.objectToString(valueVO.getObject())).write("\n");
            }

        } else if (timeTunnelModel.getWatchResults() != null) {
            // 条件搜索 + watch：tt -s 表达式 -w 表达式
            TableElement table = TimeTunnelTable.createDefaultTable();
            TimeTunnelTable.drawWatchTableHeader(table);
            TimeTunnelTable.drawWatchResults(table, timeTunnelModel.getWatchResults(), sizeLimitValue);
            process.write(RenderUtil.render(table, process.width()));

        } else if (timeTunnelModel.getReplayResult() != null) {
            // 重放模式：tt -i INDEX -p 再次调用并展示结果
            TimeFragmentVO replayResult = timeTunnelModel.getReplayResult();
            Integer replayNo = timeTunnelModel.getReplayNo();
            TableElement table = TimeTunnelTable.createDefaultTable();
            TimeTunnelTable.drawPlayHeader(replayResult.getClassName(), replayResult.getMethodName(), replayResult.getObject(), replayResult.getIndex(), table);
            TimeTunnelTable.drawParameters(table, replayResult.getParams());
            if (replayResult.isReturn()) {
                TimeTunnelTable.drawPlayResult(table, replayResult.getReturnObj(), sizeLimitValue, replayResult.getCost());
            } else {
                TimeTunnelTable.drawPlayException(table, replayResult.getThrowExp());
            }
            process.write(RenderUtil.render(table, process.width()))
                    .write(format("Time fragment[%d] successfully replayed %d times.", replayResult.getIndex(), replayNo))
                    .write("\n\n");
        }
    }

}
