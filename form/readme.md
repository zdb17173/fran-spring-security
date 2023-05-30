
### 两步验证功能
1. 两步验证filter 
2. /code/image 生成验证码
3. 验证失败session中放入提示信息，模板中显示验证码异常

### 基础的登录功能
1. 访问的拦截与放行 /login & /loginApi & /code/image不需要验证
2. 获取用户密码的字段
3. 密码加密工具SecurityUtils

### 接口&服务的访问控制
1. @EnableGlobalMethodSecurity 开启secured注解，基于用户角色进行拦截
2. http.authorizeRequests().antMatchers().permitAll() 基于配置的访问控制

