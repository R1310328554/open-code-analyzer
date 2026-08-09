package com.taobao.arthas.core.command.hidden;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ArthasBanner;
import com.taobao.middleware.cli.annotations.Hidden;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 隐藏致谢命令 {@code thanks}：输出 Arthas 贡献者与帮助组织的 credits 信息。
 * <p>
 * 实际内容由 {@link com.taobao.arthas.core.util.ArthasBanner#credit()} 生成；
 * 命令为 Hidden，仅供知晓口令的用户查看。
 *
 * @author vlinux on 15/9/1.
 */
@Name("thanks")
@Summary("Credits to all personnel and organization who either contribute or help to this product. Thanks you all!")
@Hidden
public class ThanksCommand extends AnnotatedCommand {

    /** 打印 Banner credits 并换行结束 */
    @Override
    public void process(CommandProcess process) {
        process.write(ArthasBanner.credit()).write("\n").end();
    }
}
