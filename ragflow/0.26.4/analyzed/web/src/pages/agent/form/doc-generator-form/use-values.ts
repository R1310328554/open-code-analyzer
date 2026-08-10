// use-values.ts — DocGenerator 节点表单初始值：输出格式校验与字体下限。

import { type Node } from '@xyflow/react';
import { useMemo } from 'react';
import { initialDocGeneratorValues } from '../../constant';

/** 合并节点 form 与默认 DocGenerator 配置，规范化 output_format 与 font_size。 */
export const useValues = (node?: Node) => {
  const values = useMemo(() => {
    // 允许的文档输出格式白名单
    const supportedOutputFormats = ['pdf', 'docx', 'txt', 'markdown', 'html'];
    const nextValues = {
      ...initialDocGeneratorValues,
      ...(node?.data.form ?? {}),
    };

    return {
      output_format: supportedOutputFormats.includes(nextValues.output_format)
        ? nextValues.output_format
        : initialDocGeneratorValues.output_format,
      content: nextValues.content,
      filename: nextValues.filename,
      header_text: nextValues.header_text,
      footer_text: nextValues.footer_text,
      watermark_text: nextValues.watermark_text,
      add_page_numbers: nextValues.add_page_numbers,
      add_timestamp: nextValues.add_timestamp,
      include_download_info_in_content:
        nextValues.include_download_info_in_content ?? false,
      font_size: Math.max(12, Number(nextValues.font_size) || 12),
      outputs: initialDocGeneratorValues.outputs,
    };
  }, [node?.data.form]);

  return values;
};
