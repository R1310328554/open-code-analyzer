/** 熔断降级规则 HTTP 服务：按机器查询及 CRUD 降级规则。 */
var app = angular.module('sentinelDashboardApp');

app.service('DegradeService', ['$http', function ($http) {
  /** GET degrade/rules.json 拉取指定机器的熔断规则列表。 */
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'degrade/rules.json',
      params: param,
      method: 'GET'
    });
  };

  /** POST /degrade/rule 新增熔断规则。 */
  this.newRule = function (rule) {
    return $http({
        url: '/degrade/rule',
        data: rule,
        method: 'POST'
    });
  };

  /** PUT /degrade/rule/{id} 更新已有熔断规则。 */
  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
      resource: rule.resource,
      limitApp: rule.limitApp,
      grade: rule.grade,
      count: rule.count,
      timeWindow: rule.timeWindow,
        statIntervalMs: rule.statIntervalMs,
        minRequestAmount: rule.minRequestAmount,
        slowRatioThreshold: rule.slowRatioThreshold,
    };
    return $http({
        url: '/degrade/rule/' + rule.id,
        data: param,
        method: 'PUT'
    });
  };

  /** DELETE /degrade/rule/{id} 删除熔断规则。 */
  this.deleteRule = function (rule) {
      return $http({
          url: '/degrade/rule/' + rule.id,
          method: 'DELETE'
      });
  };

  /** 校验资源名、降级策略、阈值、熔断时长与统计窗口等字段。 */
  this.checkRuleValid = function (rule) {
      if (rule.resource === undefined || rule.resource === '') {
          alert('资源名称不能为空');
          return false;
      }
      if (rule.grade === undefined || rule.grade < 0) {
          alert('未知的降级策略');
          return false;
      }
      if (rule.count === undefined || rule.count === '' || rule.count < 0) {
          alert('降级阈值不能为空或小于 0');
          return false;
      }
      if (rule.timeWindow == undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
          alert('熔断时长必须大于 0s');
          return false;
      }
      if (rule.minRequestAmount == undefined || rule.minRequestAmount <= 0) {
          alert('最小请求数目需大于 0');
          return false;
      }
      if (rule.statIntervalMs == undefined || rule.statIntervalMs <= 0) {
          alert('统计窗口时长需大于 0s');
          return false;
      }
      if (rule.statIntervalMs !== undefined && rule.statIntervalMs > 60 * 1000 * 2) {
          alert('统计窗口时长最大 120s');
          return false;
      }
      // 异常比率策略：count 须在 [0.0, 1.0] 范围内
      if (rule.grade == 1 && rule.count > 1) {
          alert('异常比率超出范围：[0.0 - 1.0]');
          return false;
      }
      if (rule.grade == 0) {
          if (rule.slowRatioThreshold == undefined) {
              alert('慢调用比率不能为空');
              return false;
          }
          if (rule.slowRatioThreshold < 0 || rule.slowRatioThreshold > 1) {
              alert('慢调用比率超出范围：[0.0 - 1.0]');
              return false;
          }
      }
      return true;
  };
}]);
