-- ============================================================
-- TestHub 企业级智能测试平台 - 数据库初始化脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS testhub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE testhub;

-- -----------------------------------------------------------
-- 1. 用户与权限模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name   VARCHAR(50)  COMMENT '真实姓名',
    email       VARCHAR(100) COMMENT '邮箱',
    phone       VARCHAR(20)  COMMENT '手机号',
    avatar      VARCHAR(255) COMMENT '头像URL',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    deleted     TINYINT     NOT NULL DEFAULT 0,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- 2. 测试用例管理模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS test_case_module;
CREATE TABLE test_case_module (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模块ID',
    name        VARCHAR(100) NOT NULL COMMENT '模块名称',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父模块ID',
    project_id  BIGINT       NOT NULL COMMENT '所属项目ID',
    sort        INT          DEFAULT 0 COMMENT '排序',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用例模块表';

DROP TABLE IF EXISTS test_case;
CREATE TABLE test_case (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用例ID',
    case_no      VARCHAR(50)  NOT NULL UNIQUE COMMENT '用例编号 TC-00001',
    title        VARCHAR(200) NOT NULL COMMENT '用例标题',
    module_id    BIGINT       COMMENT '所属模块ID',
    project_id   BIGINT       NOT NULL COMMENT '所属项目ID',
    case_type    VARCHAR(20)  NOT NULL DEFAULT 'FUNCTIONAL' COMMENT '用例类型: FUNCTIONAL-功能 API-接口 UI-界面 PERFORMANCE-性能 SECURITY-安全',
    priority     VARCHAR(10)  NOT NULL DEFAULT 'P2' COMMENT '优先级 P0-P3',
    precondition TEXT         COMMENT '前置条件',
    steps        TEXT         COMMENT '测试步骤(JSON数组)',
    expected     TEXT         COMMENT '预期结果',
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT-草稿 REVIEW-待评审 APPROVED-已评审 OBSOLETE-废弃',
    tags         VARCHAR(200) COMMENT '标签(逗号分隔)',
    creator_id   BIGINT       NOT NULL COMMENT '创建人ID',
    deleted      TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_module (module_id),
    INDEX idx_project (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- -----------------------------------------------------------
-- 3. 接口自动化测试模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS api_environment;
CREATE TABLE api_environment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '环境名称',
    base_url    VARCHAR(500) NOT NULL COMMENT '基础URL',
    headers     TEXT         COMMENT '公共请求头(JSON)',
    variables   TEXT         COMMENT '环境变量(JSON)',
    project_id  BIGINT       NOT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试环境表';

DROP TABLE IF EXISTS api_case;
CREATE TABLE api_case (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '接口用例名称',
    module_id       BIGINT       COMMENT '模块ID',
    project_id      BIGINT       NOT NULL,
    env_id          BIGINT       COMMENT '默认环境ID',
    method          VARCHAR(10)  NOT NULL COMMENT '请求方法 GET/POST/PUT/DELETE',
    url             VARCHAR(500) NOT NULL COMMENT '请求URL(支持变量)',
    headers         TEXT         COMMENT '请求头(JSON)',
    query_params    TEXT         COMMENT 'Query参数(JSON)',
    body_type       VARCHAR(20)  DEFAULT 'NONE' COMMENT '请求体类型 NONE/FORM/JSON/RAW',
    body            TEXT         COMMENT '请求体内容',
    assertions      TEXT         COMMENT '断言规则(JSON数组)',
    extract_vars    TEXT         COMMENT '提取变量(JSON数组)',
    timeout         INT          DEFAULT 10000 COMMENT '超时时间ms',
    tags            VARCHAR(200),
    status          VARCHAR(20)  DEFAULT 'ACTIVE',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试用例表';

DROP TABLE IF EXISTS api_test_suite;
CREATE TABLE api_test_suite (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200) NOT NULL COMMENT '测试套件名称',
    project_id   BIGINT       NOT NULL,
    description  VARCHAR(500),
    case_ids     TEXT         COMMENT '用例ID列表(逗号分隔)',
    run_order    VARCHAR(20)  DEFAULT 'SEQUENTIAL' COMMENT '执行顺序 SEQUENTIAL/PARALLEL',
    deleted      TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试套件表';

DROP TABLE IF EXISTS api_test_record;
CREATE TABLE api_test_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    suite_id        BIGINT       COMMENT '套件ID',
    case_id         BIGINT       COMMENT '单条用例ID',
    project_id      BIGINT       NOT NULL,
    exec_type       VARCHAR(20)  NOT NULL COMMENT '执行类型 SINGLE/SUITE',
    total_count     INT          DEFAULT 0 COMMENT '总用例数',
    pass_count      INT          DEFAULT 0 COMMENT '通过数',
    fail_count      INT          DEFAULT 0 COMMENT '失败数',
    skip_count      INT          DEFAULT 0 COMMENT '跳过数',
    status          VARCHAR(20)  COMMENT '整体结果 PASS/FAIL/RUNNING',
    start_time      DATETIME     COMMENT '开始时间',
    end_time        DATETIME     COMMENT '结束时间',
    duration_ms     BIGINT       COMMENT '耗时ms',
    trigger_type    VARCHAR(20)  DEFAULT 'MANUAL' COMMENT '触发方式 MANUAL/SCHEDULE/CI',
    operator_id     BIGINT       COMMENT '操作人ID',
    report_path     VARCHAR(500) COMMENT '报告路径',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_suite (suite_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试执行记录表';

DROP TABLE IF EXISTS api_test_result;
CREATE TABLE api_test_result (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id       BIGINT       NOT NULL COMMENT '执行记录ID',
    case_id         BIGINT       NOT NULL COMMENT '用例ID',
    case_name       VARCHAR(200) COMMENT '用例名称',
    request_method  VARCHAR(10),
    request_url     TEXT,
    request_headers TEXT,
    request_body    TEXT,
    response_status INT          COMMENT '响应状态码',
    response_headers TEXT,
    response_body   MEDIUMTEXT,
    response_time   BIGINT       COMMENT '响应时间ms',
    result          VARCHAR(20)  COMMENT 'PASS/FAIL',
    error_message   TEXT         COMMENT '错误信息',
    assertions      TEXT         COMMENT '断言详情(JSON)',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口测试结果明细表';

-- -----------------------------------------------------------
-- 4. UI 自动化测试模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS ui_test_case;
CREATE TABLE ui_test_case (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    project_id   BIGINT       NOT NULL,
    browser      VARCHAR(20)  DEFAULT 'chrome' COMMENT '浏览器 chrome/firefox/edge',
    base_url     VARCHAR(500) COMMENT '基础URL',
    steps        TEXT         COMMENT '步骤(JSON)',
    assertions   TEXT         COMMENT '断言(JSON)',
    tags         VARCHAR(200),
    status       VARCHAR(20)  DEFAULT 'ACTIVE',
    deleted      TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UI测试用例表';

-- -----------------------------------------------------------
-- 5. 缺陷管理模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS bug;
CREATE TABLE bug (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_no          VARCHAR(50)  NOT NULL UNIQUE COMMENT '缺陷编号 BUG-00001',
    title           VARCHAR(200) NOT NULL COMMENT '缺陷标题',
    project_id      BIGINT       NOT NULL,
    module_id       BIGINT       COMMENT '模块ID',
    severity        VARCHAR(10)  NOT NULL COMMENT '严重程度 BLOCKER/CRITICAL/MAJOR/MINOR/TRIVIAL',
    priority        VARCHAR(10)  NOT NULL COMMENT '优先级 P0-P3',
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN/ASSIGNED/FIXED/VERIFIED/CLOSED/REOPENED',
    bug_type        VARCHAR(30)  COMMENT '缺陷类型 FUNCTION/DATA/UI/PERFORMANCE/SECURITY/COMPATIBILITY',
    description     TEXT         COMMENT '缺陷描述',
    reproduce_steps TEXT         COMMENT '复现步骤',
    expected_result TEXT         COMMENT '预期结果',
    actual_result   TEXT         COMMENT '实际结果',
    attachments     VARCHAR(1000) COMMENT '附件路径(JSON)',
    reporter_id     BIGINT       NOT NULL COMMENT '报告人ID',
    assignee_id     BIGINT       COMMENT '处理人ID',
    fix_version     VARCHAR(50)  COMMENT '修复版本',
    found_version   VARCHAR(50)  COMMENT '发现版本',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project (project_id),
    INDEX idx_status (status),
    INDEX idx_assignee (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺陷表';

DROP TABLE IF EXISTS bug_comment;
CREATE TABLE bug_comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id      BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    content     TEXT   NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bug (bug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺陷评论表';

-- -----------------------------------------------------------
-- 6. 项目管理模块
-- -----------------------------------------------------------
DROP TABLE IF EXISTS project;
CREATE TABLE project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '项目名称',
    code        VARCHAR(50)  NOT NULL UNIQUE COMMENT '项目编码',
    description VARCHAR(500),
    owner_id    BIGINT       COMMENT '负责人ID',
    status      VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ARCHIVED',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- -----------------------------------------------------------
-- 7. AI 助手记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS ai_chat_record;
CREATE TABLE ai_chat_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    session_id  VARCHAR(64)  COMMENT '会话ID',
    role        VARCHAR(20)  NOT NULL COMMENT 'user/assistant/system',
    content     TEXT         NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@testhub.com', 1),
('tester', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试工程师', 'tester@testhub.com', 1);

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'ADMIN', '拥有所有权限'),
('测试经理', 'TEST_MANAGER', '测试管理权限'),
('测试工程师', 'TESTER', '测试执行权限'),
('开发工程师', 'DEVELOPER', '开发查看权限');

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 3);

-- 初始化项目
INSERT INTO project (name, code, description, owner_id) VALUES
('TestHub平台自测项目', 'TESTHUB', 'TestHub平台自身的测试项目', 1),
('电商系统测试项目', 'ECOMMERCE', '电商核心业务系统测试', 1);

-- 初始化模块
INSERT INTO test_case_module (name, parent_id, project_id, sort) VALUES
('用户管理', 0, 1, 1),
('登录注册', 1, 1, 1),
('权限管理', 1, 1, 2),
('订单管理', 0, 2, 1),
('商品管理', 0, 2, 2);

-- 初始化测试用例示例
INSERT INTO test_case (case_no, title, module_id, project_id, case_type, priority, precondition, steps, expected, status, creator_id) VALUES
('TC-00001', '验证用户正常登录', 2, 1, 'FUNCTIONAL', 'P0', '用户已注册',
 '[{"step":"打开登录页","data":"-"},{"step":"输入正确用户名密码","data":"admin/123456"},{"step":"点击登录","data":"-"}]',
 '登录成功，跳转到首页', 'APPROVED', 1),
('TC-00002', '验证密码错误登录', 2, 1, 'FUNCTIONAL', 'P1', '用户已注册',
 '[{"step":"打开登录页","data":"-"},{"step":"输入错误密码","data":"admin/wrong"},{"step":"点击登录","data":"-"}]',
 '提示密码错误', 'APPROVED', 1),
('TC-00003', '获取用户列表接口', NULL, 1, 'API', 'P0', '-',
 '[{"step":"发送GET请求","data":"/api/users"}]',
 '返回200,用户列表', 'APPROVED', 1);

-- 初始化接口测试用例示例
INSERT INTO api_case (name, project_id, method, url, headers, body, body_type, assertions) VALUES
('获取用户列表', 1, 'GET', '/api/users', '{"Content-Type":"application/json"}', NULL, 'NONE',
 '[{"field":"code","operator":"equals","expected":200},{"field":"$.data","operator":"notNull"}]'),
('用户登录', 1, 'POST', '/api/auth/login', '{"Content-Type":"application/json"}', '{"username":"admin","password":"admin123"}', 'JSON',
 '[{"field":"code","operator":"equals","expected":200}]');

-- 初始化缺陷示例
INSERT INTO bug (bug_no, title, project_id, severity, priority, status, bug_type, description, reporter_id) VALUES
('BUG-00001', '登录页面密码框未做长度校验', 1, 'MAJOR', 'P2', 'OPEN', 'FUNCTION', '密码框可以输入超过100个字符，提交后后端报错', 2),
('BUG-00002', '用户列表分页查询第10页数据为空', 1, 'MINOR', 'P3', 'FIXED', 'DATA', '当数据不足10页时，查询第10页应返回空列表且不报错', 2);
