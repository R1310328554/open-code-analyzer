/** 机器管理 HTTP 服务：查询应用机器列表与移除机器。 */
var app = angular.module('sentinelDashboardApp');

app.service('MachineService', ['$http', '$httpParamSerializerJQLike',
  function ($http, $httpParamSerializerJQLike) {
    /** GET app/{app}/machines.json 拉取应用下已注册机器列表。 */
    this.getAppMachines = function (app) {
      return $http({
        url: 'app/' + app + '/machines.json',
        method: 'GET'
      });
    };
    /** POST app/{app}/machine/remove.json 从 Dashboard 移除指定机器。 */
    this.removeAppMachine = function (app, ip, port) {
      return $http({
        url: 'app/' + app + '/machine/remove.json',
        method: 'POST',
        headers: {
          'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: $httpParamSerializerJQLike({
          ip: ip,
          port: port
        })
      });
    };
  }]
);
