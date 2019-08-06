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

### Hello World
```java
package your_package_path.noticlient;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.NotiReqPushEvents;
import com.gizwits.noti.noticlient.bean.req.body.AuthorizationData;
import com.gizwits.noti.noticlient.config.SnotiCallback;
import com.gizwits.noti.noticlient.config.SnotiConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotiClientApplicationTests {


    public static void main(String[] args) throws InterruptedException {
        String
                did = "did",
                mac = "mac",
                subKey = "sub_key",
                authId = "auth_id",
                authSecret = "auth_secret",
                productKey = "product_key";

        //登陆信息
        AuthorizationData authorizationData = new AuthorizationData()
                //默认为 Wi-Fi_GPRS, 可选 NB_IoT, LORA. 此配置让client自动根据协议类型发送对应的请求体
                .setProtocolType(ProtocolType.WiFi_GPRS)
                .setAuth_id(authId)
                .setAuth_secret(authSecret)
                .setSubkey(subKey)
                //订阅事件, 建议按照需求订阅
                .addEvents(NotiReqPushEvents.values())
                .setProduct_key(productKey);

        //初始化客户端
        OhMyNotiClient client = new OhMyNotiClientImpl()
                //设置snoti回调, 默认回调见SnotiCallback#identity
                .setCallback(SnotiCallback.identity())
                //加载登陆信息
                .addLoginAuthorizes(authorizationData)
                //snoti配置, 初始化配置见SnotiConfig
                .setSnotiConfig(new SnotiConfig());

        //启动客户端
        client.doStart();

        //模拟消费信息
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (; ; ) {
                JSONObject jsonObject = client.receiveMessage();
                log.info(jsonObject.toJSONString());
            }
        });
        //模拟消费信息end

        //透传控制
        Byte[] rawAttrs = new Byte[]{};
        client.control(productKey, mac, did, rawAttrs);
        //透传控制end

        //数据点控制
        Map<String, Object> jsonAttrs = new HashMap<>();
        jsonAttrs.put("data_point_code1", "data_point_value1");
        jsonAttrs.put("data_point_code2", "data_point_value2");
        jsonAttrs.put("data_point_code3", "data_point_value3");
        client.control(productKey, mac, did, jsonAttrs);
        //数据点控制end


        //重新加载登陆信息
        TimeUnit.SECONDS.sleep(30);
        client.reload(authorizationData);
        //重新加载登陆信息end

        //结束
        TimeUnit.SECONDS.sleep(30);
        client.doStop();
        //结束end
    }
}
```

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
  
## CHANGELOG  
1.8.8-RELEASE 发布日期: 2019-08-06  
1. GPS事件支持
        
1.8.7-RELEASE 发布日期: 2019-07-22        
1. 更新文档中maven仓库描述
2. 客户端生成, 优先使用epoll, 失败则使用 Java NIO
3. 监控指标支持    

BUG:   
1. 修复KV设备控制不支持自定义messageId的问题
2. 修复网络环境不好的情况下回复重复ack的问题   

1.8.6-RELEASE 发布日期: 2019-06-04      
1. 推送事件添加 msgId 字段  
2. 数据点控制支持自定义msgId
3. 设备控制指令默认msgId的生成规则与云端统一
4. 支持读取控制回调消息  

BUG: 
1. 修复1.8.5-RELEASE有可能控制无效的问题


1.8.5-RELEASE 发布日期: 2019-03-05  
1. 支持 LoRa 产品控制
2. client 支持 tryControl 
3. 更新文档

1.8.4-RELEASE 发布日期: 2018-12-28  
1. 完善控制

1.8.3-RELEASE 发布日期: 2018-12-05  
BUG: 修复了新建连接回复旧的ack导致服务端不再推送消息的问题

1.8.2-RELEASE 发布日期: 2018-11-20  
1. ack失败重试
2. SnotiConfig支持更多的配置

1.8.1 发布日期: 2018-11-06  
1. 增加push-event-message计数(从SnotiConfig中开启)
2. 增加消息活跃检测

1.8.0 发布日期: 2018-11-05  
1. 修复发送消息bug

1.7.9 发布日期: 2018-11-05  
1. 优化ack回复
2. 优化重连
3. 新增SnotiConfig以自定义snoti配置
4. 标志过期方法

1.7.8 发布日期: 2018-10-15
1. 修改发送,接收队列的容量
2. 优化ack回复
3. 优化bootstrap配置参数

1.7.7 发布日期: 2018-09-28  
BUG: 修复了获取消息返回null的bug

1.7.6 发布日期: 2018-09-26
1. Linux环境下优先使用epoll

1.7.5 发布日期: 2018-09-18
1. 上线，数据点添加imsi, iccid字段(NB-IoT)

1.7.4 发布日期: 2018-09-11
1. 登陆成功后才开始处理控制请求
2. 优化了ackMessage生成方法
3. 推送事件接收队列余量预警（少于4%打印告警日志）

1.7.3 发布日期: 2018-09-08
1. 支持设备控制事件
2. 修复一些bug

1.7.2 发布日期: 2018-01-01
1. 项目初始化

## 建议
有任何使用问题或者建议请联系 `jcliu@gizwits.com`
