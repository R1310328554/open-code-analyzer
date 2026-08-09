/**
 * 热点参数限流 Angular 服务：规则 CRUD 及前端表单校验。
 *
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ParamFlowService', ['$http', function ($http) {
  /** 拉取指定机器上的 ParamFlow 规则列表。 */
  this.queryMachineRules = function(app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/paramFlow/rules',
      params: param,
      method: 'GET'
    });
  };

  /** 新增热点参数限流规则。 */
  this.addNewRule = function(rule) {
    return $http({
      url: '/paramFlow/rule',
      data: rule,
      method: 'POST'
    });
  };

  /** 按 ID 更新热点参数限流规则。 */
  this.saveRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      data: entity,
      method: 'PUT'
    });
  };

  /** 按 ID 删除热点参数限流规则。 */
  this.deleteRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      method: 'DELETE'
    });
  };

    /** 判断参数类型是否为数值型（int/double/float/long/short）。 */
    function isNumberClass(classType) {
        return classType === 'int' || classType === 'double' ||
            classType === 'float' || classType === 'long' || classType === 'short';
    }

  /** 判断参数类型是否为 byte。 */
    function isByteClass(classType) {
        return classType === 'byte';
    }

    /** 校验数值未定义、空或非负整数（用于例外项 count）。 */
    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    /** 校验数值未定义、空或 NaN（用于数值型参数 object）。 */
    function notGoodNumber(num) {
        return num === undefined || num === '' || isNaN(num);
    }

    /** 校验数值是否在 (l, r) 开区间内（byte 参数范围 -128~127）。 */
    function notGoodNumberBetweenExclusive(num, l ,r) {
        return num === undefined || num === '' || isNaN(num) || num < l || num > r;
    }

    /** 校验单条热点参数例外项：object、classType 与 count 是否合法。 */
    function notValidParamItem(curExItem) {
        if (isNumberClass(curExItem.classType) && notGoodNumber(curExItem.object)) {
            return true;
        }
        if (isByteClass(curExItem.classType) && notGoodNumberBetweenExclusive(curExItem.object, -128, 127)) {
            return true;
        }
        return curExItem.object === undefined || curExItem.classType === undefined ||
            notNumberAtLeastZero(curExItem.count);
    }

  /** 提交前校验规则：资源名、限流模式、阈值、参数索引及例外项列表。 */
  this.checkRuleValid = function (rule) {
      if (!rule.resource || rule.resource === '') {
          alert('资源名称不能为空');
          return false;
      }
      if (rule.grade != 1) {
          alert('未知的限流模式');
          return false;
      }
      if (rule.count < 0) {
          alert('限流阈值必须大于等于 0');
          return false;
      }
      if (rule.paramIdx === undefined || rule.paramIdx === '' || isNaN(rule.paramIdx) || rule.paramIdx < 0) {
          alert('热点参数索引必须大于等于 0');
          return false;
      }
      if (rule.paramFlowItemList !== undefined) {
          for (var i = 0; i < rule.paramFlowItemList.length; i++) {
              var item = rule.paramFlowItemList[i];
              if (notValidParamItem(item)) {
                  alert('热点参数例外项不合法，请检查值和类型是否正确：参数为 ' + item.object + ', 类型为 ' +
                      item.classType + ', 限流阈值为 ' + item.count);
                  return false;
              }
          }
      }
      return true;
  };
}]);
