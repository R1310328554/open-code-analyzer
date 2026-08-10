// 第三方库 TypeScript 增强：扩展 jQuery Flot 图表选项与全局 jQuery.color、moment 类型。

declare namespace jquery.flot {
  interface plot extends jquery.flot.plot {
    destroy: () => void;
  }
  interface plotOptions extends jquery.flot.plotOptions {
    tooltip: {
      show?: boolean;
      cssClass?: string;
      content: (
        label: string,
        xval: number,
        yval: number,
        flotItem: jquery.flot.item & {
          series: {
            labels: { [key: string]: string };
            color: string;
            data: (number | null)[][]; // [x,y][]
            index: number;
          };
        }
      ) => string | string;
      xDateFormat?: string;
      yDateFormat?: string;
      monthNames?: string;
      dayNames?: string;
      shifts?: {
        x: number;
        y: number;
      };
      defaultTheme?: boolean;
      lines?: boolean;
      onHover?: () => string;
      $compat?: boolean;
    };
    crosshair: Partial<jquery.flot.axisOptions, 'mode' | 'color'>;
    xaxis: { [K in keyof jquery.flot.axisOptions]: jquery.flot.axisOptions[K] } & {
      showTicks: boolean;
      showMinorTicks: boolean;
      timeBase: 'milliseconds';
    };
    series: { [K in keyof jquery.flot.seriesOptions]: jq.flot.seriesOptions[K] } & {
      stack: boolean;
      heatmap: boolean;
    };
    selection: {
      mode: string;
    };
  }
}

// Color 描述 jquery-color 插件的 RGBA 分量及 add/scale/normalize 等颜色运算。
interface Color {
  r: number;
  g: number;
  b: number;
  a: number;
  add: (c: string, d: number) => Color;
  scale: (c: string, f: number) => Color;
  toString: () => string;
  normalize: () => Color;
  clone: () => Color;
}

// JQueryStatic.color 提供从 DOM/CSS 提取颜色与 parse/make 工厂方法。
interface JQueryStatic {
  color: {
    extract: (el: JQuery<HTMLElement>, css?: CSSStyleDeclaration) => Color;
    make: (r?: number, g?: number, b?: number, a?: number) => Color;
    parse: (c: string) => Color;
    scale: () => Color;
  };
}

// Window 声明全局 jQuery 与 moment，供 graph 页 Flot 与时间轴格式化使用。
interface Window {
  jQuery: JQueryStatic;
  moment: typeof import('moment');
}
