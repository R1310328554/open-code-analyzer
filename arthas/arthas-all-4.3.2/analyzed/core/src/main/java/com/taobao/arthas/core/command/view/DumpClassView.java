package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.DumpClassModel;
import com.taobao.arthas.core.command.model.DumpClassVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.arthas.core.util.TypeRenderUtils;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

import static com.taobao.text.ui.Element.label;

/**
 * {@code dump} 命令的终端渲染视图：展示已 dump 的 class 文件路径或 ClassLoader 消歧列表。
 * <p>
 * 多个 ClassLoader 匹配时先列出候选；成功 dump 后以表格输出 hash、ClassLoader 与磁盘路径。
 *
 * @author gongdewei 2020/4/21
 */
public class DumpClassView extends ResultView<DumpClassModel> {

    /** 优先展示 matchedClassLoaders，否则 dump 结果或 matchedClasses 列表 */
    @Override
    public void draw(CommandProcess process, DumpClassModel result) {
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }
        if (result.getDumpedClasses() != null) {
            drawDumpedClasses(process, result.getDumpedClasses());

        } else if (result.getMatchedClasses() != null) {
            Element table = ClassUtils.renderMatchedClasses(result.getMatchedClasses());
            process.write(RenderUtil.render(table)).write("\n");
        }
    }

    /** 三列表格：ClassLoader hash、名称、class 文件落盘路径 */
    private void drawDumpedClasses(CommandProcess process, List<DumpClassVO> classVOs) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("HASHCODE").style(Decoration.bold.bold()),
                new LabelElement("CLASSLOADER").style(Decoration.bold.bold()),
                new LabelElement("LOCATION").style(Decoration.bold.bold()));

        for (DumpClassVO clazz : classVOs) {
            table.row(label(clazz.getClassLoaderHash()).style(Decoration.bold.fg(Color.red)),
                    TypeRenderUtils.drawClassLoader(clazz),
                    label(clazz.getLocation()).style(Decoration.bold.fg(Color.red)));
        }

        process.write(RenderUtil.render(table, process.width()))
                .write(com.taobao.arthas.core.util.Constants.EMPTY_STRING);
    }

}
