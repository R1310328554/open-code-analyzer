package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.GetStaticModel;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.text.ui.Element;
import com.taobao.text.util.RenderUtil;

/**
 * {@code getstatic} 命令的终端渲染视图：输出静态字段值或 ClassLoader/类名消歧信息。
 * <p>
 * 字段值经 {@link ObjectView} 按 expand 深度展开；多 ClassLoader 时复用 {@link ClassLoaderView}。
 *
 * @author gongdewei 2020/4/20
 */
public class GetStaticView extends ResultView<GetStaticModel> {

    /** 消歧 ClassLoader → 输出 field 值 → 或展示 matchedClasses 列表 */
    @Override
    public void draw(CommandProcess process, GetStaticModel result) {
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }
        if (result.getField() != null) {
            ObjectVO field = result.getField();
            String valueStr = StringUtils.objectToString(field.needExpand() ? new ObjectView(field).draw() : field.getObject());
            process.write("field: " + result.getFieldName() + "\n" + valueStr + "\n");
        } else if (result.getMatchedClasses() != null) {
            Element table = ClassUtils.renderMatchedClasses(result.getMatchedClasses());
            process.write(RenderUtil.render(table)).write("\n");
        }
    }
}
