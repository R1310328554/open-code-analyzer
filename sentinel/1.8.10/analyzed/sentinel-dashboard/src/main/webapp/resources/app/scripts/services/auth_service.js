/** 登录认证 HTTP 服务：校验会话、登录与登出。 */
var app = angular.module('sentinelDashboardApp');

app.service('AuthService', ['$http', function ($http) {
  /** POST /auth/check 校验当前 Session 是否有效。 */
  this.check = function () {
    return $http({
      url: '/auth/check',
      method: 'POST'
    });
  };

  /** POST /auth/login 提交用户名密码登录。 */
  this.login = function (param) {
    return $http({
      url: '/auth/login',
      params: param,
      method: 'POST'
    });
  };

  /** POST /auth/logout 清除服务端 Session。 */
  this.logout = function () {
    return $http({
      url: '/auth/logout',
      method: 'POST'
    });
  };
}]);
