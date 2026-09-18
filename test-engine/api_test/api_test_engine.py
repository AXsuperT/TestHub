"""
TestHub 接口自动化测试引擎
支持: HTTP请求发送、变量提取、断言验证、测试套件执行、Allure报告生成
"""
import json
import time
import requests
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, field
from enum import Enum


class AssertionOperator(Enum):
    EQUALS = "equals"
    NOT_EQUALS = "notEquals"
    CONTAINS = "contains"
    NOT_CONTAINS = "notContains"
    GREATER_THAN = "greaterThan"
    LESS_THAN = "lessThan"
    NOT_NULL = "notNull"
    IS_NULL = "isNull"
    REGEX = "regex"


@dataclass
class ApiCase:
    id: int
    name: str
    method: str
    url: str
    headers: Optional[Dict] = None
    query_params: Optional[Dict] = None
    body_type: str = "NONE"
    body: Optional[str] = None
    assertions: Optional[List[Dict]] = None
    extract_vars: Optional[List[Dict]] = None
    timeout: int = 10000


@dataclass
class TestResult:
    case_id: int
    case_name: str
    result: str  # PASS / FAIL
    response_status: int = 0
    response_time: int = 0
    response_body: str = ""
    error_message: str = ""
    assertion_details: List[Dict] = field(default_factory=list)


class ApiTestEngine:
    """接口测试执行引擎"""

    def __init__(self, base_url: str = "", global_headers: Dict = None):
        self.base_url = base_url
        self.global_headers = global_headers or {}
        self.context_vars: Dict[str, str] = {}

    def execute(self, case: ApiCase) -> TestResult:
        """执行单个接口用例"""
        result = TestResult(case_id=case.id, case_name=case.name, result="FAIL")
        start_time = time.time()

        try:
            url = self._replace_vars(case.url)
            if not url.startswith("http"):
                url = self.base_url.rstrip("/") + "/" + url.lstrip("/")

            # 构建请求
            kwargs = {
                "method": case.method.upper(),
                "url": url,
                "headers": self._build_headers(case),
                "timeout": case.timeout / 1000,
            }
            if case.query_params:
                kwargs["params"] = self._replace_vars_dict(case.query_params)
            if case.body and case.body_type != "NONE":
                kwargs["data"] = self._replace_vars(case.body)

            # 发送请求
            resp = requests.request(**kwargs)
            result.response_status = resp.status_code
            result.response_time = int((time.time() - start_time) * 1000)
            result.response_body = resp.text

            # 执行断言
            result.assertion_details = self._execute_assertions(
                case.assertions, resp.status_code, resp.text
            )
            result.result = "PASS" if all(
                a.get("passed", False) for a in result.assertion_details
            ) else "FAIL"

            # 提取变量
            if case.extract_vars:
                self._extract_variables(case.extract_vars, resp.text)

        except Exception as e:
            result.error_message = str(e)
            result.response_time = int((time.time() - start_time) * 1000)

        return result

    def execute_suite(self, cases: List[ApiCase]) -> Dict[str, Any]:
        """执行测试套件"""
        results = []
        pass_count = 0
        fail_count = 0
        suite_start = time.time()

        for case in cases:
            r = self.execute(case)
            results.append(r)
            if r.result == "PASS":
                pass_count += 1
            else:
                fail_count += 1

        return {
            "total": len(cases),
            "pass": pass_count,
            "fail": fail_count,
            "duration_ms": int((time.time() - suite_start) * 1000),
            "results": results,
        }

    def _build_headers(self, case: ApiCase) -> Dict:
        headers = dict(self.global_headers)
        if case.headers:
            headers.update(self._replace_vars_dict(case.headers))
        return headers

    def _execute_assertions(
        self, assertions: Optional[List[Dict]], status_code: int, body: str
    ) -> List[Dict]:
        details = []
        if not assertions:
            details.append({
                "field": "statusCode",
                "operator": "equals",
                "expected": 200,
                "actual": status_code,
                "passed": status_code == 200,
            })
            return details

        for a in assertions:
            field = a.get("field", "")
            operator = a.get("operator", "")
            expected = a.get("expected")
            actual = self._extract_actual_value(field, status_code, body)
            passed = self._evaluate_assertion(operator, actual, expected)
            details.append({
                "field": field,
                "operator": operator,
                "expected": expected,
                "actual": actual,
                "passed": passed,
            })
        return details

    def _extract_actual_value(self, field: str, status_code: int, body: str):
        if field == "statusCode":
            return status_code
        if field == "responseBody":
            return body
        if field.startswith("$."):
            try:
                data = json.loads(body)
                return self._get_json_path(data, field[2:])
            except Exception:
                return None
        return None

    def _get_json_path(self, data, path: str):
        keys = path.split(".")
        current = data
        for key in keys:
            if isinstance(current, dict):
                current = current.get(key)
            elif isinstance(current, list):
                try:
                    current = current[int(key)]
                except (ValueError, IndexError):
                    return None
            else:
                return None
        return current

    def _evaluate_assertion(self, operator: str, actual, expected) -> bool:
        if actual is None:
            return False
        try:
            op = AssertionOperator(operator)
        except ValueError:
            return False

        if op == AssertionOperator.EQUALS:
            return str(actual) == str(expected)
        elif op == AssertionOperator.NOT_EQUALS:
            return str(actual) != str(expected)
        elif op == AssertionOperator.CONTAINS:
            return str(expected) in str(actual)
        elif op == AssertionOperator.NOT_CONTAINS:
            return str(expected) not in str(actual)
        elif op == AssertionOperator.GREATER_THAN:
            return float(actual) > float(expected)
        elif op == AssertionOperator.LESS_THAN:
            return float(actual) < float(expected)
        elif op == AssertionOperator.NOT_NULL:
            return actual is not None and str(actual) != ""
        elif op == AssertionOperator.IS_NULL:
            return actual is None or str(actual) == ""
        elif op == AssertionOperator.REGEX:
            import re
            return bool(re.match(str(expected), str(actual)))
        return False

    def _extract_variables(self, extract_vars: List[Dict], body: str):
        try:
            data = json.loads(body)
            for ev in extract_vars:
                name = ev.get("name")
                path = ev.get("path", "")
                if path.startswith("$."):
                    value = self._get_json_path(data, path[2:])
                    if value is not None:
                        self.context_vars[name] = str(value)
        except Exception:
            pass

    def _replace_vars(self, text: str) -> str:
        if not text:
            return text
        import re
        def replacer(match):
            key = match.group(1)
            return self.context_vars.get(key, match.group(0))
        return re.sub(r"\{\{(\w+)\}\}", replacer, text)

    def _replace_vars_dict(self, d: Dict) -> Dict:
        return {k: self._replace_vars(str(v)) for k, v in d.items()}


def load_cases_from_json(file_path: str) -> List[ApiCase]:
    """从JSON文件加载测试用例"""
    with open(file_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    cases = []
    for item in data:
        cases.append(ApiCase(
            id=item["id"],
            name=item["name"],
            method=item["method"],
            url=item["url"],
            headers=item.get("headers"),
            query_params=item.get("query_params"),
            body_type=item.get("body_type", "NONE"),
            body=item.get("body"),
            assertions=item.get("assertions"),
            extract_vars=item.get("extract_vars"),
            timeout=item.get("timeout", 10000),
        ))
    return cases


if __name__ == "__main__":
    # 示例: 执行一个登录接口测试
    engine = ApiTestEngine(base_url="http://localhost:8080/api")

    # 1. 登录获取token
    login_case = ApiCase(
        id=1,
        name="用户登录",
        method="POST",
        url="/auth/login",
        headers={"Content-Type": "application/json"},
        body_type="JSON",
        body=json.dumps({"username": "admin", "password": "admin123"}),
        assertions=[
            {"field": "statusCode", "operator": "equals", "expected": 200},
            {"field": "$.code", "operator": "equals", "expected": 200},
        ],
        extract_vars=[{"name": "token", "path": "$.data.token"}],
    )

    result = engine.execute(login_case)
    print(f"用例: {result.case_name}")
    print(f"结果: {result.result}")
    print(f"状态码: {result.response_status}")
    print(f"耗时: {result.response_time}ms")
    print(f"提取变量: {engine.context_vars}")
