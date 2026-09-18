"""
TestHub UI 自动化测试引擎
基于 Selenium 4, 支持步骤化执行、断言验证、截图
"""
import time
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, field
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException


@dataclass
class UiStep:
    action: str  # open/click/input/select/assert/wait/screenshot
    target: Optional[str] = None  # 定位器, 如 "id:username" 或 "xpath://input"
    value: Optional[str] = None
    timeout: int = 10


@dataclass
class UiTestCase:
    id: int
    name: str
    browser: str = "chrome"
    base_url: str = ""
    steps: List[UiStep] = field(default_factory=list)


class UiTestEngine:
    """UI测试执行引擎"""

    LOCATOR_MAP = {
        "id": By.ID,
        "name": By.NAME,
        "class": By.CLASS_NAME,
        "css": By.CSS_SELECTOR,
        "xpath": By.XPATH,
        "tag": By.TAG_NAME,
        "link": By.LINK_TEXT,
    }

    def __init__(self, headless: bool = True, screenshot_dir: str = "./screenshots"):
        self.headless = headless
        self.screenshot_dir = screenshot_dir
        self.driver = None

    def _init_driver(self, browser: str):
        if browser.lower() == "chrome":
            options = webdriver.ChromeOptions()
            if self.headless:
                options.add_argument("--headless=new")
            options.add_argument("--no-sandbox")
            options.add_argument("--disable-dev-shm-usage")
            options.add_argument("--window-size=1920,1080")
            self.driver = webdriver.Chrome(options=options)
        elif browser.lower() == "firefox":
            options = webdriver.FirefoxOptions()
            if self.headless:
                options.add_argument("-headless")
            self.driver = webdriver.Firefox(options=options)
        else:
            raise ValueError(f"不支持的浏览器: {browser}")

        self.driver.implicitly_wait(5)

    def _parse_locator(self, target: str):
        """解析定位器, 格式: type:value"""
        if ":" not in target:
            return By.CSS_SELECTOR, target
        loc_type, value = target.split(":", 1)
        by = self.LOCATOR_MAP.get(loc_type.lower())
        if by is None:
            return By.CSS_SELECTOR, target
        return by, value

    def execute(self, case: UiTestCase) -> Dict[str, Any]:
        """执行UI测试用例"""
        result = {
            "case_id": case.id,
            "case_name": case.name,
            "result": "PASS",
            "steps": [],
            "error": "",
            "duration_ms": 0,
        }
        start = time.time()

        try:
            self._init_driver(case.browser)
            step_results = []

            for step in case.steps:
                step_result = self._execute_step(step)
                step_results.append(step_result)
                if not step_result["passed"]:
                    result["result"] = "FAIL"
                    result["error"] = step_result.get("error", "")
                    break

            result["steps"] = step_results
        except Exception as e:
            result["result"] = "FAIL"
            result["error"] = str(e)
        finally:
            if self.driver:
                self.driver.quit()

        result["duration_ms"] = int((time.time() - start) * 1000)
        return result

    def _execute_step(self, step: UiStep) -> Dict[str, Any]:
        """执行单个步骤"""
        step_result = {
            "action": step.action,
            "target": step.target,
            "passed": True,
            "error": "",
        }

        try:
            if step.action == "open":
                url = step.value if step.value.startswith("http") else \
                    (self.driver.current_url.rstrip("/") if False else "")
                # 简化处理
                self.driver.get(step.value)

            elif step.action == "click":
                by, value = self._parse_locator(step.target)
                element = WebDriverWait(self.driver, step.timeout).until(
                    EC.element_to_be_clickable((by, value))
                )
                element.click()

            elif step.action == "input":
                by, value = self._parse_locator(step.target)
                element = WebDriverWait(self.driver, step.timeout).until(
                    EC.presence_of_element_located((by, value))
                )
                element.clear()
                element.send_keys(step.value)

            elif step.action == "select":
                from selenium.webdriver.support.ui import Select
                by, value = self._parse_locator(step.target)
                element = self.driver.find_element(by, value)
                Select(element).select_by_visible_text(step.value)

            elif step.action == "assert":
                by, value = self._parse_locator(step.target)
                WebDriverWait(self.driver, step.timeout).until(
                    EC.presence_of_element_located((by, value))
                )
                element = self.driver.find_element(by, value)
                if step.value and step.value not in element.text:
                    step_result["passed"] = False
                    step_result["error"] = f"断言失败: 期望包含 '{step.value}', 实际为 '{element.text}'"

            elif step.action == "wait":
                time.sleep(float(step.value or 1))

            elif step.action == "screenshot":
                import os
                os.makedirs(self.screenshot_dir, exist_ok=True)
                filename = f"{self.screenshot_dir}/step_{int(time.time())}.png"
                self.driver.save_screenshot(filename)
                step_result["screenshot"] = filename

        except (TimeoutException, NoSuchElementException) as e:
            step_result["passed"] = False
            step_result["error"] = str(e)

        return step_result


if __name__ == "__main__":
    # 示例: 测试登录页面
    case = UiTestCase(
        id=1,
        name="登录功能测试",
        browser="chrome",
        base_url="http://localhost:5173",
        steps=[
            UiStep(action="open", value="http://localhost:5173/login"),
            UiStep(action="input", target="css:input[placeholder='用户名']", value="admin"),
            UiStep(action="input", target="css:input[placeholder='密码']", value="admin123"),
            UiStep(action="click", target="css:button[type='button']"),
            UiStep(action="wait", value="2"),
            UiStep(action="assert", target="css:.page-title", value="工作台"),
        ],
    )

    engine = UiTestEngine(headless=True)
    result = engine.execute(case)
    print(f"用例: {result['case_name']}")
    print(f"结果: {result['result']}")
    print(f"耗时: {result['duration_ms']}ms")
