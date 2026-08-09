
/** 应用列表 HTTP 服务：拉取 Dashboard 已注册应用摘要。 */
var app = angular.module('sentinelDashboardApp');

app.service('AppService', ['$http', function ($http) {
  /** GET briefinfos.json 获取所有应用及其机器健康概况。 */
  this.getApps = function () {
    return $http({
      // url: 'app/mock_infos',
      url: 'app/briefinfos.json',
      method: 'GET'
    });
  };
}]);
