/** 网关资源簇（Identity）页控制器：展示资源列表并快捷新增流控/降级规则。 */
var app = angular.module('sentinelDashboardApp');

app.controller('GatewayIdentityCtl', ['$scope', '$stateParams', 'IdentityService',
  'ngDialog', 'GatewayFlowService', 'GatewayApiService', 'DegradeService', 'MachineService',
  '$interval', '$location', '$timeout',
  function ($scope, $stateParams, IdentityService, ngDialog,
    GatewayFlowService, GatewayApiService, DegradeService, MachineService, $interval, $location, $timeout) {

    $scope.app = $stateParams.app;

    $scope.currentPage = 1;
    $scope.pageSize = 16;
    $scope.totalPage = 1;
    $scope.totalCount = 0;
    $scope.identities = [];

    $scope.searchKey = '';

    $scope.macsInputConfig = {
      searchField: ['text', 'value'],
      persist: true,
      create: false,
      maxItems: 1,
      render: {
        item: function (data, escape) {
          return '<div>' + escape(data.text) + '</div>';
        }
      },
      onChange: function (value, oldValue) {
        $scope.macInputModel = value;
      }
    };
    $scope.table = null;

    getApiNames();
    /** 拉取自定义 API 名称，用于判断资源模式。 */
    function getApiNames() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = $scope.macInputModel.split(':');
      GatewayApiService.queryApis($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.apiNames = [];

            data.data.forEach(function (api) {
              $scope.apiNames.push(api["apiName"]);
            });
          }
        });
    }

    var gatewayFlowRuleDialog;
    var gatewayFlowRuleDialogScope;
    /** 为指定资源打开新增网关流控规则对话框（独立 scope）。 */
    $scope.addNewGatewayFlowRule = function (resource) {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      gatewayFlowRuleDialogScope = $scope.$new(true);

      gatewayFlowRuleDialogScope.apiNames = $scope.apiNames;

      gatewayFlowRuleDialogScope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];

      gatewayFlowRuleDialogScope.currentRule = {
        grade: 1,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        resourceMode: gatewayFlowRuleDialogScope.apiNames.indexOf(resource) == -1 ? 0 : 1,
        resource: resource,
        interval: 1,
        intervalUnit: 0,
        controlBehavior: 0,
        burst: 0,
        maxQueueingTimeoutMs: 0
      };

      gatewayFlowRuleDialogScope.gatewayFlowRuleDialog = {
        title: '新增网关流控规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加',
        showAdvanceButton: true
      };

      gatewayFlowRuleDialogScope.useRouteID = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useCustormAPI = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = {
          parseStrategy: 0,
          matchStrategy: 0
        };
      };

      gatewayFlowRuleDialogScope.notUseParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = null;
      };

      gatewayFlowRuleDialogScope.useParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = "";
      };

      gatewayFlowRuleDialogScope.notUseParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = null;
      };

      gatewayFlowRuleDialogScope.saveRule = saveGatewayFlowRule;
      gatewayFlowRuleDialogScope.saveRuleAndContinue = saveGatewayFlowRuleAndContinue;
      gatewayFlowRuleDialogScope.onOpenAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = false;
      };
      gatewayFlowRuleDialogScope.onCloseAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = true;
      };

      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: gatewayFlowRuleDialogScope
      });
    };

    /** 保存网关流控规则并跳转至流控规则页。 */
    function saveGatewayFlowRule() {
      if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
        return;
      }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code === 0) {
          gatewayFlowRuleDialog.close();
          let url = '/dashboard/gateway/flow/' + $scope.app;
          $location.path(url);
        } else {
          alert('失败!');
        }
      }).error((data, header, config, status) => {
          alert('未知错误');
      });
    }

    /** 保存规则后保持当前页，便于连续添加。 */
    function saveGatewayFlowRuleAndContinue() {
        if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
            return;
        }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          gatewayFlowRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var degradeRuleDialog;
    /** 为指定资源打开新增降级规则对话框。 */
    $scope.addNewDegradeRule = function (resource) {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      degradeRuleDialogScope = $scope.$new(true);
      degradeRuleDialogScope.currentRule = {
        enable: false,
        grade: 0,
        strategy: 0,
        resource: resource,
        limitApp: 'default',
        app: $scope.app,
        ip: mac[0],
        port: mac[1]
      };

      degradeRuleDialogScope.degradeRuleDialog = {
        title: '新增降级规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加'
      };
      degradeRuleDialogScope.saveRule = saveDegradeRule;
      degradeRuleDialogScope.saveRuleAndContinue = saveDegradeRuleAndContinue;

      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: degradeRuleDialogScope
      });
    };

    /** 保存降级规则并跳转至降级规则页。 */
    function saveDegradeRule() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
          var url = '/dashboard/degrade/' + $scope.app;
          $location.path(url);
        } else {
          alert('失败!');
        }
      });
    }

    /** 保存降级规则后继续留在当前页。 */
    function saveDegradeRuleAndContinue() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var searchHandler;
    /** 防抖搜索：600ms 后按关键字刷新资源列表。 */
    $scope.searchChange = function (searchKey) {
      $timeout.cancel(searchHandler);
      searchHandler = $timeout(function () {
        $scope.searchKey = searchKey;
        reInitIdentityDatas();
      }, 600);
    };

    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code === 0) {
            if (data.data) {
              $scope.machines = [];
              $scope.macsInputOptions = [];
              data.data.forEach(function (item) {
                if (item.healthy) {
                  $scope.macsInputOptions.push({
                    text: item.ip + ':' + item.port,
                    value: item.ip + ':' + item.port
                  });
                }
              });
            }
            if ($scope.macsInputOptions.length > 0) {
              $scope.macInputModel = $scope.macsInputOptions[0].value;
            }
          } else {
            $scope.macsInputOptions = [];
          }
        }
      );
    }

    // 按当前应用名加载全部机器
    queryAppMachines();

    $scope.$watch('macInputModel', function () {
      if ($scope.macInputModel) {
        reInitIdentityDatas();
      }
    });

    $scope.$on('$destroy', function () {
      $interval.cancel(intervalId);
    });

    var intervalId;
    /** 机器或搜索条件变化时重新拉取 API 名称与资源簇。 */
    function reInitIdentityDatas() {
      getApiNames();
      queryIdentities();
    };

    /** 查询机器上的 ClusterNode 资源列表（支持关键字过滤）。 */
    function queryIdentities() {
      var mac = $scope.macInputModel.split(':');
      if (mac == null || mac.length < 2) {
        return;
      }

      IdentityService.fetchClusterNodeOfMachine(mac[0], mac[1], $scope.searchKey).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.identities = data.data;
            $scope.totalCount = $scope.identities.length;
          } else {
            $scope.identities = [];
            $scope.totalCount = 0;
          }
        }
      );
    };
    $scope.queryIdentities = queryIdentities;
  }]);
