// django.contrib.admin 静态脚本：将 jQuery 隔离到 django.jQuery 命名空间
/*global jQuery:false*/
"use strict";
/* Puts the included jQuery into our own namespace using noConflict and passing
 * it 'true'. This ensures that the included jQuery doesn't pollute the global
 * namespace (i.e. this preserves pre-existing values for both window.$ and
 * window.jQuery).
 */
// noConflict(true) 同时释放全局 $ 与 jQuery，避免污染站点脚本
window.django = { jQuery: jQuery.noConflict(true) };
