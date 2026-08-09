/**
 * @ngdoc directive
 * @name sentinelDashboardApp.directive:header
 * @description
 * 顶栏指令：展示控制台版本、会话校验与登出。
 */
angular.module('sentinelDashboardApp')
  .directive('header', ['VersionService', 'AuthService', function () {
    return {
      templateUrl: 'app/scripts/directives/header/header.html',
      restrict: 'E',
      replace: true,
      /** 顶栏控制器：拉取版本号并维护登录态。 */
      controller: function ($scope, $state, $window, VersionService, AuthService) {
        VersionService.version().success(function (data) {
          if (data.code == 0) {
            $scope.dashboardVersion = data.data;
          }
        });

        if (!$window.localStorage.getItem("session_sentinel_admin")) {
          AuthService.check().success(function (data) {
            if (data.code == 0) {
              $window.localStorage.setItem('session_sentinel_admin', JSON.stringify(data.data));
              handleLogout($scope, data.data.id)
            } else {
              $state.go('login');
            }
          });
        } else {
          try {
            var id = JSON.parse($window.localStorage.getItem("session_sentinel_admin")).id;
            handleLogout($scope, id);
          } catch (e) {
            // 历史版本 localStorage 格式兼容处理，修复 issue-1449
            // 解析失败则清除缓存并跳转登录页
            $window.localStorage.removeItem("session_sentinel_admin");
            $state.go('login');
          }
        }

        /** 内置账号 FAKE_EMP_ID 不显示登出按钮。 */
        function handleLogout($scope, id) {
          if (id == 'FAKE_EMP_ID') {
            $scope.showLogout = false;
          } else {
            $scope.showLogout = true;
          }
        }

        /** 调用登出接口并清除本地会话。 */
        $scope.logout = function () {
          AuthService.logout().success(function (data) {
            if (data.code == 0) {
              $window.localStorage.removeItem("session_sentinel_admin");
              $state.go('login');
            } else {
              alert('logout error');
            }
          });
        }
      }
    }
  }]);
