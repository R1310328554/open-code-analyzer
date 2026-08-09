/** 网关流控规则 HTTP 服务：按 API 维度管理 QPS/线程数限流。 */
var app = angular.module('sentinelDashboardApp');

app.service('GatewayFlowService', ['$http', function ($http) {
  /** GET /gateway/flow/list.json 拉取网关流控规则列表。 */
  this.queryRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };

    return $http({
      url: '/gateway/flow/list.json',
      params: param,
      method: 'GET'
    });
  };

  /** POST /gateway/flow/new.json 新增网关流控规则。 */
  this.newRule = function (rule) {
    return $http({
      url: '/gateway/flow/new.json',
      data: rule,
      method: 'POST'
    });
  };

  /** POST /gateway/flow/save.json 更新已有网关流控规则。 */
  this.saveRule = function (rule) {
    return $http({
      url: '/gateway/flow/save.json',
      data: rule,
      method: 'POST'
    });
  };

  /** POST /gateway/flow/delete.json 删除网关流控规则。 */
  this.deleteRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app
    };

    return $http({
      url: '/gateway/flow/delete.json',
      params: param,
      method: 'POST'
    });
  };

  /** 校验 API 名称、参数属性匹配串与 QPS/线程数阈值。 */
  this.checkRuleValid = function (rule) {
    if (rule.resource === undefined || rule.resource === '') {
      alert('API名称不能为空');
      return false;
    }

    if (rule.paramItem != null) {
      if (rule.paramItem.parseStrategy == 2 ||
          rule.paramItem.parseStrategy == 3 ||
          rule.paramItem.parseStrategy == 4) {
        if (rule.paramItem.fieldName === undefined || rule.paramItem.fieldName === '') {
          alert('当参数属性为Header、URL参数、Cookie时，参数名称不能为空');
          return false;
        }

        if (rule.paramItem.pattern === '') {
          alert('匹配串不能为空');
          return false;
        }
      }
    }

    if (rule.count === undefined || rule.count < 0) {
      alert((rule.grade === 1 ? 'QPS阈值' : '线程数') + '必须大于等于 0');
      return false;
    }

    return true;
  };
}]);
