#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-23b block [15:30] (dashboard services/gulp/vendor JS + annotation demos)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w23b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave23b_batch15_30.py"
MARK_NOTE = "wave23b"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemoryRuleRepositoryAdapter.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/metricservice.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 实时监控指标 Angular 服务：封装 metric 查询 REST 接口。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "app.service('MetricService', ['$http', function ($http) {",
        "/** MetricService：按应用/资源/机器拉取 pass、block 等时序指标。 */\napp.service('MetricService', ['$http', function ($http) {",
    ),
    (
        "  this.queryAppSortedIdentities = function (params) {",
        "  /** 查询应用下各资源的排序 metric（Top N），供实时监控页列表展示。 */\n  this.queryAppSortedIdentities = function (params) {",
    ),
    (
        "  this.queryByAppAndIdentity = function (params) {",
        "  /** 按应用名与资源名查询 metric 时序数据。 */\n  this.queryByAppAndIdentity = function (params) {",
    ),
    (
        "  this.queryByMachineAndIdentity = function (ip, port, identity, startTime, endTime) {",
        "  /** 按机器 IP/端口、资源名与时间窗口查询 metric（start/end 转为毫秒时间戳）。 */\n  this.queryByMachineAndIdentity = function (ip, port, identity, startTime, endTime) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/param_flow_service.js"] = [
    (
        "/**\n * Parameter flow control service.\n * \n * @author Eric Zhao\n */",
        "/**\n * 热点参数限流 Angular 服务：规则 CRUD 及前端表单校验。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "  this.queryMachineRules = function(app, ip, port) {",
        "  /** 拉取指定机器上的 ParamFlow 规则列表。 */\n  this.queryMachineRules = function(app, ip, port) {",
    ),
    (
        "  this.addNewRule = function(rule) {",
        "  /** 新增热点参数限流规则。 */\n  this.addNewRule = function(rule) {",
    ),
    (
        "  this.saveRule = function (entity) {",
        "  /** 按 ID 更新热点参数限流规则。 */\n  this.saveRule = function (entity) {",
    ),
    (
        "  this.deleteRule = function (entity) {",
        "  /** 按 ID 删除热点参数限流规则。 */\n  this.deleteRule = function (entity) {",
    ),
    (
        "    function isNumberClass(classType) {",
        "    /** 判断参数类型是否为数值型（int/double/float/long/short）。 */\n    function isNumberClass(classType) {",
    ),
    (
        "    function isByteClass(classType) {",
        "    /** 判断参数类型是否为 byte。 */\n    function isByteClass(classType) {",
    ),
    (
        "    function notNumberAtLeastZero(num) {",
        "    /** 校验数值未定义、空或非负整数（用于例外项 count）。 */\n    function notNumberAtLeastZero(num) {",
    ),
    (
        "    function notGoodNumber(num) {",
        "    /** 校验数值未定义、空或 NaN（用于数值型参数 object）。 */\n    function notGoodNumber(num) {",
    ),
    (
        "    function notGoodNumberBetweenExclusive(num, l ,r) {",
        "    /** 校验数值是否在 (l, r) 开区间内（byte 参数范围 -128~127）。 */\n    function notGoodNumberBetweenExclusive(num, l ,r) {",
    ),
    (
        "    function notValidParamItem(curExItem) {",
        "    /** 校验单条热点参数例外项：object、classType 与 count 是否合法。 */\n    function notValidParamItem(curExItem) {",
    ),
    (
        "  this.checkRuleValid = function (rule) {",
        "  /** 提交前校验规则：资源名、限流模式、阈值、参数索引及例外项列表。 */\n  this.checkRuleValid = function (rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/systemservice.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 系统保护规则 Angular 服务：LOAD/RT/线程数/QPS/CPU 阈值管理。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.queryMachineRules = function (app, ip, port) {",
        "  /** 拉取指定机器上的系统保护规则列表。 */\n  this.queryMachineRules = function (app, ip, port) {",
    ),
    (
        "  this.newRule = function (rule) {",
        "  /** 新增系统保护规则，按 grade 映射 highestSystemLoad/avgRt 等字段。 */\n  this.newRule = function (rule) {",
    ),
    (
        "    if (rule.grade == 0) {// avgLoad",
        "    if (rule.grade == 0) {// 系统负载（avgLoad）",
    ),
    (
        "    } else if (rule.grade == 1) {// avgRt",
        "    } else if (rule.grade == 1) {// 平均 RT（avgRt）",
    ),
    (
        "    } else if (rule.grade == 2) {// maxThread",
        "    } else if (rule.grade == 2) {// 最大线程数（maxThread）",
    ),
    (
        "    } else if (rule.grade == 3) {// qps",
        "    } else if (rule.grade == 3) {// 入口 QPS（qps）",
    ),
    (
        "    } else if (rule.grade == 4) {// cpu",
        "    } else if (rule.grade == 4) {// CPU 使用率（cpu）",
    ),
    (
        "  this.saveRule = function (rule) {",
        "  /** 按 ID 保存系统保护规则修改。 */\n  this.saveRule = function (rule) {",
    ),
    (
        "  this.deleteRule = function (rule) {",
        "  /** 按 ID 与应用名删除系统保护规则。 */\n  this.deleteRule = function (rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/version_service.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** Dashboard 版本信息 Angular 服务。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.version = function () {",
        "  /** 请求后端 /version 接口，返回 Dashboard 版本号。 */\n  this.version = function () {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/gulpfile.js"] = [
    (
        "const app = {\n  srcPath: 'app/', // 源代码\n  devPath: 'tmp/', // 开发打包\n  prdPath: 'dist/' // 生产打包\n};",
        "/** 路径配置：源码、开发临时目录与生产输出目录。 */\nconst app = {\n  srcPath: 'app/', // 源代码\n  devPath: 'tmp/', // 开发打包\n  prdPath: 'dist/' // 生产打包\n};",
    ),
    (
        "const JS_LIBS = [",
        "/** 第三方 JS 依赖列表，合并为 app.vendor.js。 */\nconst JS_LIBS = [",
    ),
    (
        "const CSS_APP = [",
        "/** 应用 CSS 与第三方样式列表，合并为 app.css。 */\nconst CSS_APP = [",
    ),
    (
        "const JS_APP = [",
        "/** 业务 JS 模块列表（服务/控制器等），合并为 app.js。 */\nconst JS_APP = [",
    ),
    (
        "gulp.task('lib', function () {",
        "/** lib 任务：合并并压缩 vendor JS 到 tmp 与 dist。 */\ngulp.task('lib', function () {",
    ),
    (
        "gulp.task('css', function () {",
        "/** css 任务：合并并压缩样式到 tmp 与 dist。 */\ngulp.task('css', function () {",
    ),
    (
        "gulp.task('js', function () {",
        "/** js 任务：合并并压缩业务脚本到 tmp 与 dist。 */\ngulp.task('js', function () {",
    ),
    (
        "gulp.task('jshint', function () {",
        "/** jshint 任务：对 JS_APP 做静态语法检查。 */\ngulp.task('jshint', function () {",
    ),
    (
        "gulp.task('clean', function () {",
        "/** clean 任务：发布前清空 tmp 与 dist，避免旧文件残留。 */\ngulp.task('clean', function () {",
    ),
    (
        "gulp.task('build', ['clean', 'jshint', 'lib', 'js', 'css']);",
        "/** build 总任务：清理 → 检查 → 打包 lib/js/css。 */\ngulp.task('build', ['clean', 'jshint', 'lib', 'js', 'css']);",
    ),
    (
        "gulp.task('serve', ['build'], function () {",
        "/** serve 任务：build 后启动 connect 静态服务并监听文件变更。 */\ngulp.task('serve', ['build'], function () {",
    ),
    (
        "gulp.task('default', ['serve']);",
        "/** 默认任务：启动开发服务器。 */\ngulp.task('default', ['serve']);",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/lib/js/angular.min.js"] = [
    (
        "/*\n AngularJS v1.4.8\n (c) 2010-2015 Google, Inc. http://angularjs.org\n License: MIT\n*/",
        "/*\n AngularJS v1.4.8\n (c) 2010-2015 Google, Inc. http://angularjs.org\n License: MIT\n\n 第三方 AngularJS 核心库压缩版，Dashboard 前端 MVC/路由/DI 框架依赖。\n 本体为 min 文件，不做逐行注解。\n*/",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/lib/js/bootstrap.min.js"] = [
    (
        "/*!\n * Bootstrap v3.0.3 (http://getbootstrap.com)\n * Copyright 2013 Twitter, Inc.\n * Licensed under http://www.apache.org/licenses/LICENSE-2.0\n */",
        "/*!\n * Bootstrap v3.0.3 (http://getbootstrap.com)\n * Copyright 2013 Twitter, Inc.\n * Licensed under http://www.apache.org/licenses/LICENSE-2.0\n *\n * 第三方 Bootstrap 3 UI 组件库压缩版，提供按钮、模态框、下拉等 JS 插件。\n * 本体为 min 文件，不做逐行注解。\n */",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/lib/js/g2.min.js"] = [
    (
        '!function(t,e){"object"==typeof exports&&"object"==typeof module?module.exports=e():',
        "/** AntV G2 图表库压缩版（v3），Dashboard 实时监控页 pass/block QPS 折线图渲染依赖。\n * 本体为第三方 min 文件，不做逐行注解。\n */\n!function(t,e){\"object\"==typeof exports&&\"object\"==typeof module?module.exports=e():",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/lib/js/jquery.min.js"] = [
    (
        "/*! jQuery v2.1.4 | (c) 2005, 2015 jQuery Foundation, Inc. | jquery.org/license */",
        "/*! jQuery v2.1.4 | (c) 2005, 2015 jQuery Foundation, Inc. | jquery.org/license\n * 第三方 jQuery DOM/Ajax 库压缩版，Dashboard 与 Bootstrap 插件的基础依赖。\n * 本体为 min 文件，不做逐行注解。\n */",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/demo/annotation/cdi/interceptor/DemoApplication.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * CDI 拦截器注解演示入口：启动 SE 容器并调用 {@link TestService} 各方法。\n *\n * @author sea\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 初始化 CDI 容器，依次演示限流、降级与默认 fallback 行为。 */\n    public static void main(String[] args) {",
    ),
    (
        "        SeContainerInitializer containerInit = SeContainerInitializer.newInstance();",
        "        // 创建并启动 CDI SE 容器\n        SeContainerInitializer containerInit = SeContainerInitializer.newInstance();",
    ),
    (
        "        TestService testService = container.select(TestService.class).get();",
        "        // 从容器获取 TestService 代理实例\n        TestService testService = container.select(TestService.class).get();",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/demo/annotation/cdi/interceptor/ExceptionUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * CDI 演示用 BlockException 处理工具类，供 {@code @SentinelResourceBinding} 的 blockHandler 引用。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Handler method that handles BlockException when blocked.\n        // The method parameter list should match original method, with the last additional\n        // parameter with type BlockException. The return type should be same as the original method.\n        // The block handler method should be located in the same class with original method by default.\n        // If you want to use method in other classes, you can set the blockHandlerClass\n        // with corresponding Class (Note the method in other classes must be static).",
        "        // 被限流时调用的 blockHandler：参数列表与原方法一致，末尾追加 BlockException；\n        // 返回类型须与原方法相同；默认须与原方法同类，跨类引用时需 blockHandlerClass 且方法为 static。",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/demo/annotation/cdi/interceptor/TestService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * CDI 演示服务接口，定义带 Sentinel 注解绑定的测试方法。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    void test();",
        "    /** 无参测试方法，演示 blockHandler 限流回调。 */\n    void test();",
    ),
    (
        "    String hello(long s);",
        "    /** 带 long 参数，演示 fallback 降级。 */\n    String hello(long s);",
    ),
    (
        "    String helloAnother(String name);",
        "    /** 带 String 参数，演示 defaultFallback 与 exceptionsToIgnore。 */\n    String helloAnother(String name);",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-cdi-interceptor/src/main/java/com/alibaba/csp/sentinel/demo/annotation/cdi/interceptor/TestServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link TestService} 实现：通过 {@link SentinelResourceBinding} 绑定资源名与降级策略。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @SentinelResourceBinding(value = \"test\", blockHandler = \"handleException\", blockHandlerClass = {ExceptionUtil.class})",
        "    /** 资源 test：限流时调用 ExceptionUtil.handleException。 */\n    @SentinelResourceBinding(value = \"test\", blockHandler = \"handleException\", blockHandlerClass = {ExceptionUtil.class})",
    ),
    (
        "    @SentinelResourceBinding(value = \"hello\", fallback = \"helloFallback\")",
        "    /** 资源 hello：异常时走 helloFallback 方法降级。 */\n    @SentinelResourceBinding(value = \"hello\", fallback = \"helloFallback\")",
    ),
    (
        "    @SentinelResourceBinding(value = \"helloAnother\", defaultFallback = \"defaultFallback\",\n        exceptionsToIgnore = {IllegalStateException.class})",
        "    /** 资源 helloAnother：除 IllegalStateException 外均走 defaultFallback。 */\n    @SentinelResourceBinding(value = \"helloAnother\", defaultFallback = \"defaultFallback\",\n        exceptionsToIgnore = {IllegalStateException.class})",
    ),
    (
        "    public String helloFallback(long s, Throwable ex) {",
        "    /** fallback 方法：参数为原方法参数 + 捕获的 Throwable。 */\n    public String helloFallback(long s, Throwable ex) {",
    ),
    (
        "        // Do some log here.",
        "        // 可在此记录降级日志",
    ),
    (
        "    public String defaultFallback() {",
        "    /** 默认 fallback，无参数，返回固定降级文案。 */\n    public String defaultFallback() {",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/DemoApplication.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Spring AOP 注解适配演示入口，启动 Spring Boot 容器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static void main(String[] args) {",
        "    /** 启动 Spring Boot，加载 AOP 切面与 Sentinel 注解拦截。 */\n    public static void main(String[] args) {",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/config/AopConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Spring AOP 配置：注册 {@link SentinelResourceAspect} 以拦截 {@code @SentinelResource}。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public SentinelResourceAspect sentinelResourceAspect() {",
        "    /** 声明 Sentinel 资源切面 Bean，启用注解方式的流控/降级。 */\n    @Bean\n    public SentinelResourceAspect sentinelResourceAspect() {",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-sentinel-w23b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    orig = text
    for old, new in R.get(rel, []):
        if old not in text:
            raise ValueError(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise ValueError(f"No Chinese in {rel} after annotation")
    dst.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    if not batch_path.exists():
        return
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    if pending_path.exists():
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 23b [15:30]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave23b done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len([ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    pending_total = len([ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    chinese_confirmed = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese_confirmed,
                "all_15_chinese": all(chinese_confirmed.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
