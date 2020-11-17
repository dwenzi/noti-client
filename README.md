# noti-client
>让gizwits-snoti的使用更加简单. 

## 依赖
1. Java8+
2. [Netty](https://netty.io)
3. [fastjson](https://github.com/alibaba/fastjson)
4. [机智云SNoti API](http://docs.gizwits.com/zh-cn/Cloud/NotificationAPI.html)

## 快速上手

### 设置maven仓库
在`pom.xml`添加仓库地址
```xml
<repositories>
    ...
    <repository>
        <id>archiva.general</id>
        <name>Gizwits General Repo</name>
        <url>https://archiva.gizwits.com/repository/general/</url>
    </repository>
    ...
</repositories>
```

### 添加maven依赖
在`pom.xml`添加仓库依赖, 其中版本号在`changelog`中可查.
```xml
<dependencies>
    ...
    <dependency>
        <groupId>com.gizwits.noti</groupId>
        <artifactId>noti-client</artifactId>
        <version>${snoti-client.version}</version>
    </dependency>
    ...
</dependencies>
```



### 结合 Spring Boot 使用

如果你正在使用 Spring Boot, 可以参考 [snoti-demo](https://github.com/smallCC/snoti-demo) .



### 使用 
1. 简单使用请参考类 "com.gizwits.noti.OhMyNotiClientQuickStart"  
2. 登陆、订阅、控制、接收消息等可参考对应单元测试

## TODO
- [x] 断开重连
- [x] 控制设备
  - [x] NB-IoT设备控制
  - [x] Wi-Fi,GPRS设备控制
  - [x] LORA设备控制
- [x] 事件
  - [x] 控制回调
  - [x] 数据点改变
  - [x] 重置
  - [x] 告警
  - [x] 故障
  - [x] 绑定
  - [x] 解绑
  - [x] 下线
  - [x] 上线
  - [x] 数据点
  - [x] 透传
  - [x] 中控添加子设备
  - [x] 中控删除子设备
  - [x] GPS事件
  - [x] LBS事件
  

## 建议
有任何使用问题或者建议请联系 `jcliu@gizwits.com`
