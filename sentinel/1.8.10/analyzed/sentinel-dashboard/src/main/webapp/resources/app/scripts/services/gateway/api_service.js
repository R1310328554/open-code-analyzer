/** 网关自定义 API 定义 HTTP 服务：管理 API 名称与匹配规则。 */
var app = angular.module('sentinelDashboardApp');

app.service('GatewayApiService', ['$http', function ($http) {
  /** GET /gateway/api/list.json 拉取网关 API 定义列表。 */
  this.queryApis = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/gateway/api/list.json',
      params: param,
      method: 'GET'
    });
  };

  /** POST /gateway/api/new.json 新增网关 API 定义。 */
  this.newApi = function (api) {
    return $http({
      url: '/gateway/api/new.json',
      data: api,
      method: 'POST'
    });
  };

  /** POST /gateway/api/save.json 更新已有网关 API 定义。 */
  this.saveApi = function (api) {
    return $http({
      url: '/gateway/api/save.json',
      data: api,
      method: 'POST'
    });
  };

  /** POST /gateway/api/delete.json 删除网关 API 定义。 */
  this.deleteApi = function (api) {
    var param = {
      id: api.id,
      app: api.app
    };
    return $http({
      url: '/gateway/api/delete.json',
      params: param,
      method: 'POST'
    });
  };

  /** 校验 API 名称、匹配规则非空且名称不重复。 */
  this.checkApiValid = function (api, apiNames) {
    if (api.apiName === undefined || api.apiName === '') {
      alert('API名称不能为空');
      return false;
    }

    if (api.predicateItems == null || api.predicateItems.length === 0) {
      // 仅剩一条匹配规则时不应出现此情况（界面不显示删除按钮）
      alert('至少有一个匹配规则');
      return false;
    }

    for (var i = 0; i < api.predicateItems.length; i++) {
      var predicateItem = api.predicateItems[i];
      var pattern = predicateItem.pattern;
      if (pattern === undefined || pattern === '') {
        alert('匹配串不能为空，请检查');
        return false;
      }
    }

    if (apiNames.indexOf(api.apiName) !== -1) {
      alert('API名称(' + api.apiName + ')已存在');
      return false;
    }

    return true;
  };
}]);
