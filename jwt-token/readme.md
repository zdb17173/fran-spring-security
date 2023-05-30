

### 登录成功、登录失败、登出
- login 
  - 接口返回采用json格式返回
  - 登录成功后，返回用户信息 & token
  - 登录成功后将token存储至JwtTokenStoreService
- logout 
  - 接口返回采用json格式返回
  - 登出成功后，从JwtTokenStoreService中将token摘除
- login error 
  - 接口返回采用json格式返回


### jwtToken管理
- jwtToken生成：JwtTokenService
- jwtToken存储（内存存储、redis存储、none不存储）：JwtTokenStoreService
- jwtToken SecurityContext上下文管理，从request中获取token
后转换成UsernamePasswordAuthenticationToken。


### jwtToken存储三种类型说明，参考JwtTokenStoreService
- memory 基于hashmap进行token存储，存在单机问题。缓存用户信息，可不从DB中获取，
  但是修改用户权限后需要登出
- redis 基于redis进行token存储，不存在单机问题。缓存用户信息，可不从DB中获取，
  但是修改用户权限后需要登出
- none 不存储，无单机问题，但是登出的token失效、token踢出等操作无法实现。需要前端
  自己进行处理（logout后将前端缓存中的token置空）


### 覆盖UsernamePasswordAuthenticationFilter
原UsernamePasswordAuthenticationFilter是基于form表单中的username与password
获取登录用户信息，覆盖的JwtJsonUsernamePasswordAuthenticationFilter可从
json中获取username与password信息


### 踢人功能及配置，单个用户ID仅维持一个token
todo


### jwt的两步验证功能
验证码生成后，根据存储类型，存储在内存或redis中。系统会以验证码本身为key，在redis
中缓存一份验证码的值，当用户输入的验证码在缓存中存在时，则通过（粗略版）

复杂版应在生成验证码图片时，返回图片 + 临时token，验证时将临时token传入进行验证
（未实现）

