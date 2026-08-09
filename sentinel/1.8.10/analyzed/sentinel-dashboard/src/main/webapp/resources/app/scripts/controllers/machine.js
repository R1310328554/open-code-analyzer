/** 机器管理页控制器：展示应用下客户端机器列表并支持移除。 */
var app = angular.module('sentinelDashboardApp');

app.controller('MachineCtl', ['$scope', '$stateParams', 'MachineService',
  function ($scope, $stateParams, MachineService) {
    $scope.app = $stateParams.app;
    $scope.propertyName = '';
    $scope.reverse = false;
    $scope.currentPage = 1;
    $scope.machines = [];
    $scope.machinesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    /** 按列名切换升序/降序排序。 */
    $scope.sortBy = function (propertyName) {
      // console.log('machine sortBy ' + propertyName);
      $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };
    
    /** 重新拉取机器列表并统计健康数量。 */
    $scope.reloadMachines = function() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          // console.log('get machines: ' + data.data[0].hostname)
          if (data.code == 0 && data.data) {
            $scope.machines = data.data;
            var healthy = 0;
            $scope.machines.forEach(function (item) {
              if (item.healthy) {
                  healthy++;
              }
              if (!item.hostname) {
                item.hostname = '未知'
              }
            })
            $scope.healthyCount = healthy;
            $scope.machinesPageConfig.totalCount = $scope.machines.length;
          } else {
            $scope.machines = [];
            $scope.healthyCount = 0;
          }
        }
      );
    };
    
    /** 确认后从 Dashboard 移除指定机器注册信息。 */
    $scope.removeMachine = function(ip, port) {
      if (!confirm("confirm to remove machine [" + ip + ":" + port + "]?")) {
        return;
      }
      MachineService.removeAppMachine($scope.app, ip, port).success(
        function(data) {
          if (data.code == 0) {
            $scope.reloadMachines();
          } else {
            alert("remove failed");
          }
        }
      );
    };
    
    $scope.reloadMachines();
    
  }]);
