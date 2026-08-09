#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave2 batch files [0:20]."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ConditionMessage.java": [
        (
            "/**\n * A message associated with a {@link ConditionOutcome}. Provides a fluent builder style\n * API to encourage consistency across all condition messages.\n *\n * @author Phillip Webb\n * @since 1.4.1\n */",
            "/**\n * 与 {@link ConditionOutcome} 关联的消息。提供流式构建器风格 API，\n * 以促使所有条件消息保持一致。\n *\n * @author Phillip Webb\n * @since 1.4.1\n */",
        ),
        (
            "/**\n\t * Return {@code true} if the message is empty.\n\t * @return if the message is empty\n\t */",
            "/**\n\t * 若消息为空则返回 {@code true}。\n\t * @return 消息是否为空\n\t */",
        ),
        (
            "/**\n\t * Return a new {@link ConditionMessage} based on the instance and an appended\n\t * message.\n\t * @param message the message to append\n\t * @return a new {@link ConditionMessage} instance\n\t */",
            "/**\n\t * 基于当前实例及追加消息返回新的 {@link ConditionMessage}。\n\t * @param message 要追加的消息\n\t * @return 新的 {@link ConditionMessage} 实例\n\t */",
        ),
        (
            "/**\n\t * Return a new builder to construct a new {@link ConditionMessage} based on the\n\t * instance and a new condition outcome.\n\t * @param condition the condition\n\t * @param details details of the condition\n\t * @return a {@link Builder} builder\n\t * @see #andCondition(String, Object...)\n\t * @see #forCondition(Class, Object...)\n\t */",
            "/**\n\t * 基于当前实例及新的条件评估结果，返回用于构建新 {@link ConditionMessage} 的构建器。\n\t * @param condition 条件\n\t * @param details 条件详情\n\t * @return {@link Builder} 构建器\n\t * @see #andCondition(String, Object...)\n\t * @see #forCondition(Class, Object...)\n\t */",
        ),
        (
            "/**\n\t * Return a new builder to construct a new {@link ConditionMessage} based on the\n\t * instance and a new condition outcome.\n\t * @param condition the condition\n\t * @param details details of the condition\n\t * @return a {@link Builder} builder\n\t * @see #andCondition(Class, Object...)\n\t * @see #forCondition(String, Object...)\n\t */",
            "/**\n\t * 基于当前实例及新的条件评估结果，返回用于构建新 {@link ConditionMessage} 的构建器。\n\t * @param condition 条件\n\t * @param details 条件详情\n\t * @return {@link Builder} 构建器\n\t * @see #andCondition(Class, Object...)\n\t * @see #forCondition(String, Object...)\n\t */",
        ),
        (
            "/**\n\t * Factory method to return a new empty {@link ConditionMessage}.\n\t * @return a new empty {@link ConditionMessage}\n\t */",
            "/**\n\t * 返回新的空 {@link ConditionMessage} 的工厂方法。\n\t * @return 新的空 {@link ConditionMessage}\n\t */",
        ),
        (
            "/**\n\t * Factory method to create a new {@link ConditionMessage} with a specific message.\n\t * @param message the source message (may be a format string if {@code args} are\n\t * specified)\n\t * @param args format arguments for the message\n\t * @return a new {@link ConditionMessage} instance\n\t */",
            "/**\n\t * 使用指定消息创建新 {@link ConditionMessage} 的工厂方法。\n\t * @param message 源消息（若指定 {@code args} 则可为格式字符串）\n\t * @param args 消息的格式参数\n\t * @return 新的 {@link ConditionMessage} 实例\n\t */",
        ),
        (
            "/**\n\t * Factory method to create a new {@link ConditionMessage} comprised of the specified\n\t * messages.\n\t * @param messages the source messages (may be {@code null})\n\t * @return a new {@link ConditionMessage} instance\n\t */",
            "/**\n\t * 由指定消息组合创建新 {@link ConditionMessage} 的工厂方法。\n\t * @param messages 源消息（可为 {@code null}）\n\t * @return 新的 {@link ConditionMessage} 实例\n\t */",
        ),
        (
            "/**\n\t * Factory method for a builder to construct a new {@link ConditionMessage} for a\n\t * condition.\n\t * @param condition the condition\n\t * @param details details of the condition\n\t * @return a {@link Builder} builder\n\t * @see #forCondition(String, Object...)\n\t * @see #andCondition(String, Object...)\n\t */",
            "/**\n\t * 为条件构建新 {@link ConditionMessage} 的构建器工厂方法。\n\t * @param condition 条件\n\t * @param details 条件详情\n\t * @return {@link Builder} 构建器\n\t * @see #forCondition(String, Object...)\n\t * @see #andCondition(String, Object...)\n\t */",
        ),
        (
            "/**\n\t * Factory method for a builder to construct a new {@link ConditionMessage} for a\n\t * condition.\n\t * @param condition the condition\n\t * @param details details of the condition\n\t * @return a {@link Builder} builder\n\t * @see #forCondition(Class, Object...)\n\t * @see #andCondition(String, Object...)\n\t */",
            "/**\n\t * 为条件构建新 {@link ConditionMessage} 的构建器工厂方法。\n\t * @param condition 条件\n\t * @param details 条件详情\n\t * @return {@link Builder} 构建器\n\t * @see #forCondition(Class, Object...)\n\t * @see #andCondition(String, Object...)\n\t */",
        ),
        (
            "/**\n\t * Builder used to create a {@link ConditionMessage} for a condition.\n\t */",
            "/**\n\t * 用于为条件创建 {@link ConditionMessage} 的构建器。\n\t */",
        ),
        (
            "/**\n\t\t * Indicate that an exact result was found. For example\n\t\t * {@code foundExactly(\"foo\")} results in the message \"found foo\".\n\t\t * @param result the result that was found\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 表示找到了精确结果。例如 {@code foundExactly(\"foo\")} 生成消息 \"found foo\"。\n\t\t * @param result 找到的结果\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate that one or more results were found. For example\n\t\t * {@code found(\"bean\").items(\"x\")} results in the message \"found bean x\".\n\t\t * @param article the article found\n\t\t * @return an {@link ItemsBuilder}\n\t\t */",
            "/**\n\t\t * 表示找到一个或多个结果。例如 {@code found(\"bean\").items(\"x\")} 生成消息 \"found bean x\"。\n\t\t * @param article 找到项的冠词\n\t\t * @return {@link ItemsBuilder}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate that one or more results were found. For example\n\t\t * {@code found(\"bean\", \"beans\").items(\"x\", \"y\")} results in the message \"found\n\t\t * beans x, y\".\n\t\t * @param singular the article found in singular form\n\t\t * @param plural the article found in plural form\n\t\t * @return an {@link ItemsBuilder}\n\t\t */",
            "/**\n\t\t * 表示找到一个或多个结果。例如 {@code found(\"bean\", \"beans\").items(\"x\", \"y\")}\n\t\t * 生成消息 \"found beans x, y\"。\n\t\t * @param singular 单数形式的冠词\n\t\t * @param plural 复数形式的冠词\n\t\t * @return {@link ItemsBuilder}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate that one or more results were not found. For example\n\t\t * {@code didNotFind(\"bean\").items(\"x\")} results in the message \"did not find bean\n\t\t * x\".\n\t\t * @param article the article found\n\t\t * @return an {@link ItemsBuilder}\n\t\t */",
            "/**\n\t\t * 表示未找到一个或多个结果。例如 {@code didNotFind(\"bean\").items(\"x\")}\n\t\t * 生成消息 \"did not find bean x\"。\n\t\t * @param article 未找到项的冠词\n\t\t * @return {@link ItemsBuilder}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate that one or more results were found. For example\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")} results in the message \"did\n\t\t * not find beans x, y\".\n\t\t * @param singular the article found in singular form\n\t\t * @param plural the article found in plural form\n\t\t * @return an {@link ItemsBuilder}\n\t\t */",
            "/**\n\t\t * 表示未找到一个或多个结果。例如 {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")}\n\t\t * 生成消息 \"did not find beans x, y\"。\n\t\t * @param singular 单数形式的冠词\n\t\t * @param plural 复数形式的冠词\n\t\t * @return {@link ItemsBuilder}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicates a single result. For example {@code resultedIn(\"yes\")} results in the\n\t\t * message \"resulted in yes\".\n\t\t * @param result the result\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 表示单一结果。例如 {@code resultedIn(\"yes\")} 生成消息 \"resulted in yes\"。\n\t\t * @param result 结果\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicates something is available. For example {@code available(\"money\")}\n\t\t * results in the message \"money is available\".\n\t\t * @param item the item that is available\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 表示某项可用。例如 {@code available(\"money\")} 生成消息 \"money is available\"。\n\t\t * @param item 可用的项\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicates something is not available. For example {@code notAvailable(\"time\")}\n\t\t * results in the message \"time is not available\".\n\t\t * @param item the item that is not available\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 表示某项不可用。例如 {@code notAvailable(\"time\")} 生成消息 \"time is not available\"。\n\t\t * @param item 不可用的项\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicates the reason. For example {@code because(\"running Linux\")} results in\n\t\t * the message \"running Linux\".\n\t\t * @param reason the reason for the message\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 表示原因。例如 {@code because(\"running Linux\")} 生成消息 \"running Linux\"。\n\t\t * @param reason 消息的原因\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t * Builder used to create an {@link ItemsBuilder} for a condition.\n\t */",
            "/**\n\t * 用于为条件创建 {@link ItemsBuilder} 的构建器。\n\t */",
        ),
        (
            "/**\n\t\t * Used when no items are available. For example\n\t\t * {@code didNotFind(\"any beans\").atAll()} results in the message \"did not find\n\t\t * any beans\".\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 在无可用项时使用。例如 {@code didNotFind(\"any beans\").atAll()}\n\t\t * 生成消息 \"did not find any beans\"。\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate the items. For example\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")} results in the message \"did\n\t\t * not find beans x, y\".\n\t\t * @param items the items (may be {@code null})\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 指定项。例如 {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")}\n\t\t * 生成消息 \"did not find beans x, y\"。\n\t\t * @param items 项（可为 {@code null}）\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate the items. For example\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")} results in the message \"did\n\t\t * not find beans x, y\".\n\t\t * @param style the render style\n\t\t * @param items the items (may be {@code null})\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 指定项。例如 {@code didNotFind(\"bean\", \"beans\").items(\"x\", \"y\")}\n\t\t * 生成消息 \"did not find beans x, y\"。\n\t\t * @param style 渲染样式\n\t\t * @param items 项（可为 {@code null}）\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate the items. For example\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(Collections.singleton(\"x\")} results in\n\t\t * the message \"did not find bean x\".\n\t\t * @param items the source of the items (may be {@code null})\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 指定项。例如 {@code didNotFind(\"bean\", \"beans\").items(Collections.singleton(\"x\")}\n\t\t * 生成消息 \"did not find bean x\"。\n\t\t * @param items 项的来源（可为 {@code null}）\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicate the items with a {@link Style}. For example\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(Style.QUOTE, Collections.singleton(\"x\")}\n\t\t * results in the message \"did not find bean 'x'\".\n\t\t * @param style the render style\n\t\t * @param items the source of the items (may be {@code null})\n\t\t * @return a built {@link ConditionMessage}\n\t\t */",
            "/**\n\t\t * 使用 {@link Style} 指定项。例如\n\t\t * {@code didNotFind(\"bean\", \"beans\").items(Style.QUOTE, Collections.singleton(\"x\")}\n\t\t * 生成消息 \"did not find bean 'x'\"。\n\t\t * @param style 渲染样式\n\t\t * @param items 项的来源（可为 {@code null}）\n\t\t * @return 构建完成的 {@link ConditionMessage}\n\t\t */",
        ),
        (
            "/**\n\t * Render styles.\n\t */",
            "/**\n\t * 渲染样式。\n\t */",
        ),
        (
            "/**\n\t\t * Render with normal styling.\n\t\t */",
            "/**\n\t\t * 以普通样式渲染。\n\t\t */",
        ),
        (
            "/**\n\t\t * Render with the item surrounded by quotes.\n\t\t */",
            "/**\n\t\t * 以引号包裹项进行渲染。\n\t\t */",
        ),
    ],
    "ProjectInfoAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for various project information.\n *\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @since 1.4.0\n */",
            "/**\n * 各类项目信息的 {@link EnableAutoConfiguration 自动配置}。\n *\n * @author Stephane Nicoll\n * @author Madhura Bhave\n * @since 1.4.0\n */",
        ),
    ],
    "ProjectInfoProperties.java": [
        (
            "/**\n * Configuration properties for project information.\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
            "/**\n * 项目信息的配置属性。\n *\n * @author Stephane Nicoll\n * @since 1.4.0\n */",
        ),
        (
            "/**\n\t * Build specific info properties.\n\t */",
            "/**\n\t * 构建信息相关属性。\n\t */",
        ),
        (
            "/**\n\t\t * Location of the generated build-info.properties file.\n\t\t */",
            "/**\n\t\t * 生成的 build-info.properties 文件位置。\n\t\t */",
        ),
        (
            "/**\n\t\t * File encoding.\n\t\t */",
            "/**\n\t\t * 文件编码。\n\t\t */",
        ),
        (
            "/**\n\t * Git specific info properties.\n\t */",
            "/**\n\t * Git 信息相关属性。\n\t */",
        ),
        (
            "/**\n\t\t * Location of the generated git.properties file.\n\t\t */",
            "/**\n\t\t * 生成的 git.properties 文件位置。\n\t\t */",
        ),
    ],
    "JmxAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} to enable/disable Spring's\n * {@link EnableMBeanExport @EnableMBeanExport} mechanism based on configuration\n * properties.\n * <p>\n * To enable auto export of annotation beans set {@code spring.jmx.enabled: true}.\n *\n * @author Christian Dupuis\n * @author Madhura Bhave\n * @author Artsiom Yudovin\n * @author Scott Frederick\n * @since 1.0.0\n */",
            "/**\n * 根据配置属性启用或禁用 Spring\n * {@link EnableMBeanExport @EnableMBeanExport} 机制的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 要启用注解 Bean 的自动导出，请设置 {@code spring.jmx.enabled: true}。\n *\n * @author Christian Dupuis\n * @author Madhura Bhave\n * @author Artsiom Yudovin\n * @author Scott Frederick\n * @since 1.0.0\n */",
        ),
    ],
    "JmxProperties.java": [
        (
            "/**\n * Configuration properties for JMX.\n *\n * @author Scott Frederick\n * @since 2.7.0\n */",
            "/**\n * JMX 的配置属性。\n *\n * @author Scott Frederick\n * @since 2.7.0\n */",
        ),
        (
            "/**\n\t * Expose Spring's management beans to the JMX domain.\n\t */",
            "/**\n\t * 将 Spring 的管理 Bean 暴露到 JMX 域。\n\t */",
        ),
        (
            "/**\n\t * Whether unique runtime object names should be ensured.\n\t */",
            "/**\n\t * 是否应确保运行时对象名称唯一。\n\t */",
        ),
        (
            "/**\n\t * MBeanServer bean name.\n\t */",
            "/**\n\t * MBeanServer Bean 名称。\n\t */",
        ),
        (
            "/**\n\t * JMX domain name.\n\t */",
            "/**\n\t * JMX 域名。\n\t */",
        ),
        (
            "/**\n\t * JMX Registration policy.\n\t */",
            "/**\n\t * JMX 注册策略。\n\t */",
        ),
    ],
    "ParentAwareNamingStrategy.java": [
        (
            "/**\n * Extension of {@link MetadataNamingStrategy} that supports a parent\n * {@link ApplicationContext}.\n *\n * @author Dave Syer\n * @since 1.1.1\n */",
            "/**\n * 支持父级 {@link ApplicationContext} 的 {@link MetadataNamingStrategy} 扩展。\n *\n * @author Dave Syer\n * @since 1.1.1\n */",
        ),
        (
            "/**\n\t * Set if unique runtime object names should be ensured.\n\t * @param ensureUniqueRuntimeObjectNames {@code true} if unique names should be\n\t * ensured.\n\t */",
            "/**\n\t * 设置是否应确保运行时对象名称唯一。\n\t * @param ensureUniqueRuntimeObjectNames 若为 {@code true} 则确保名称唯一\n\t */",
        ),
    ],
    "ConditionEvaluationReportLogger.java": [
        (
            "/**\n * Logs the {@link ConditionEvaluationReport}.\n *\n * @author Greg Turnquist\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n */",
            "/**\n * 记录 {@link ConditionEvaluationReport}。\n *\n * @author Greg Turnquist\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n */",
        ),
    ],
    "ConditionEvaluationReportLoggingListener.java": [
        (
            "/**\n * {@link ApplicationContextInitializer} that writes the {@link ConditionEvaluationReport}\n * to the log. Reports are logged at the {@link LogLevel#DEBUG DEBUG} level. A crash\n * report triggers an info output suggesting the user runs again with debug enabled to\n * display the report.\n * <p>\n * This initializer is not intended to be shared across multiple application context\n * instances.\n *\n * @author Greg Turnquist\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @since 2.0.0\n */",
            "/**\n * 将 {@link ConditionEvaluationReport} 写入日志的 {@link ApplicationContextInitializer}。\n * 报告以 {@link LogLevel#DEBUG DEBUG} 级别记录。崩溃时会输出 info 提示，\n * 建议用户启用 debug 后重新运行以查看报告。\n * <p>\n * 该初始化器不应在多个应用上下文实例间共享。\n *\n * @author Greg Turnquist\n * @author Dave Syer\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Madhura Bhave\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Static factory method that creates a\n\t * {@link ConditionEvaluationReportLoggingListener} which logs the report at the\n\t * specified log level.\n\t * @param logLevelForReport the log level to log the report at\n\t * @return a {@link ConditionEvaluationReportLoggingListener} instance.\n\t * @since 3.0.0\n\t */",
            "/**\n\t * 创建在指定日志级别记录报告的 {@link ConditionEvaluationReportLoggingListener} 的静态工厂方法。\n\t * @param logLevelForReport 记录报告所用的日志级别\n\t * @return {@link ConditionEvaluationReportLoggingListener} 实例\n\t * @since 3.0.0\n\t */",
        ),
    ],
    "ConditionEvaluationReportLoggingProcessor.java": [
        (
            "/**\n * {@link BeanFactoryInitializationAotProcessor} that logs the\n * {@link ConditionEvaluationReport} during ahead-of-time processing.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 在提前编译（AOT）处理期间记录 {@link ConditionEvaluationReport} 的\n * {@link BeanFactoryInitializationAotProcessor}。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "ConditionEvaluationReportMessage.java": [
        (
            "/**\n * A condition evaluation report message that can logged or printed.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
            "/**\n * 可记录或打印的条件评估报告消息。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.4.0\n */",
        ),
    ],
    "BackgroundPreinitializer.java": [
        (
            "/**\n * Interface used to preinitialize in the background code that may otherwise cause a delay\n * when first called. Implementations should be registered in {@code spring.factories}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
            "/**\n * 用于在后台预初始化代码的接口；这些代码若首次调用时初始化可能导致延迟。\n * 实现类应在 {@code spring.factories} 中注册。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
        ),
        (
            "/**\n\t * Perform any require preinitialization.\n\t * @throws Exception on any initialization error\n\t */",
            "/**\n\t * 执行所需的预初始化。\n\t * @throws Exception 初始化出错时\n\t */",
        ),
    ],
    "BackgroundPreinitializingApplicationListener.java": [
        (
            "/**\n * {@link ApplicationListener} to trigger early initialization in a background thread of\n * time-consuming tasks.\n * <p>\n * Set the {@link #IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME} system property to\n * {@code true} to disable this mechanism.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Artsiom Yudovin\n * @author Sebastien Deleuze\n * @see BackgroundPreinitializer\n */",
            "/**\n * 在后台线程中触发耗时任务早期初始化的 {@link ApplicationListener}。\n * <p>\n * 将 {@link #IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME} 系统属性设为 {@code true} 可禁用此机制。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @author Artsiom Yudovin\n * @author Sebastien Deleuze\n * @see BackgroundPreinitializer\n */",
        ),
        (
            "/**\n\t * System property that instructs Spring Boot how to run pre initialization. When the\n\t * property is set to {@code true}, no pre-initialization happens and each item is\n\t * initialized in the foreground as it needs to. When the property is {@code false}\n\t * (default), pre initialization runs in a separate thread in the background.\n\t */",
            "/**\n\t * 指示 Spring Boot 如何运行预初始化的系统属性。设为 {@code true} 时不进行预初始化，\n\t * 各项在需要时于前台初始化；为 {@code false}（默认）时，预初始化在后台独立线程中运行。\n\t */",
        ),
        (
            "/**\n\t * Runner thread to call the {@link BackgroundPreinitializer} instances.\n\t *\n\t * @param preinitializers the preinitializers\n\t */",
            "/**\n\t * 调用 {@link BackgroundPreinitializer} 实例的运行器线程。\n\t *\n\t * @param preinitializers 预初始化器列表\n\t */",
        ),
    ],
    "CharsetsBackgroundPreinitializer.java": [
        (
            "/**\n * {@link BackgroundPreinitializer} for commonly used charsets.\n *\n * @author Phillip Webb\n */",
            "/**\n * 常用字符集的 {@link BackgroundPreinitializer}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ConversionServiceBackgroundPreinitializer.java": [
        (
            "/**\n * {@link BackgroundPreinitializer} for Spring's {@link ConversionService}.\n *\n * @author Phillip Webb\n */",
            "/**\n * Spring {@link ConversionService} 的 {@link BackgroundPreinitializer}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ZoneIdBackgroundPreinitializer.java": [
        (
            "/**\n * {@link BackgroundPreinitializer} for {@link ZoneId}.\n *\n * @author Phillip Webb\n */",
            "/**\n * {@link ZoneId} 的 {@link BackgroundPreinitializer}。\n *\n * @author Phillip Webb\n */",
        ),
    ],
    "ConnectionDetails.java": [
        (
            "/**\n * Base interface for types that provide the details required to establish a connection to\n * a remote service.\n * <p>\n * Implementation classes can also implement {@link OriginProvider} in order to provide\n * origin information.\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 提供建立远程服务连接所需详情的类型的基接口。\n * <p>\n * 实现类还可实现 {@link OriginProvider} 以提供来源信息。\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
    ],
    "ConnectionDetailsFactories.java": [
        (
            "/**\n * A registry of {@link ConnectionDetailsFactory} instances.\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Pedro Xavier Leite Cavadas\n * @since 3.1.0\n */",
            "/**\n * {@link ConnectionDetailsFactory} 实例的注册表。\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @author Pedro Xavier Leite Cavadas\n * @since 3.1.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConnectionDetailsFactories} instance.\n\t * @param classLoader the class loader used to load factories\n\t * @since 3.5.0\n\t */",
            "/**\n\t * 创建新的 {@link ConnectionDetailsFactories} 实例。\n\t * @param classLoader 用于加载工厂的类加载器\n\t * @since 3.5.0\n\t */",
        ),
        (
            "/**\n\t * Return a {@link Map} of {@link ConnectionDetails} interface type to\n\t * {@link ConnectionDetails} instance created from the factories associated with the\n\t * given source.\n\t * @param <S> the source type\n\t * @param source the source\n\t * @param required if a connection details result is required\n\t * @return a map of {@link ConnectionDetails} instances\n\t * @throws ConnectionDetailsFactoryNotFoundException if a result is required but no\n\t * connection details factory is registered for the source\n\t * @throws ConnectionDetailsNotFoundException if a result is required but no\n\t * connection details instance was created from a registered factory\n\t */",
            "/**\n\t * 返回由与给定源关联的工厂创建的 {@link ConnectionDetails} 接口类型到\n\t * {@link ConnectionDetails} 实例的 {@link Map}。\n\t * @param <S> 源类型\n\t * @param source 源对象\n\t * @param required 是否必须得到连接详情结果\n\t * @return {@link ConnectionDetails} 实例映射\n\t * @throws ConnectionDetailsFactoryNotFoundException 需要结果但未为该源注册连接详情工厂\n\t * @throws ConnectionDetailsNotFoundException 需要结果但已注册工厂未创建连接详情实例\n\t */",
        ),
        (
            "/**\n\t * A {@link ConnectionDetailsFactory} registration.\n\t *\n\t * @param <S> the source type\n\t * @param <D> the connection details type\n\t * @param sourceType the source type\n\t * @param connectionDetailsType the connection details type\n\t * @param factory the factory\n\t */",
            "/**\n\t * {@link ConnectionDetailsFactory} 注册项。\n\t *\n\t * @param <S> 源类型\n\t * @param <D> 连接详情类型\n\t * @param sourceType 源类型\n\t * @param connectionDetailsType 连接详情类型\n\t * @param factory 工厂\n\t */",
        ),
    ],
    "ConnectionDetailsFactory.java": [
        (
            "/**\n * A factory to create {@link ConnectionDetails} from a given {@code source}.\n * Implementations should be registered in {@code META-INF/spring.factories}.\n *\n * @param <S> the source type accepted by the factory. Implementations are expected to\n * provide a valid {@code toString}.\n * @param <D> the type of {@link ConnectionDetails} produced by the factory\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 从给定 {@code source} 创建 {@link ConnectionDetails} 的工厂。\n * 实现类应在 {@code META-INF/spring.factories} 中注册。\n *\n * @param <S> 工厂接受的源类型；实现类应提供有效的 {@code toString}\n * @param <D> 工厂产生的 {@link ConnectionDetails} 类型\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
        (
            "/**\n\t * Get the {@link ConnectionDetails} from the given {@code source}. May return\n\t * {@code null} if no details can be created.\n\t * @param source the source\n\t * @return the connection details or {@code null}\n\t */",
            "/**\n\t * 从给定 {@code source} 获取 {@link ConnectionDetails}。若无法创建详情则可能返回 {@code null}。\n\t * @param source 源对象\n\t * @return 连接详情，或 {@code null}\n\t */",
        ),
    ],
    "ConnectionDetailsFactoryNotFoundException.java": [
        (
            "/**\n * {@link RuntimeException} thrown when a {@link ConnectionDetailsFactory} could not be\n * found.\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 找不到 {@link ConnectionDetailsFactory} 时抛出的 {@link RuntimeException}。\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
    ],
    "ConnectionDetailsNotFoundException.java": [
        (
            "/**\n * {@link RuntimeException} thrown when required {@link ConnectionDetails} could not be\n * found.\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
            "/**\n * 找不到所需的 {@link ConnectionDetails} 时抛出的 {@link RuntimeException}。\n *\n * @author Moritz Halbritter\n * @author Andy Wilkinson\n * @author Phillip Webb\n * @since 3.1.0\n */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        dst = ANALYZED / rel
        if not dst.exists():
            failures.append(f"MISSING analyzed: {rel}")
            continue
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
