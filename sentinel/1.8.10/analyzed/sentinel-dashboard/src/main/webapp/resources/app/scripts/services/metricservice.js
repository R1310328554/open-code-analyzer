/** 实时监控指标 Angular 服务：封装 metric 查询 REST 接口。 */
var app = angular.module('sentinelDashboardApp');

/** MetricService：按应用/资源/机器拉取 pass、block 等时序指标。 */
app.service('MetricService', ['$http', function ($http) {

  /** 查询应用下各资源的排序 metric（Top N），供实时监控页列表展示。 */
  this.queryAppSortedIdentities = function (params) {
    return $http({
      url: '/metric/queryTopResourceMetric.json',
      params: params,
      method: 'GET'
    });
  };

  /** 按应用名与资源名查询 metric 时序数据。 */
  this.queryByAppAndIdentity = function (params) {
    return $http({
      url: '/metric/queryByAppAndResource.json',
      params: params,
      method: 'GET'
    });
  };

  /** 按机器 IP/端口、资源名与时间窗口查询 metric（start/end 转为毫秒时间戳）。 */
  this.queryByMachineAndIdentity = function (ip, port, identity, startTime, endTime) {
    var param = {
      ip: ip,
      port: port,
      identity: identity,
      startTime: startTime.getTime(),
      endTime: endTime.getTime()
    };

    return $http({
      url: '/metric/queryByAppAndResource.json',
      params: param,
      method: 'GET'
    });
  };
}]);
