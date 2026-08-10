// Jest 测试全局 setup：配置 Enzyme React 17 适配器、fetch mock 与 jsdom 缺失 API 垫片。

import { configure } from 'enzyme';
import Adapter from '@wojtekmaj/enzyme-adapter-react-17';
import { GlobalWithFetchMock } from 'jest-fetch-mock';
// mutationobserver-shim 供 CodeMirror 表达式输入在 jsdom 中监听 DOM 变更。
import 'mutationobserver-shim'; // Needed for CodeMirror.
import './globals';
import 'jest-canvas-mock';

configure({ adapter: new Adapter() });
const customGlobal: GlobalWithFetchMock = global as GlobalWithFetchMock;
customGlobal.fetch = require('jest-fetch-mock');
customGlobal.fetchMock = customGlobal.fetch;

// mock matchMedia 避免 Mantine/响应式组件在 Jest 中因缺少媒体查询 API 而报错。
// https://stackoverflow.com/questions/39830580/jest-test-fails-typeerror-window-matchmedia-is-not-a-function
// https://jestjs.io/docs/manual-mocks#mocking-methods-which-are-not-implemented-in-jsdom
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // Deprecated
    removeListener: jest.fn(), // Deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// document.getSelection 桩实现供 CodeMirror 选区操作，升级 react-scripts 后或可移除。
// CodeMirror in the expression input requires this DOM API. When we upgrade react-scripts
// and the associated Jest deps, hopefully this won't be needed anymore.
document.getSelection = function () {
  return {
    addRange: function () {
      return;
    },
    removeAllRanges: function () {
      return;
    },
  };
};
// setupTests 在所有 *.test.ts(x) 运行前执行一次全局初始化。
