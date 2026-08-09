package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.CatModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code cat} 命令的终端渲染视图：将文件内容 {@link CatModel#getContent()} 写入终端。
 * <p>
 * 文本模式直接输出；二进制或非 UTF-8 内容由 CatCommand 侧已处理为可读字符串。
 *
 * @author gongdewei 2020/5/11
 */
public class CatView extends ResultView<CatModel> {

    /** 输出文件内容并换行 */
    @Override
    public void draw(CommandProcess process, CatModel result) {
        process.write(result.getContent()).write("\n");
    }

}
