/**
 * 侧边栏指令：展示应用列表、健康机器数与网关标识，支持折叠切换。
 */
angular.module('sentinelDashboardApp')
  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      controller: function ($scope, $stateParams, $location, AppService) {
        $scope.app = $stateParams.app;
        $scope.collapseVar = 0;

        // 拉取应用列表并标记当前路由对应的应用为 active
        AppService.getApps().success(
          function (data) {
            if (data.code === 0) {
              let path = $location.path().split('/');
              let initHashApp = path[path.length - 1];
              $scope.apps = data.data;
              $scope.apps = $scope.apps.map(function (item) {
                if (item.app === initHashApp) {
                  item.active = true;
                }
                let healthyCount = 0;
                for (let i in item.machines) {
                  if (item.machines[i].healthy) {
                      healthyCount++;
                  }
                }
                item.healthyCount = healthyCount;
                // 根据 appType 判断是否为网关应用（1/11/12）
                item.isGateway = item.appType === 1 || item.appType === 11 || item.appType === 12;

                if (item.shown) {
                  return item;
                }
              });
            }
          }
        );

        // 点击应用条目时展开当前项并折叠其余项
        $scope.click = function ($event) {
          let entry = angular.element($event.target).scope().entry;
          entry.active = !entry.active;  // 切换当前应用条目的展开状态

          $scope.apps.forEach(function (item) {  // 折叠其他应用条目
            if (item !== entry) {
              item.active = false;
            }
          });
        };

        /**
         * 手动添加搜索应用（已废弃）。
         * @deprecated
         */
        $scope.addSearchApp = function () {
          let findApp = false;
          for (let i = 0; i < $scope.apps.length; i++) {
            if ($scope.apps[i].app === $scope.searchApp) {
              findApp = true;
              break;
            }
          }
          if (!findApp) {
            $scope.apps.push({ app: $scope.searchApp });
          }
        };
      }
    };
  }]);
