#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-22b block [15:30] (dashboard AngularJS controllers/directives)."""
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
BATCH_LIST = Path("/tmp/sentinel_w22b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave22b_batch15_30.py"
MARK_NOTE = "wave22b"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemoryRuleRepositoryAdapter.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_single.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 单台机器集群限流配置页控制器：查询/修改客户端或服务端 Token 配置。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "        const UNSUPPORTED_CODE = 4041;",
        "        /** 客户端不支持集群限流时的 API 错误码。 */\n        const UNSUPPORTED_CODE = 4041;",
    ),
    (
        "        const CLUSTER_MODE_CLIENT = 0;\n        const CLUSTER_MODE_SERVER = 1;",
        "        /** 集群模式：0=客户端，1=服务端。 */\n        const CLUSTER_MODE_CLIENT = 0;\n        const CLUSTER_MODE_SERVER = 1;",
    ),
    (
        "        function convertSetToString(set) {",
        "        /** 将命名空间集合数组转为逗号分隔字符串，供表单展示。 */\n        function convertSetToString(set) {",
    ),
    (
        "        function convertStrToNamespaceSet(str) {",
        "        /** 将逗号分隔的命名空间字符串解析为去空白后的数组。 */\n        function convertStrToNamespaceSet(str) {",
    ),
    (
        "        function fetchMachineClusterState() {",
        "        /** 拉取当前选中机器的集群通用状态并填充 scope。 */\n        function fetchMachineClusterState() {",
    ),
    (
        "        function checkValidClientConfig(stateVO) {",
        "        /** 校验集群客户端配置：Token Server 地址、端口与请求超时。 */\n        function checkValidClientConfig(stateVO) {",
    ),
    (
        "        function sendClusterClientRequest(stateVO) {",
        "        /** 提交集群客户端模式配置修改请求。 */\n        function sendClusterClientRequest(stateVO) {",
    ),
    (
        "        function checkValidServerConfig(stateVO) {",
        "        /** 校验集群服务端配置：命名空间集合、端口与最大 QPS。 */\n        function checkValidServerConfig(stateVO) {",
    ),
    (
        "        function sendClusterServerRequest(stateVO) {",
        "        /** 提交集群服务端模式配置修改请求。 */\n        function sendClusterServerRequest(stateVO) {",
    ),
    (
        "        function queryAppMachines() {",
        "        /** 加载应用下健康机器列表，填充机器选择下拉框。 */\n        function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/degrade.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 熔断降级规则页控制器：按机器查询、增删改熔断规则。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    //初始化",
        "    // 初始化应用名与分页、机器选择器配置",
    ),
    (
        "    getMachineRules();\n    function getMachineRules() {",
        "    getMachineRules();\n    /** 按当前选中机器拉取熔断规则列表。 */\n    function getMachineRules() {",
    ),
    (
        "    function parseDegradeMode(grade) {",
        "    /** 将熔断策略 grade 转为中文描述（慢调用比例/异常比例/异常数）。 */\n    function parseDegradeMode(grade) {",
    ),
    (
        "    function deleteRule(rule) {",
        "    /** 调用服务删除规则并刷新列表。 */\n    function deleteRule(rule) {",
    ),
    (
        "    function addNewRule(rule) {",
        "    /** 新增熔断规则并关闭编辑对话框。 */\n    function addNewRule(rule) {",
    ),
    (
        "    function saveRule(rule, edit) {",
        "    /** 保存编辑后的熔断规则。 */\n    function saveRule(rule, edit) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载应用下健康机器供规则绑定。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/flow_v1.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 流控规则页控制器（v1 API）：单机/集群流控规则的 CRUD。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    $scope.generateThresholdTypeShow = (rule) => {",
        "    /** 根据 clusterMode 与 thresholdType 生成阈值类型展示文案。 */\n    $scope.generateThresholdTypeShow = (rule) => {",
    ),
    (
        "    getMachineRules();\n    function getMachineRules() {",
        "    getMachineRules();\n    /** 拉取当前机器的流控规则列表。 */\n    function getMachineRules() {",
    ),
    (
        "    function deleteRule(rule) {",
        "    /** 删除流控规则并刷新。 */\n    function deleteRule(rule) {",
    ),
    (
        "    function addNewRule(rule) {",
        "    /** 新增流控规则。 */\n    function addNewRule(rule) {",
    ),
    (
        "    function saveRule(rule, edit) {",
        "    /** 保存流控规则修改。 */\n    function saveRule(rule, edit) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/flow_v2.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 流控规则页控制器（v2 API）：支持 fallbackToLocalWhenFail 等扩展配置。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    $scope.generateThresholdTypeShow = (rule) => {",
        "    /** 根据 clusterMode 与 thresholdType 生成阈值类型展示文案。 */\n    $scope.generateThresholdTypeShow = (rule) => {",
    ),
    (
        "    getMachineRules();\n    function getMachineRules() {",
        "    getMachineRules();\n    /** 拉取当前机器的流控规则列表（v2 接口）。 */\n    function getMachineRules() {",
    ),
    (
        "    function deleteRule(rule) {",
        "    /** 删除流控规则并刷新。 */\n    function deleteRule(rule) {",
    ),
    (
        "    function addNewRule(rule) {",
        "    /** 新增流控规则（含集群降级本地兜底默认值）。 */\n    function addNewRule(rule) {",
    ),
    (
        "    function saveRule(rule, edit) {",
        "    /** 保存流控规则修改。 */\n    function saveRule(rule, edit) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/gateway/api.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 网关自定义 API 定义页控制器：管理 API 名称与匹配规则（predicateItems）。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    getApis();\n    function getApis() {",
        "    getApis();\n    /** 拉取网关 API 定义并按 predicateItem 展开为多行表格数据。 */\n    function getApis() {",
    ),
    (
        "            // To merge rows for api who has more than one predicateItems, here we build data manually",
        "            // 含多个 predicateItem 的 API 需手动展开行，以便表格 rowspan 合并",
    ),
    (
        "                // The itemSize indicates how many rows to merge, by using rowspan=\"{{api.itemSize}}\" in <td> tag",
        "                // itemSize 供模板 rowspan 合并同一 API 的多行",
    ),
    (
        "                // Mark the flag of first item to zero, indicates the start row to merge",
        "                // firstFlag=0 标记合并块的首行",
    ),
    (
        "                // Still hold the data of predicateItems, in order to bind data in edit dialog html",
        "                // 保留完整 predicateItems 供编辑对话框绑定",
    ),
    (
        "    function addNewApi(api) {",
        "    /** 新增自定义 API 定义。 */\n    function addNewApi(api) {",
    ),
    (
        "    function saveApi(api, edit) {",
        "    /** 保存 API 定义修改。 */\n    function saveApi(api, edit) {",
    ),
    (
        "    function deleteApi(api) {",
        "    /** 删除自定义 API 定义。 */\n    function deleteApi(api) {",
    ),
    (
        "        // Should never happen since no remove button will display when only one predicateItem.",
        "        // 仅剩一条匹配规则时不应触发（UI 已隐藏删除按钮）",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/gateway/flow.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 网关流控规则页控制器：按 Route ID 或自定义 API 配置限流。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    getMachineRules();\n    function getMachineRules() {",
        "    getMachineRules();\n    /** 拉取当前机器的网关流控规则。 */\n    function getMachineRules() {",
    ),
    (
        "    getApiNames();\n    function getApiNames() {",
        "    getApiNames();\n    /** 拉取自定义 API 名称列表，供规则资源选择。 */\n    function getApiNames() {",
    ),
    (
        "    $scope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];",
        "    /** 统计窗口单位选项（秒/分/时/天）。 */\n    $scope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];",
    ),
    (
        "    function addNewRule(rule) {",
        "    /** 新增网关流控规则。 */\n    function addNewRule(rule) {",
    ),
    (
        "    function saveRule(rule, edit) {",
        "    /** 保存网关流控规则修改。 */\n    function saveRule(rule, edit) {",
    ),
    (
        "    function deleteRule(rule) {",
        "    /** 删除网关流控规则。 */\n    function deleteRule(rule) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/gateway/identity.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 网关资源簇（Identity）页控制器：展示资源列表并快捷新增流控/降级规则。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    getApiNames();\n    function getApiNames() {",
        "    getApiNames();\n    /** 拉取自定义 API 名称，用于判断资源模式。 */\n    function getApiNames() {",
    ),
    (
        "    $scope.addNewGatewayFlowRule = function (resource) {",
        "    /** 为指定资源打开新增网关流控规则对话框（独立 scope）。 */\n    $scope.addNewGatewayFlowRule = function (resource) {",
    ),
    (
        "    function saveGatewayFlowRule() {",
        "    /** 保存网关流控规则并跳转至流控规则页。 */\n    function saveGatewayFlowRule() {",
    ),
    (
        "    function saveGatewayFlowRuleAndContinue() {",
        "    /** 保存规则后保持当前页，便于连续添加。 */\n    function saveGatewayFlowRuleAndContinue() {",
    ),
    (
        "    $scope.addNewDegradeRule = function (resource) {",
        "    /** 为指定资源打开新增降级规则对话框。 */\n    $scope.addNewDegradeRule = function (resource) {",
    ),
    (
        "    function saveDegradeRule() {",
        "    /** 保存降级规则并跳转至降级规则页。 */\n    function saveDegradeRule() {",
    ),
    (
        "    function saveDegradeRuleAndContinue() {",
        "    /** 保存降级规则后继续留在当前页。 */\n    function saveDegradeRuleAndContinue() {",
    ),
    (
        "    $scope.searchChange = function (searchKey) {",
        "    /** 防抖搜索：600ms 后按关键字刷新资源列表。 */\n    $scope.searchChange = function (searchKey) {",
    ),
    (
        "    // Fetch all machines by current app name.",
        "    // 按当前应用名加载全部机器",
    ),
    (
        "    function reInitIdentityDatas() {",
        "    /** 机器或搜索条件变化时重新拉取 API 名称与资源簇。 */\n    function reInitIdentityDatas() {",
    ),
    (
        "    function queryIdentities() {",
        "    /** 查询机器上的 ClusterNode 资源列表（支持关键字过滤）。 */\n    function queryIdentities() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/home.js"] = [
    (
        "/**\n * @ngdoc function\n * @name sentinelDashboardApp.controller:MainCtrl\n * @description\n * # MainCtrl\n * Controller of the sentinelDashboardApp\n */",
        "/**\n * @ngdoc function\n * @name sentinelDashboardApp.controller:HomeCtrl\n * @description\n * 控制台首页占位控制器，当前无业务逻辑。\n */",
    ),
    (
        "    // do noting",
        "    // 暂无实现",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/login.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 登录页控制器：校验凭据并写入 localStorage 会话。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    // If auth passed, jump to the index page directly",
        "    // 已有有效会话则直接跳转控制台首页",
    ),
    (
        "    $scope.login = function () {",
        "    /** 提交用户名密码，成功后缓存 session 并进入 dashboard。 */\n    $scope.login = function () {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/machine.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 机器管理页控制器：展示应用下客户端机器列表并支持移除。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    $scope.sortBy = function (propertyName) {",
        "    /** 按列名切换升序/降序排序。 */\n    $scope.sortBy = function (propertyName) {",
    ),
    (
        "    $scope.reloadMachines = function() {",
        "    /** 重新拉取机器列表并统计健康数量。 */\n    $scope.reloadMachines = function() {",
    ),
    (
        "    $scope.removeMachine = function(ip, port) {",
        "    /** 确认后从 Dashboard 移除指定机器注册信息。 */\n    $scope.removeMachine = function(ip, port) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/main.js"] = [
    (
        "/**\n * @ngdoc function\n * @name sentinelDashboardApp.controller:MainCtrl\n * @description\n * # MainCtrl\n * Controller of the sentinelDashboardApp\n */",
        "/**\n * @ngdoc function\n * @name sentinelDashboardApp.controller:DashboardCtrl\n * @description\n * 控制台主框架占位控制器，路由容器无额外逻辑。\n */",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/metric.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 实时监控页控制器：按资源展示 pass/block QPS 时序图表并定时刷新。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    function formatDate(date) {",
        "    /** 将 Date 格式化为 YYYY/MM/DD HH:mm:ss 供时间选择器展示。 */\n    function formatDate(date) {",
    ),
    (
        "    // 数据自动刷新频率",
        "    /** 指标数据自动刷新间隔（毫秒）。 */",
    ),
    (
        "    reInitIdentityDatas();\n    function reInitIdentityDatas() {",
        "    reInitIdentityDatas();\n    /** 启动/重置定时拉取任务。 */\n    function reInitIdentityDatas() {",
    ),
    (
        "    $scope.initAllChart = function () {",
        "    /** 销毁旧图表并按 metrics 数据用 G2 渲染 pass/block QPS 折线图。 */\n    $scope.initAllChart = function () {",
    ),
    (
        "      //revoke useless charts positively",
        "      // 主动销毁旧图表实例，避免内存泄漏",
    ),
    (
        "              textAlign: 'center', // 文本对齐方向，可取值为： start center end",
        "              textAlign: 'center', // 文本对齐：start / center / end",
    ),
    (
        "              fill: '#404040', // 文本的颜色",
        "              fill: '#404040', // 文本颜色",
    ),
    (
        "              fontSize: '11', // 文本大小",
        "              fontSize: '11', // 字体大小",
    ),
    (
        "              //textBaseline: 'top', // 文本基准线，可取 top middle bottom，默认为middle",
        "              // textBaseline: 'top'，基准线可选 top/middle/bottom",
    ),
    (
        "    function queryIdentityDatas() {",
        "    /** 分页查询应用下各资源的排序 metric 数据。 */\n    function queryIdentityDatas() {",
    ),
    (
        "          // push an empty element in the last, for ng-init reasons.",
        "          // 末尾追加空元素，配合 ng-init 触发图表渲染",
    ),
    (
        "    function fillZeros(metricData) {",
        "    /** 在缺失的时间戳处补零，保证折线图时间轴连续。 */\n    function fillZeros(metricData) {",
    ),
    (
        "    function lastOfArray(arr, n) {",
        "    /** 取数组末尾 n 个元素（倒序），用于缩略展示。 */\n    function lastOfArray(arr, n) {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/param_flow.js"] = [
    (
        "/**\n * Parameter flow control controller.\n * \n * @author Eric Zhao\n */",
        "/**\n * 热点参数限流页控制器：按机器管理 ParamFlow 规则及例外项列表。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    const UNSUPPORTED_CODE = 4041;",
        "    /** 客户端不支持热点参数限流时的错误码。 */\n    const UNSUPPORTED_CODE = 4041;",
    ),
    (
        "    $scope.paramItemClassTypeList = [",
        "    /** 热点参数允许的 Java 类型列表。 */\n    $scope.paramItemClassTypeList = [",
    ),
    (
        "      function updateSingleParamItem(arr, v, t, c) {",
        "      /** 更新或追加单条参数例外项（object + classType -> count）。 */\n      function updateSingleParamItem(arr, v, t, c) {",
    ),
    (
        "      function removeSingleParamItem(arr, v, t) {",
        "      /** 从例外项列表移除匹配的参数项。 */\n      function removeSingleParamItem(arr, v, t) {",
    ),
    (
        "      function isNumberClass(classType) {",
        "      /** 判断类型是否为数值型（int/double/float/long/short）。 */\n      function isNumberClass(classType) {",
    ),
    (
        "      function isByteClass(classType) {",
        "      /** 判断类型是否为 byte。 */\n      function isByteClass(classType) {",
    ),
    (
        "    function getMachineRules() {",
        "    /** 拉取当前机器的热点参数限流规则。 */\n    function getMachineRules() {",
    ),
    (
        "    function addNewRuleAndPush(rule) {",
        "    /** 新增规则并推送至客户端。 */\n    function addNewRuleAndPush(rule) {",
    ),
    (
        "    function saveRuleAndPush(rule, edit) {",
        "    /** 保存规则修改并推送。 */\n    function saveRuleAndPush(rule, edit) {",
    ),
    (
        "    function deleteRuleAndPush(entity) {",
        "    /** 按 ID 删除规则并推送。 */\n    function deleteRuleAndPush(entity) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/system.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/** 系统保护规则页控制器：LOAD/RT/线程数/QPS/CPU 等全局阈值管理。 */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "    //初始化",
        "    // 初始化应用名、分页与机器选择器",
    ),
    (
        "    getMachineRules();\n    function getMachineRules() {",
        "    getMachineRules();\n    /** 拉取系统保护规则并按有效阈值字段推断 grade 展示类型。 */\n    function getMachineRules() {",
    ),
    (
        "    function deleteRule(rule) {",
        "    /** 删除系统保护规则。 */\n    function deleteRule(rule) {",
    ),
    (
        "    function addNewRule(rule) {",
        "    /** 新增系统保护规则（CPU 模式校验 [0,1] 区间）。 */\n    function addNewRule(rule) {",
    ),
    (
        "    function saveRule(rule, edit) {",
        "    /** 保存系统保护规则修改。 */\n    function saveRule(rule, edit) {",
    ),
    (
        "    function queryAppMachines() {",
        "    /** 加载健康机器列表。 */\n    function queryAppMachines() {",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/directives/header/header.js"] = [
    (
        "/**\n * @ngdoc directive\n * @name izzyposWebApp.directive:adminPosHeader\n * @description\n * # adminPosHeader\n */",
        "/**\n * @ngdoc directive\n * @name sentinelDashboardApp.directive:header\n * @description\n * 顶栏指令：展示控制台版本、会话校验与登出。\n */",
    ),
    (
        "      controller: function ($scope, $state, $window, VersionService, AuthService) {",
        "      /** 顶栏控制器：拉取版本号并维护登录态。 */\n      controller: function ($scope, $state, $window, VersionService, AuthService) {",
    ),
    (
        "            // Historical version compatibility processing, fixes issue-1449",
        "            // 历史版本 localStorage 格式兼容处理，修复 issue-1449",
    ),
    (
        "            // If error happens while parsing, remove item in localStorage and redirect to login page.",
        "            // 解析失败则清除缓存并跳转登录页",
    ),
    (
        "        function handleLogout($scope, id) {",
        "        /** 内置账号 FAKE_EMP_ID 不显示登出按钮。 */\n        function handleLogout($scope, id) {",
    ),
    (
        "        $scope.logout = function () {",
        "        /** 调用登出接口并清除本地会话。 */\n        $scope.logout = function () {",
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
    index_file = Path("/tmp/git-index-sentinel-w22b")
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
        "sentinel 1.8.10: Chinese-annotate wave 22b [15:30]",
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
        "queue: mark sentinel 1.8.10 wave22b done",
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
