/** 资源树 HTTP 服务：拉取机器上的调用链路资源与集群节点。 */
var app = angular.module('sentinelDashboardApp');

app.service('IdentityService', ['$http', function ($http) {

  /** GET machineResource.json 拉取机器普通资源树（可按 searchKey 过滤）。 */
  this.fetchIdentityOfMachine = function (ip, port, searchKey) {
    var param = {
      ip: ip,
      port: port,
      searchKey: searchKey
    };
    return $http({
      url: 'resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
  /** GET machineResource.json?type=cluster 拉取机器集群节点资源树。 */
  this.fetchClusterNodeOfMachine = function (ip, port, searchKey) {
    var param = {
      ip: ip,
      port: port,
      type: 'cluster',
      searchKey: searchKey
    };
    return $http({
      url: 'resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
}]);
