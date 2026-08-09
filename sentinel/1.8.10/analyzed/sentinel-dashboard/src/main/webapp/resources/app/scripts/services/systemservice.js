/** 系统保护规则 Angular 服务：LOAD/RT/线程数/QPS/CPU 阈值管理。 */
var app = angular.module('sentinelDashboardApp');

app.service('SystemService', ['$http', function ($http) {
  /** 拉取指定机器上的系统保护规则列表。 */
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'system/rules.json',
      params: param,
      method: 'GET'
    });
  };

  /** 新增系统保护规则，按 grade 映射 highestSystemLoad/avgRt 等字段。 */
  this.newRule = function (rule) {
    var param = {
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };
    if (rule.grade == 0) {// 系统负载（avgLoad）
      param.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// 平均 RT（avgRt）
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// 最大线程数（maxThread）
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// 入口 QPS（qps）
      param.qps = rule.qps;
    } else if (rule.grade == 4) {// CPU 使用率（cpu）
      param.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/system/new.json',
      params: param,
      method: 'GET'
    });
  };

  /** 按 ID 保存系统保护规则修改。 */
  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
    };
    if (rule.grade == 0) {// avgLoad
      param.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// avgRt
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      param.qps = rule.qps;
    } else if (rule.grade == 4) {// cpu
        param.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/system/save.json',
      params: param,
      method: 'GET'
    });
  };

  /** 按 ID 与应用名删除系统保护规则。 */
  this.deleteRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app
    };

    return $http({
      url: '/system/delete.json',
      params: param,
      method: 'GET'
    });
  };
}]);
