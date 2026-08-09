package com.taobao.arthas.core.command.basic1000;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.command.model.ChangeResultVO;
import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.core.command.model.VMOptionModel;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * HotSpot 虚拟机诊断选项（VMOption）的查看与修改命令。
 * <p>
 * 通过 {@link HotSpotDiagnosticMXBean} 读写 PrintGC 等运行时诊断开关。
 * 
 * @author hengyunabc 2019-09-02
 *
 */
// @formatter:off
@Name("vmoption")
@Summary("Display, and update the vm diagnostic options.")
@Description("\nExamples:\n" + 
        "  vmoption\n" + 
        "  vmoption PrintGC\n" + 
        "  vmoption PrintGC true\n" + 
        "  vmoption PrintGCDetails true\n" + 
        Constants.WIKI + Constants.WIKI_HOME + "vmoption")
//@formatter:on
public class VMOptionCommand extends AnnotatedCommand {
    private static final Logger logger = LoggerFactory.getLogger(VMOptionCommand.class);

    /** VMOption 名称 */
    private String name;
    /** 新值，为空表示只读 */
    private String value;

    @Argument(index = 0, argName = "name", required = false)
    @Description("VMOption name")
    public void setOptionName(String name) {
        this.name = name;
    }

    @Argument(index = 1, argName = "value", required = false)
    @Description("VMOption value")
    public void setOptionValue(String value) {
        this.value = value;
    }

    @Override
    public void process(CommandProcess process) {
        run(process, name, value);
    }

    /** 列出、查询或修改 VM 诊断选项 */
    private static void run(CommandProcess process, String name, String value) {
        try {
            HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean = ManagementFactory
                            .getPlatformMXBean(HotSpotDiagnosticMXBean.class);

            if (StringUtils.isBlank(name) && StringUtils.isBlank(value)) {
                // 无参数：列出全部可写诊断选项
                process.appendResult(new VMOptionModel(hotSpotDiagnosticMXBean.getDiagnosticOptions()));
            } else if (StringUtils.isBlank(value)) {
                // 仅指定名称：查看单个选项当前值
                VMOption option = hotSpotDiagnosticMXBean.getVMOption(name);
                if (option == null) {
                    process.end(-1, "In order to change the system properties, you must specify the property value.");
                    return;
                } else {
                    process.appendResult(new VMOptionModel(Collections.singletonList(option)));
                }
            } else {
                VMOption vmOption = hotSpotDiagnosticMXBean.getVMOption(name);
                String originValue = vmOption.getValue();

                // 指定名称与值：更新选项并输出变更前后对比
                hotSpotDiagnosticMXBean.setVMOption(name, value);
                process.appendResult(new MessageModel("Successfully updated the vm option."));
                process.appendResult(new VMOptionModel(new ChangeResultVO(name, originValue,
                        hotSpotDiagnosticMXBean.getVMOption(name).getValue())));
            }
            process.end();
        } catch (Throwable t) {
            logger.error("Error during setting vm option", t);
            process.end(-1, "Error during setting vm option: " + t.getMessage());
        }
    }

    /** Tab 补全所有已注册的 VMOption 名称 */
    @Override
    public void complete(Completion completion) {
        HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean = ManagementFactory
                        .getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        List<VMOption> diagnosticOptions = hotSpotDiagnosticMXBean.getDiagnosticOptions();
        List<String> names = new ArrayList<String>(diagnosticOptions.size());
        for (VMOption option : diagnosticOptions) {
            names.add(option.getName());
        }
        CompletionUtils.complete(completion, names);
    }
}
