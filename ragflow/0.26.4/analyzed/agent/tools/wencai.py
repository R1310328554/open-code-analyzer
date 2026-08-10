#
#  Copyright 2024 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
同花顺问财选股工具：自然语言条件查询股票/基金等（pywencai 集成当前已注释）。
"""

#
import logging
import os
import time
from abc import ABC
import pandas as pd
# import pywencai

from agent.tools.base import ToolParamBase, ToolMeta, ToolBase
from common.connection_utils import timeout


class WenCaiParam(ToolParamBase):
    """
    问财参数：查询语句、query_type（stock/fund 等）与 top_n。
    """

    def __init__(self):
        self.meta: ToolMeta = {
            "name": "iwencai",
            "description": """
iwencai search: search platform is committed to providing hundreds of millions of investors with the most timely, accurate and comprehensive information, covering news, announcements, research reports, blogs, forums, Weibo, characters, etc.
robo-advisor intelligent stock selection platform: through AI technology, is committed to providing investors with intelligent stock selection, quantitative investment, main force tracking, value investment, technical analysis and other types of stock selection technologies.
fund selection platform: through AI technology, is committed to providing excellent fund, value investment, quantitative analysis and other fund selection technologies for foundation citizens.
""",
            "parameters": {"query": {"type": "string", "description": "The question/conditions to select stocks.", "default": "{sys.query}", "required": True}},
        }
        super().__init__()
        self.top_n = 10
        self.query_type = "stock"

    def check(self):
        self.check_positive_integer(self.top_n, "Top N")
        self.check_valid_value(self.query_type, "Query type", ["stock", "zhishu", "fund", "hkstock", "usstock", "threeboard", "conbond", "insurance", "futures", "lccp", "foreign_exchange"])

    def get_input_form(self) -> dict[str, dict]:
        return {"query": {"name": "Query", "type": "line"}}


class WenCai(ToolBase, ABC):
    """
    调用问财 API 获取结构化结果并转为 Markdown 写入 report 输出。
    """

    component_name = "WenCai"

    @timeout(int(os.environ.get("COMPONENT_EXEC_TIMEOUT", 12)))
    def _invoke(self, **kwargs):
        if self.check_if_canceled("WenCai processing"):
            return

        if not kwargs.get("query"):
            self.set_output("report", "")
            return ""

        last_e = ""
        for _ in range(self._param.max_retries + 1):
            if self.check_if_canceled("WenCai processing"):
                return

            try:
                wencai_res = []
                # pywencai 调用已注释；res 为空时仍走统一格式化分支
                # pywencai 调用已注释；res 为空时仍走统一格式化分支
                # res = pywencai.get(query=kwargs["query"], query_type=self._param.query_type, perpage=self._param.top_n)
                res = []
                if self.check_if_canceled("WenCai processing"):
                    return

                if isinstance(res, pd.DataFrame):
                    wencai_res.append(res.to_markdown())
                # dict 结果按 key 分类型转 Markdown
                # dict 结果按 key 分类型转 Markdown
                elif isinstance(res, dict):
                    for item in res.items():
                        if self.check_if_canceled("WenCai processing"):
                            return

                        if isinstance(item[1], list):
                            wencai_res.append(item[0] + "\n" + pd.DataFrame(item[1]).to_markdown())
                        elif isinstance(item[1], str):
                            wencai_res.append(item[0] + "\n" + item[1])
                        elif isinstance(item[1], dict):
                            if "meta" in item[1].keys():
                                continue
                            wencai_res.append(pd.DataFrame.from_dict(item[1], orient="index").to_markdown())
                        elif isinstance(item[1], pd.DataFrame):
                            if "image_url" in item[1].columns:
                                continue
                            wencai_res.append(item[1].to_markdown())
                        else:
                            wencai_res.append(item[0] + "\n" + str(item[1]))
                self.set_output("report", "\n\n".join(wencai_res))
                return self.output("report")
            except Exception as e:
                if self.check_if_canceled("WenCai processing"):
                    return

                last_e = e
                logging.exception(f"WenCai error: {e}")
                time.sleep(self._param.delay_after_error)

        if last_e:
            self.set_output("_ERROR", str(last_e))
            return f"WenCai error: {last_e}"

        assert False, self.output()

    def thoughts(self) -> str:
        return "Pulling live financial data for `{}`.".format(self.get_input().get("query", "-_-!"))
