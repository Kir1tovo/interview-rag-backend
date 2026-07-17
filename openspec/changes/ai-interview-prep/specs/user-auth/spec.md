## ADDED Requirements

### Requirement: 用户注册
系统 SHALL 允许用户通过用户名和密码注册账号。

#### Scenario: 注册成功
- **WHEN** 用户提交有效的用户名和密码
- **THEN** 系统创建用户账号并返回成功提示

#### Scenario: 用户名已存在
- **WHEN** 用户提交的用户名已被注册
- **THEN** 系统返回错误提示，告知用户名已存在

#### Scenario: 密码强度不足
- **WHEN** 用户提交的密码长度小于6位
- **THEN** 系统返回错误提示，要求密码至少6位

### Requirement: 用户登录
系统 SHALL 支持用户通过用户名和密码登录，登录成功后返回 JWT Token。

#### Scenario: 登录成功
- **WHEN** 用户输入正确的用户名和密码
- **THEN** 系统返回 JWT Token 和用户基本信息

#### Scenario: 密码错误
- **WHEN** 用户输入正确的用户名但密码错误
- **THEN** 系统返回错误提示，告知用户名或密码错误

#### Scenario: 用户不存在
- **WHEN** 用户输入的用户名不存在
- **THEN** 系统返回错误提示，告知用户名或密码错误

### Requirement: Token 鉴权
系统 SHALL 对需要登录的接口进行 Token 校验，无效或过期的 Token SHALL 被拒绝访问。

#### Scenario: 携带有效 Token 访问
- **WHEN** 请求头携带有效的 JWT Token
- **THEN** 系统正常处理请求并返回数据

#### Scenario: 未携带 Token
- **WHEN** 请求未携带 Authorization 头
- **THEN** 系统返回 401 未授权错误

#### Scenario: Token 过期
- **WHEN** 请求携带的 JWT Token 已过期
- **THEN** 系统返回 401 未授权错误

### Requirement: 用户信息管理
系统 SHALL 允许用户查看和修改个人信息。

#### Scenario: 获取个人信息
- **WHEN** 已登录用户请求获取个人信息
- **THEN** 系统返回用户的基本信息（用户名、密码，创建时间等）

#### Scenario: 修改密码
- **WHEN** 用户提交旧密码和新密码
- **THEN** 系统验证旧密码正确后更新为新密码
