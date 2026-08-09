/** Angular 过滤器模块：提供分页等辅助过滤器。 */
var app = angular.module('sentinelDashboardApp');

/** 生成 1..length 的整数数组，供 ng-repeat 分页使用。 */
app.filter('range', [function () {
  return function (input, length) {
    if (isNaN(length) || length <= 0) {
      return [];
    }

    input = [];
    for (var index = 1; index <= length; index++) {
      input.push(index);
    }

    return input;
  };
  
}]);
