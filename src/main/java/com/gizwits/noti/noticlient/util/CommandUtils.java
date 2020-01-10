package com.gizwits.noti.noticlient.util;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import com.gizwits.noti.noticlient.bean.resp.body.AbstractPushEventBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gizwits.noti.noticlient.bean.SnotiConstants.EVENT_TYPE_KEY;
import static com.gizwits.noti.noticlient.bean.SnotiConstants.STR_CMD;

/**
 * The type Command utils.
 *
 * @author Jcxcc
 * @since 1.0
 */
@Slf4j
public class CommandUtils {

    private static final Map<String, NotiGeneralCommandType> REQ_CMD_MAP;
    private static final Map<String, NotiRespPushEvents> RESP_EVENT_MAP;

    static {
        REQ_CMD_MAP = Arrays.stream(NotiGeneralCommandType.values())
                .collect(Collectors.toMap(NotiGeneralCommandType::getCode, Function.identity(), (oldVal, newVal) -> newVal));
        log.info("初始化请求命令路由成功.");

        RESP_EVENT_MAP = Arrays.stream(NotiRespPushEvents.values())
                .collect(Collectors.toMap(NotiRespPushEvents::getCode, Function.identity(), (oldVal, newVal) -> newVal));
        log.info("初始化响应命令路由成功.");
    }

    /**
     * 根据code获取noti返回的推送事件类型
     *
     * @param code the code
     * @return the resp event
     */
    public static NotiRespPushEvents getResEvent(String code) {
        return RESP_EVENT_MAP.getOrDefault(code, NotiRespPushEvents.INVALID);
    }

    /**
     * 根据code获取noti请求命令
     *
     * @param code the code
     * @return req cmd
     */
    public static NotiGeneralCommandType getReqCmd(String code) {
        return REQ_CMD_MAP.getOrDefault(code, NotiGeneralCommandType.invalid_msg);
    }

    /**
     * 获取ack回复消息主体
     *
     * @param deliveryId the delivery id
     * @return event ack message
     */
    public static String getEventAckMessage(Object deliveryId) {
        if (Objects.isNull(deliveryId)) {
            return StringUtils.LF;
        }

        return "{\"delivery_id\":" +
                deliveryId
                + ",\"cmd\":\"event_ack\"}\n";
    }

    /**
     * 获取推送事件的代号
     * 此处设备控制回调消息当作推送
     *
     * @param json the json
     * @return the push event code
     */
    public static String getPushEventCode(JSONObject json) {
        String eventCode = StringUtils.defaultString(StringUtils.defaultString(json.getString(EVENT_TYPE_KEY), json.getString(STR_CMD)));
        return RESP_EVENT_MAP.containsKey(eventCode) ? eventCode : NotiRespPushEvents.INVALID.getCode();
    }

    /**
     * 解析推送事件
     * <p>
     * 从客户端获取到json后, 可以使用该方法将json转化成JavaBean
     * 如:
     * 1. 设备上报数据点事件 {@link com.gizwits.noti.noticlient.bean.resp.body.StatusKvEventBody}
     * 2. 设备上线事件 {@link com.gizwits.noti.noticlient.bean.resp.body.OnLineEventBody}
     * 3. 设备重置事件 {@link com.gizwits.noti.noticlient.bean.resp.body.ResetEventBody}
     * ...
     *
     * @param <T>   the type parameter
     * @param json  the json
     * @param clazz the clazz
     * @return the t
     */
    public static <T extends AbstractPushEventBody> T parsePushEvent(JSONObject json, Class<T> clazz) {
        return json.toJavaObject(clazz);
    }

    /**
     * 十六进制字符串转int数组
     *
     * @param hex16Str the hex 16 str
     * @return the int [ ]
     */
    public static int[] hexStrToIntArr(String hex16Str) {
        byte[] bytes = hexStrToBytes(hex16Str);
        int[] ret = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ret[i] = (bytes[i]) & 0xff;
        }
        return ret;
    }

    /**
     * 十六进制字符串转byte数组
     *
     * @param hex16Str the hex 16 str
     * @return the byte [ ]
     */
    public static byte[] hexStrToBytes(String hex16Str) {
        int len = hex16Str.length();
        byte[] ba = new byte[len / 2];

        for (int i = 0; i < ba.length; i++) {
            int j = i * 2;
            int t = Integer.parseInt(hex16Str.substring(j, j + 2), 16);
            byte b = (byte) (t & 0xFF);
            ba[i] = b;
        }
        return ba;
    }
}
