// React 页面模块 barrel 导出：各功能页经 withStartingIndicator 包装后统一对外暴露。

import Agent from './agent/Agent';
import Alerts from './alerts/Alerts';
import Config from './config/Config';
import Flags from './flags/Flags';
import Rules from './rules/Rules';
import ServiceDiscovery from './serviceDiscovery/Services';
import Status from './status/Status';
import Targets from './targets/Targets';
import PanelList from './graph/PanelList';
import TSDBStatus from './tsdbStatus/TSDBStatus';
import { withStartingIndicator } from '../components/withStartingIndicator';

// withStartingIndicator 在 Prometheus 未就绪时展示启动/WAL 重放进度占位页。
const AgentPage = withStartingIndicator(Agent);
const AlertsPage = withStartingIndicator(Alerts);
const ConfigPage = withStartingIndicator(Config);
const FlagsPage = withStartingIndicator(Flags);
const RulesPage = withStartingIndicator(Rules);
const ServiceDiscoveryPage = withStartingIndicator(ServiceDiscovery);
const StatusPage = withStartingIndicator(Status);
const TSDBStatusPage = withStartingIndicator(TSDBStatus);
const TargetsPage = withStartingIndicator(Targets);
const PanelListPage = withStartingIndicator(PanelList);

// prettier-ignore
// 导出 Agent/Alerts/Graph(PanelList)/TSDB 等路由页面组件供 App 路由表引用。
export {
  AgentPage,
  AlertsPage,
  ConfigPage,
  FlagsPage,
  RulesPage,
  ServiceDiscoveryPage,
  StatusPage,
  TSDBStatusPage,
  TargetsPage,
  PanelListPage
};
