/** 登录页控制器：校验凭据并写入 localStorage 会话。 */
var app = angular.module('sentinelDashboardApp');

app.controller('LoginCtl', ['$scope', '$state', '$window', 'AuthService',
  function ($scope, $state, $window, AuthService) {
    // 已有有效会话则直接跳转控制台首页
    if ($window.localStorage.getItem('session_sentinel_admin')) {
      $state.go('dashboard');
    }

    /** 提交用户名密码，成功后缓存 session 并进入 dashboard。 */
    $scope.login = function () {
      if (!$scope.username) {
        alert('请输入用户名');
        return;
      }

      if (!$scope.password) {
        alert('请输入密码');
        return;
      }

      var param = {"username": $scope.username, "password": $scope.password};

      AuthService.login(param).success(function (data) {
        if (data.code == 0) {
          $window.localStorage.setItem('session_sentinel_admin', JSON.stringify(data.data));
          $state.go('dashboard');
        } else {
          alert(data.msg);
        }
      });
    };
  }]
);