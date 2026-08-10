// 全局依赖注入：将 jQuery 与 moment 挂到 window，供旧版 Flot 图表与模板脚本使用。

import jquery from 'jquery';
import moment from 'moment';

window.jQuery = jquery;
window.moment = moment;
