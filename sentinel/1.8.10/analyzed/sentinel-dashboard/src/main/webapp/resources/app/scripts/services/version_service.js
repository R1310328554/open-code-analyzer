/** Dashboard 版本信息 Angular 服务。 */
var app = angular.module('sentinelDashboardApp');

app.service('VersionService', ['$http', function ($http) {
  /** 请求后端 /version 接口，返回 Dashboard 版本号。 */
  this.version = function () {
    return $http({
      url: '/version',
      method: 'GET'
    });
  };
}]);
