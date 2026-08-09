package com.taobao.arthas.core.command.basic1000;


import com.taobao.arthas.core.command.model.VersionModel;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ArthasBanner;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 输出当前 Arthas 版本号，版本字符串来自 {@link ArthasBanner#version()}。
 *
 * @author vlinux
 */
@Name("version")
@Summary("Display Arthas version")
public class VersionCommand extends AnnotatedCommand {

    @Override
    public void process(CommandProcess process) {
        VersionModel result = new VersionModel();
        result.setVersion(ArthasBanner.version());
        process.appendResult(result);
        process.end();
    }

}
