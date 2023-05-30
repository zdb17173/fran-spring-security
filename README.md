# fran-spring-security

## from
基于表单登录的模式，使用session保存信息

功能
- 两步验证（验证码）
- 防火墙设置


## token
基于jwtoken保存用户登录信息，使用redis、memory等方式存储状态。

功能
- 基于jwt的token生成
- 基于redis的用户登录信息存储
- 两步验证（验证码）
- 基于application/json的各类接口