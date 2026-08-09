package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.klass100.RetransformCommand.RetransformEntry;
import com.taobao.arthas.core.command.model.RetransformModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

/**
 * {@code retransform} 命令的终端渲染视图。
 * <p>
 * 按模型字段区分四种输出模式：ClassLoader 歧义列表、删除条目确认（{@code -d}）、
 * 条目列表（{@code -l}）以及热替换成功类名列表。
 *
 * @author hengyunabc 2021-01-06
 */
public class RetransformView extends ResultView<RetransformModel> {

    @Override
    public void draw(CommandProcess process, RetransformModel result) {
        // 匹配到多个 classloader
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }

        // retransform -d：按 id 删除已注册的 RetransformEntry
        if (result.getDeletedRetransformEntry() != null) {
            process.write("Delete RetransformEntry by id success. id: " + result.getDeletedRetransformEntry().getId());
            process.write("\n");
            return;
        }

        // retransform -l：表格列出所有 transform 条目
        if (result.getRetransformEntries() != null) {
            TableElement table = new TableElement(1, 1, 1, 1, 1).rightCellPadding(1);
            table.add(new RowElement().style(Decoration.bold.bold()).add("Id", "ClassName", "TransformCount", "LoaderHash",
                    "LoaderClassName"));

            for (RetransformEntry entry : result.getRetransformEntries()) {
                table.row("" + entry.getId(), "" + entry.getClassName(), "" + entry.getTransformCount(), "" + entry.getHashCode(),
                        "" + entry.getClassLoaderClass());
            }

            process.write(RenderUtil.render(table));
            return;
        }

        // retransform /tmp/Demo.class：输出成功替换的类名
        if (result.getRetransformClasses() != null) {
            StringBuilder sb = new StringBuilder();
            for (String aClass : result.getRetransformClasses()) {
                sb.append(aClass).append("\n");
            }
            process.write("retransform success, size: " + result.getRetransformCount()).write(", classes:\n")
                    .write(sb.toString());
        }

    }

}
