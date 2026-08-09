/**
 * 集群限流状态 HTTP 服务：查询 Token Server/Client 状态及分配操作。
 *
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ClusterStateService', ['$http', function ($http) {

    /** GET /cluster/state_single 拉取单台机器的集群通用状态。 */
    this.fetchClusterUniversalStateSingle = function(app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/cluster/state_single',
            params: param,
            method: 'GET'
        });
    };

    /** GET /cluster/state/{app} 拉取应用下所有机器的集群通用状态。 */
    this.fetchClusterUniversalStateOfApp = function(app) {
        return $http({
            url: '/cluster/state/' + app,
            method: 'GET'
        });
    };

    /** GET /cluster/server_state/{app} 拉取应用维度 Token Server 状态。 */
    this.fetchClusterServerStateOfApp = function(app) {
        return $http({
            url: '/cluster/server_state/' + app,
            method: 'GET'
        });
    };

    /** GET /cluster/client_state/{app} 拉取应用维度 Token Client 状态。 */
    this.fetchClusterClientStateOfApp = function(app) {
        return $http({
            url: '/cluster/client_state/' + app,
            method: 'GET'
        });
    };

    /** POST /cluster/config/modify_single 修改单台机器集群配置。 */
    this.modifyClusterConfig = function(config) {
        return $http({
            url: '/cluster/config/modify_single',
            data: config,
            method: 'POST'
        });
    };

    /** POST /cluster/assign/all_server/{app} 一键应用完整集群分配方案。 */
    this.applyClusterFullAssignOfApp = function(app, clusterMap) {
        return $http({
            url: '/cluster/assign/all_server/' + app,
            data: clusterMap,
            method: 'POST'
        });
    };

    /** POST /cluster/assign/single_server/{app} 对单个 Token Server 执行分配。 */
    this.applyClusterSingleServerAssignOfApp = function(app, request) {
        return $http({
            url: '/cluster/assign/single_server/' + app,
            data: request,
            method: 'POST'
        });
    };

    /** POST /cluster/assign/unbind_server/{app} 批量解绑 Token Server。 */
    this.applyClusterServerBatchUnbind = function(app, machineSet) {
        return $http({
            url: '/cluster/assign/unbind_server/' + app,
            data: machineSet,
            method: 'POST'
        });
    };
}]);
