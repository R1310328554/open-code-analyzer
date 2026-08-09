package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.Base64Model;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code base64} 命令的终端渲染视图：将 {@link Base64Model} 中的编码/解码结果原样写出。
 * <p>
 * content 为 null 时仅输出换行，避免 NPE。
 *
 * @author hengyunabc 2021-01-05
 */
public class Base64View extends ResultView<Base64Model> {

    /** 写出 Base64 转换结果并追加换行 */
    @Override
    public void draw(CommandProcess process, Base64Model result) {
        String content = result.getContent();
        if (content != null) {
            process.write(content);
        }
        process.write("\n");
    }

}
