#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-23a block [0:15] (dashboard sidebar/directives/filters/libs/services)."""
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
SCRIPT_NAME = "annotate_sentinel_wave23a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w23a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave23a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/SystemController.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/directives/sidebar/sidebar-search/sidebar-search.js"] = [
    (
        "/**\n * @ngdoc directive\n * @name izzyposWebApp.directive:adminPosHeader\n * @description\n * # adminPosHeader\n */",
        "/**\n * @ngdoc directive\n * @name izzyposWebApp.directive:adminPosHeader\n * @description\n * 侧边栏搜索区域指令，绑定 sidebar-search 模板。\n */",
    ),
    (
        "      controller: function ($scope) {\n        $scope.selectedMenu = 'home';",
        "      /** 初始化当前选中菜单项（默认 home）。 */\n      controller: function ($scope) {\n        $scope.selectedMenu = 'home';",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/directives/sidebar/sidebar.js"] = [
    (
        "angular.module('sentinelDashboardApp')\n  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {",
        "/**\n * 侧边栏指令：展示应用列表、健康机器数与网关标识，支持折叠切换。\n */\nangular.module('sentinelDashboardApp')\n  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {",
    ),
    (
        "        // app\n        AppService.getApps().success(",
        "        // 拉取应用列表并标记当前路由对应的应用为 active\n        AppService.getApps().success(",
    ),
    (
        "                // Handle appType\n                item.isGateway = item.appType === 1 || item.appType === 11 || item.appType === 12;",
        "                // 根据 appType 判断是否为网关应用（1/11/12）\n                item.isGateway = item.appType === 1 || item.appType === 11 || item.appType === 12;",
    ),
    (
        "        // toggle side bar\n        $scope.click = function ($event) {",
        "        // 点击应用条目时展开当前项并折叠其余项\n        $scope.click = function ($event) {",
    ),
    (
        "          entry.active = !entry.active;// toggle this clicked app bar",
        "          entry.active = !entry.active;  // 切换当前应用条目的展开状态",
    ),
    (
        "          $scope.apps.forEach(function (item) { // collapse other app bars",
        "          $scope.apps.forEach(function (item) {  // 折叠其他应用条目",
    ),
    (
        "        /**\n         * @deprecated\n         */\n        $scope.addSearchApp = function () {",
        "        /**\n         * 手动添加搜索应用（已废弃）。\n         * @deprecated\n         */\n        $scope.addSearchApp = function () {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/filters/filters.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** Angular 过滤器模块：提供分页等辅助过滤器。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "app.filter('range', [function () {\n  return function (input, length) {",
        "/** 生成 1..length 的整数数组，供 ng-repeat 分页使用。 */\napp.filter('range', [function () {\n  return function (input, length) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/libs/treeTable.js"] = [
    (
        "var com_github_culmat_jsTreeTable =  (function(){",
        "/**\n * jsTreeTable 1.0：将树形数据渲染为可展开/折叠的 HTML 表格。\n * 支持 depthFirst、makeTree、renderTree 与 expandLevel 控制。\n */\nvar com_github_culmat_jsTreeTable =  (function(){",
    ),
    (
        "\tfunction depthFirst(tree, func, childrenAttr) {",
        "\t/** 深度优先遍历树，对每个节点执行 func 回调。 */\n\tfunction depthFirst(tree, func, childrenAttr) {",
    ),
    (
        "\t/*\n\t * make a deep copy of the object\n\t */",
        "\t/** 通过 JSON 序列化深拷贝对象。 */",
    ),
    (
        "\tfunction makeTree (data, idAttr, refAttr, childrenAttr) {",
        "\t/** 将扁平 id/parent 列表组装为嵌套 children 树结构。 */\n\tfunction makeTree (data, idAttr, refAttr, childrenAttr) {",
    ),
    (
        "\tfunction renderTree(tree, childrenAttr, idAttr, attrs, renderer, tableAttributes) {",
        "\t/** 将树渲染为带 data-tt-* 属性的 HTML table 行。 */\n\tfunction renderTree(tree, childrenAttr, idAttr, attrs, renderer, tableAttributes) {",
    ),
    (
        "\tfunction treeTable(table){",
        "\t/** 为已渲染表格绑定展开/折叠交互与层级缩进。 */\n\tfunction treeTable(table){",
    ),
    (
        "\tfunction appendTreetable(tree, options) {",
        "\t/** 渲染树表并挂载到 mountPoint，可选 slider 控制展开层级。 */\n\tfunction appendTreetable(tree, options) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/appservice.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 应用列表 HTTP 服务：拉取 Dashboard 已注册应用摘要。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.getApps = function () {",
        "  /** GET briefinfos.json 获取所有应用及其机器健康概况。 */\n  this.getApps = function () {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/auth_service.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 登录认证 HTTP 服务：校验会话、登录与登出。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.check = function () {",
        "  /** POST /auth/check 校验当前 Session 是否有效。 */\n  this.check = function () {",
    ),
    (
        "  this.login = function (param) {",
        "  /** POST /auth/login 提交用户名密码登录。 */\n  this.login = function (param) {",
    ),
    (
        "  this.logout = function () {",
        "  /** POST /auth/logout 清除服务端 Session。 */\n  this.logout = function () {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/authority_service.js"] = [
    (
        "/**\n * Authority rule service.\n */",
        "/**\n * 授权规则 HTTP 服务：按机器查询及 CRUD 黑白名单规则。\n */",
    ),
    (
        "    this.queryMachineRules = function(app, ip, port) {",
        "    /** GET /authority/rules 拉取指定机器的授权规则列表。 */\n    this.queryMachineRules = function(app, ip, port) {",
    ),
    (
        "    this.addNewRule = function(rule) {",
        "    /** POST /authority/rule 新增授权规则。 */\n    this.addNewRule = function(rule) {",
    ),
    (
        "    this.saveRule = function (entity) {",
        "    /** PUT /authority/rule/{id} 更新已有授权规则。 */\n    this.saveRule = function (entity) {",
    ),
    (
        "    this.deleteRule = function (entity) {",
        "    /** DELETE /authority/rule/{id} 删除授权规则。 */\n    this.deleteRule = function (entity) {",
    ),
    (
        "    this.checkRuleValid = function checkRuleValid(rule) {",
        "    /** 校验资源名、限流应用与黑白名单策略是否完整。 */\n    this.checkRuleValid = function checkRuleValid(rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/cluster_state_service.js"] = [
    (
        "/**\n * Cluster state control service.\n *\n * @author Eric Zhao\n */",
        "/**\n * 集群限流状态 HTTP 服务：查询 Token Server/Client 状态及分配操作。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    this.fetchClusterUniversalStateSingle = function(app, ip, port) {",
        "    /** GET /cluster/state_single 拉取单台机器的集群通用状态。 */\n    this.fetchClusterUniversalStateSingle = function(app, ip, port) {",
    ),
    (
        "    this.fetchClusterUniversalStateOfApp = function(app) {",
        "    /** GET /cluster/state/{app} 拉取应用下所有机器的集群通用状态。 */\n    this.fetchClusterUniversalStateOfApp = function(app) {",
    ),
    (
        "    this.fetchClusterServerStateOfApp = function(app) {",
        "    /** GET /cluster/server_state/{app} 拉取应用维度 Token Server 状态。 */\n    this.fetchClusterServerStateOfApp = function(app) {",
    ),
    (
        "    this.fetchClusterClientStateOfApp = function(app) {",
        "    /** GET /cluster/client_state/{app} 拉取应用维度 Token Client 状态。 */\n    this.fetchClusterClientStateOfApp = function(app) {",
    ),
    (
        "    this.modifyClusterConfig = function(config) {",
        "    /** POST /cluster/config/modify_single 修改单台机器集群配置。 */\n    this.modifyClusterConfig = function(config) {",
    ),
    (
        "    this.applyClusterFullAssignOfApp = function(app, clusterMap) {",
        "    /** POST /cluster/assign/all_server/{app} 一键应用完整集群分配方案。 */\n    this.applyClusterFullAssignOfApp = function(app, clusterMap) {",
    ),
    (
        "    this.applyClusterSingleServerAssignOfApp = function(app, request) {",
        "    /** POST /cluster/assign/single_server/{app} 对单个 Token Server 执行分配。 */\n    this.applyClusterSingleServerAssignOfApp = function(app, request) {",
    ),
    (
        "    this.applyClusterServerBatchUnbind = function(app, machineSet) {",
        "    /** POST /cluster/assign/unbind_server/{app} 批量解绑 Token Server。 */\n    this.applyClusterServerBatchUnbind = function(app, machineSet) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/degrade_service.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 熔断降级规则 HTTP 服务：按机器查询及 CRUD 降级规则。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.queryMachineRules = function (app, ip, port) {",
        "  /** GET degrade/rules.json 拉取指定机器的熔断规则列表。 */\n  this.queryMachineRules = function (app, ip, port) {",
    ),
    (
        "  this.newRule = function (rule) {",
        "  /** POST /degrade/rule 新增熔断规则。 */\n  this.newRule = function (rule) {",
    ),
    (
        "  this.saveRule = function (rule) {",
        "  /** PUT /degrade/rule/{id} 更新已有熔断规则。 */\n  this.saveRule = function (rule) {",
    ),
    (
        "  this.deleteRule = function (rule) {",
        "  /** DELETE /degrade/rule/{id} 删除熔断规则。 */\n  this.deleteRule = function (rule) {",
    ),
    (
        "  this.checkRuleValid = function (rule) {",
        "  /** 校验资源名、降级策略、阈值、熔断时长与统计窗口等字段。 */\n  this.checkRuleValid = function (rule) {",
    ),
    (
        "      // 异常比率类型.",
        "      // 异常比率策略：count 须在 [0.0, 1.0] 范围内",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/flow_service_v1.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 流控规则 HTTP 服务（v1 API）：按机器查询及 CRUD 流控规则。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    this.queryMachineRules = function (app, ip, port) {",
        "    /** GET /v1/flow/rules 拉取指定机器的流控规则列表。 */\n    this.queryMachineRules = function (app, ip, port) {",
    ),
    (
        "    this.newRule = function (rule) {",
        "    /** POST /v1/flow/rule 新增流控规则。 */\n    this.newRule = function (rule) {",
    ),
    (
        "    this.saveRule = function (rule) {",
        "    /** PUT /v1/flow/save.json 更新已有流控规则。 */\n    this.saveRule = function (rule) {",
    ),
    (
        "    this.deleteRule = function (rule) {",
        "    /** DELETE /v1/flow/delete.json 删除流控规则。 */\n    this.deleteRule = function (rule) {",
    ),
    (
        "    function notNumberAtLeastZero(num) {",
        "    /** 判断数值是否未定义、非数字或小于 0。 */\n    function notNumberAtLeastZero(num) {",
    ),
    (
        "    function notNumberGreaterThanZero(num) {",
        "    /** 判断数值是否未定义、非数字或不大于 0。 */\n    function notNumberGreaterThanZero(num) {",
    ),
    (
        "    this.checkRuleValid = function (rule) {",
        "    /** 校验资源名、阈值、流控模式、关联资源、整形方式与集群配置。 */\n    this.checkRuleValid = function (rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/flow_service_v2.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 流控规则 HTTP 服务（v2 API）：RESTful 风格 CRUD，支持扩展集群字段。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    this.queryMachineRules = function (app, ip, port) {",
        "    /** GET /v2/flow/rules 拉取指定机器的流控规则列表。 */\n    this.queryMachineRules = function (app, ip, port) {",
    ),
    (
        "    this.newRule = function (rule) {",
        "    /** POST /v2/flow/rule 新增流控规则。 */\n    this.newRule = function (rule) {",
    ),
    (
        "    this.saveRule = function (rule) {",
        "    /** PUT /v2/flow/rule/{id} 更新已有流控规则。 */\n    this.saveRule = function (rule) {",
    ),
    (
        "    this.deleteRule = function (rule) {",
        "    /** DELETE /v2/flow/rule/{id} 删除流控规则。 */\n    this.deleteRule = function (rule) {",
    ),
    (
        "    function notNumberAtLeastZero(num) {",
        "    /** 判断数值是否未定义、非数字或小于 0。 */\n    function notNumberAtLeastZero(num) {",
    ),
    (
        "    function notNumberGreaterThanZero(num) {",
        "    /** 判断数值是否未定义、非数字或不大于 0。 */\n    function notNumberGreaterThanZero(num) {",
    ),
    (
        "    this.checkRuleValid = function (rule) {",
        "    /** 校验资源名、阈值、流控模式、关联资源、整形方式与集群配置。 */\n    this.checkRuleValid = function (rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/gateway/api_service.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 网关自定义 API 定义 HTTP 服务：管理 API 名称与匹配规则。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.queryApis = function (app, ip, port) {",
        "  /** GET /gateway/api/list.json 拉取网关 API 定义列表。 */\n  this.queryApis = function (app, ip, port) {",
    ),
    (
        "  this.newApi = function (api) {",
        "  /** POST /gateway/api/new.json 新增网关 API 定义。 */\n  this.newApi = function (api) {",
    ),
    (
        "  this.saveApi = function (api) {",
        "  /** POST /gateway/api/save.json 更新已有网关 API 定义。 */\n  this.saveApi = function (api) {",
    ),
    (
        "  this.deleteApi = function (api) {",
        "  /** POST /gateway/api/delete.json 删除网关 API 定义。 */\n  this.deleteApi = function (api) {",
    ),
    (
        "  this.checkApiValid = function (api, apiNames) {",
        "  /** 校验 API 名称、匹配规则非空且名称不重复。 */\n  this.checkApiValid = function (api, apiNames) {",
    ),
    (
        "      // Should never happen since no remove button will display when only one predicateItem.",
        "      // 仅剩一条匹配规则时不应出现此情况（界面不显示删除按钮）",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/gateway/flow_service.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 网关流控规则 HTTP 服务：按 API 维度管理 QPS/线程数限流。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.queryRules = function (app, ip, port) {",
        "  /** GET /gateway/flow/list.json 拉取网关流控规则列表。 */\n  this.queryRules = function (app, ip, port) {",
    ),
    (
        "  this.newRule = function (rule) {",
        "  /** POST /gateway/flow/new.json 新增网关流控规则。 */\n  this.newRule = function (rule) {",
    ),
    (
        "  this.saveRule = function (rule) {",
        "  /** POST /gateway/flow/save.json 更新已有网关流控规则。 */\n  this.saveRule = function (rule) {",
    ),
    (
        "  this.deleteRule = function (rule) {",
        "  /** POST /gateway/flow/delete.json 删除网关流控规则。 */\n  this.deleteRule = function (rule) {",
    ),
    (
        "  this.checkRuleValid = function (rule) {",
        "  /** 校验 API 名称、参数属性匹配串与 QPS/线程数阈值。 */\n  this.checkRuleValid = function (rule) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/identityservice.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 资源树 HTTP 服务：拉取机器上的调用链路资源与集群节点。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "  this.fetchIdentityOfMachine = function (ip, port, searchKey) {",
        "  /** GET machineResource.json 拉取机器普通资源树（可按 searchKey 过滤）。 */\n  this.fetchIdentityOfMachine = function (ip, port, searchKey) {",
    ),
    (
        "  this.fetchClusterNodeOfMachine = function (ip, port, searchKey) {",
        "  /** GET machineResource.json?type=cluster 拉取机器集群节点资源树。 */\n  this.fetchClusterNodeOfMachine = function (ip, port, searchKey) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/services/machineservice.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 机器管理 HTTP 服务：查询应用机器列表与移除机器。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    this.getAppMachines = function (app) {",
        "    /** GET app/{app}/machines.json 拉取应用下已注册机器列表。 */\n    this.getAppMachines = function (app) {",
    ),
    (
        "    this.removeAppMachine = function (app, ip, port) {",
        "    /** POST app/{app}/machine/remove.json 从 Dashboard 移除指定机器。 */\n    this.removeAppMachine = function (app, ip, port) {",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise SystemExit(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    src_text = src.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" in src_text and "Licensed under the Apache License" not in text:
        raise SystemExit(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


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
    index_file = Path("/tmp/git-index-sentinel-w23a")
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
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 23a [0:15]",
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
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave23a done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
