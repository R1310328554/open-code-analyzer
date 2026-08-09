/** 流控规则 HTTP 服务（v1 API）：按机器查询及 CRUD 流控规则。 */
var app = angular.module('sentinelDashboardApp');

app.service('FlowServiceV1', ['$http', function ($http) {
    /** GET /v1/flow/rules 拉取指定机器的流控规则列表。 */
    this.queryMachineRules = function (app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/v1/flow/rules',
            params: param,
            method: 'GET'
        });
    };

    /** POST /v1/flow/rule 新增流控规则。 */
    this.newRule = function (rule) {
        var param = {
            resource: rule.resource,
            limitApp: rule.limitApp,
            grade: rule.grade,
            count: rule.count,
            strategy: rule.strategy,
            refResource: rule.refResource,
            controlBehavior: rule.controlBehavior,
            warmUpPeriodSec: rule.warmUpPeriodSec,
            maxQueueingTimeMs: rule.maxQueueingTimeMs,
            app: rule.app,
            ip: rule.ip,
            port: rule.port
        };

        return $http({
            url: '/v1/flow/rule',
            data: rule,
            method: 'POST'
        });
    };

    /** PUT /v1/flow/save.json 更新已有流控规则。 */
    this.saveRule = function (rule) {
        var param = {
            id: rule.id,
            resource: rule.resource,
            limitApp: rule.limitApp,
            grade: rule.grade,
            count: rule.count,
            strategy: rule.strategy,
            refResource: rule.refResource,
            controlBehavior: rule.controlBehavior,
            warmUpPeriodSec: rule.warmUpPeriodSec,
            maxQueueingTimeMs: rule.maxQueueingTimeMs,
        };

        return $http({
            url: '/v1/flow/save.json',
            params: param,
            method: 'PUT'
        });
    };

    /** DELETE /v1/flow/delete.json 删除流控规则。 */
    this.deleteRule = function (rule) {
        var param = {
            id: rule.id,
            app: rule.app
        };

        return $http({
            url: '/v1/flow/delete.json',
            params: param,
            method: 'DELETE'
        });
    };

    /** 判断数值是否未定义、非数字或小于 0。 */
    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    /** 判断数值是否未定义、非数字或不大于 0。 */
    function notNumberGreaterThanZero(num) {
        return num === undefined || num === '' || isNaN(num) || num <= 0;
    }

    /** 校验资源名、阈值、流控模式、关联资源、整形方式与集群配置。 */
    this.checkRuleValid = function (rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('资源名称不能为空');
            return false;
        }
        if (rule.count === undefined || rule.count < 0) {
            alert('限流阈值必须大于等于 0');
            return false;
        }
        if (rule.strategy === undefined || rule.strategy < 0) {
            alert('无效的流控模式');
            return false;
        }
        if (rule.strategy == 1 || rule.strategy == 2) {
            if (rule.refResource === undefined || rule.refResource == '') {
                alert('请填写关联资源或入口');
                return false;
            }
        }
        if (rule.controlBehavior === undefined || rule.controlBehavior < 0) {
            alert('无效的流控整形方式');
            return false;
        }
        if (rule.controlBehavior == 1 && notNumberGreaterThanZero(rule.warmUpPeriodSec)) {
            alert('预热时长必须大于 0');
            return false;
        }
        if (rule.controlBehavior == 2 && notNumberGreaterThanZero(rule.maxQueueingTimeMs)) {
            alert('排队超时时间必须大于 0');
            return false;
        }
        if (rule.clusterMode && (rule.clusterConfig === undefined || rule.clusterConfig.thresholdType === undefined)) {
            alert('集群限流配置不正确');
            return false;
        }
        return true;
    };
}]);
