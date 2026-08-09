/**
 * 授权规则 HTTP 服务：按机器查询及 CRUD 黑白名单规则。
 */
angular.module('sentinelDashboardApp').service('AuthorityRuleService', ['$http', function ($http) {
    /** GET /authority/rules 拉取指定机器的授权规则列表。 */
    this.queryMachineRules = function(app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/authority/rules',
            params: param,
            method: 'GET'
        });
    };

    /** POST /authority/rule 新增授权规则。 */
    this.addNewRule = function(rule) {
        return $http({
            url: '/authority/rule',
            data: rule,
            method: 'POST'
        });
    };

    /** PUT /authority/rule/{id} 更新已有授权规则。 */
    this.saveRule = function (entity) {
        return $http({
            url: '/authority/rule/' + entity.id,
            data: entity,
            method: 'PUT'
        });
    };

    /** DELETE /authority/rule/{id} 删除授权规则。 */
    this.deleteRule = function (entity) {
        return $http({
            url: '/authority/rule/' + entity.id,
            method: 'DELETE'
        });
    };

    /** 校验资源名、限流应用与黑白名单策略是否完整。 */
    this.checkRuleValid = function checkRuleValid(rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('资源名称不能为空');
            return false;
        }
        if (rule.limitApp === undefined || rule.limitApp === '') {
            alert('流控针对应用不能为空');
            return false;
        }
        if (rule.strategy === undefined) {
            alert('必须选择黑白名单模式');
            return false;
        }
        return true;
    };
}]);
