/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * 侧边栏搜索区域指令，绑定 sidebar-search 模板。
 */

angular.module('sentinelDashboardApp')
  .directive('sidebarSearch', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar-search/sidebar-search.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      /** 初始化当前选中菜单项（默认 home）。 */
      controller: function ($scope) {
        $scope.selectedMenu = 'home';
      }
    }
  });
