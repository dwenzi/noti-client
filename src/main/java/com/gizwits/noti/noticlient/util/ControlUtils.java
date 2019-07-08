package com.gizwits.noti.noticlient.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.NotiReqCommandType;
import com.gizwits.noti.noticlient.bean.req.NotiReqControlType;
import com.gizwits.noti.noticlient.bean.req.SourceCategory;
import com.gizwits.noti.noticlient.bean.req.body.AbstractCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.ControlReqCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.InternalControlReqCommandBody;
import com.gizwits.noti.noticlient.enums.ProtocolType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * The type Control utils.
 *
 * @author Jcxcc
 * @since 1.0
 */
public class ControlUtils {

    /**
     * 透传控制指令前缀
     */
    private final static byte RAW_PREFIX = (byte) 0X05;
    private final static String
            UUID_SEPARATOR = "-",
            BASE64_SUFFIX = "==";

    /**
     * 获取控制指令messageId
     * <p>
     * snoti客户端控制接口msg_id生成规则,
     * 取UUID 16位 然后base64编码后去除最后两个=号 做为msg_id,
     * 由于平台都是这个规则,
     * 所以我们需要兼容平台msg_id
     *
     * @return the uuid message id
     */
    private static String getCtrlMsgId() {
        String source = UUID.randomUUID().toString().replaceAll(UUID_SEPARATOR, StringUtils.EMPTY).substring(0, 16);
        String encode = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
        return StringUtils.endsWith(encode, BASE64_SUFFIX) ? StringUtils.substringBeforeLast(encode, BASE64_SUFFIX) : encode;
    }

    private static ControlReqCommandBody.ControlBody getControlBody(String productKey, String mac, String did, NotiReqControlType controlType) {
        return
                new ControlReqCommandBody.ControlBody()
                        .setCmd(controlType)
                        .setPayload(new ControlReqCommandBody.ControlBody.Payload()
                                .setDid(did)
                                .setMac(mac)
                                .setProductKey(productKey));
    }

    /**
     * Switch control abstract command body.
     *
     * @param msgId        the msg id
     * @param productKey   the product key
     * @param mac          the mac
     * @param did          the did
     * @param dataPoint    the data point
     * @param protocolType the protocol type
     * @return the abstract command body
     */
    public static AbstractCommandBody switchControl(String msgId, String productKey, String mac, String did, Map<String, Object> dataPoint, ProtocolType protocolType) {
        switch (protocolType) {
            case NB_IoT:
                InternalControlReqCommandBody nbBody = getInternalControlReqCommandBody(msgId, productKey, mac, did, dataPoint);
                nbBody.setSource(SourceCategory.huawei);
                nbBody.setCmd(NotiReqCommandType.remote_control_nb_req);
                return nbBody;
            case LORA:
                InternalControlReqCommandBody loraBody = getInternalControlReqCommandBody(msgId, productKey, mac, did, dataPoint);
                loraBody.setCmd(NotiReqCommandType.remote_control_lora_req);
                return loraBody;

            /**
             * lora平台为支持, 2018-08-09 18:04:30
             * 默认用WiFi GRPS协议
             */
            case WiFi_GPRS:
            default:
                ControlReqCommandBody.ControlBody controlBody = getControlBody(productKey, mac, did, NotiReqControlType.write_attrs);
                controlBody.setPayload(controlBody.getPayload().setAttrs(JSONObject.parseObject(JSONObject.toJSONString(dataPoint))));
                return new ControlReqCommandBody()
                        .setControlBody(Collections.singletonList(controlBody))
                        .setMsgId(getCtrlMsgId());
        }
    }

    /**
     * 获取内部控制请求体
     * <p>
     * lora， nb的控制为内部支持
     * 需要使用内部请求体
     *
     * @param productKey
     * @param mac
     * @param did
     * @param dataPoint
     * @return
     */
    private static InternalControlReqCommandBody getInternalControlReqCommandBody(String msgId, String productKey, String mac, String did, Map<String, Object> dataPoint) {
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(dataPoint, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty));

        InternalControlReqCommandBody body = new InternalControlReqCommandBody(productKey, mac, did, json);
        if (StringUtils.isNoneBlank(msgId)) {
            body.setMsgId(msgId);
        } else {
            body.setMsgId(getCtrlMsgId());
        }

        return body;
    }


    /**
     * Switch control abstract command body.
     * 当前nb-IoT不支持数据点
     *
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param raw        the raw
     * @return the abstract command body
     */
    public static AbstractCommandBody switchControl(String productKey, String mac, String did, Object raw) {
        byte[] rawByteArray = null;
        int[] rawIntArray = null;
        if (raw instanceof byte[]) {
            rawByteArray = (byte[]) raw;
        } else if (raw instanceof Byte[]) {
            Byte[] rawArray = (Byte[]) raw;
            rawByteArray = ArrayUtils.toPrimitive(rawArray);
        } else if (raw instanceof int[]) {
            rawIntArray = (int[]) raw;
        } else if (raw instanceof Integer[]) {
            Integer[] rawArray = (Integer[]) raw;
            rawIntArray = ArrayUtils.toPrimitive(rawArray);
        } else if (raw instanceof InputStream) {
            try (InputStream is = (InputStream) raw; ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] bytes = new byte[1024];
                while (is.read(bytes) != -1) {
                    baos.write(bytes);
                }
                rawByteArray = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            rawByteArray = raw.toString().getBytes();
        }
        if (rawIntArray != null) {
            if (rawIntArray[0] != RAW_PREFIX) {
                int[] target = new int[rawIntArray.length + 1];
                target[0] = RAW_PREFIX;
                System.arraycopy(rawIntArray, 0, target, 1, rawIntArray.length);
                rawIntArray = target;
            }
        } else if (rawByteArray != null) {
            int rawIntArrayIndex = 0;
            if (rawByteArray[0] != RAW_PREFIX) {
                rawIntArray = new int[rawByteArray.length + 1];
                rawIntArray[rawIntArrayIndex++] = RAW_PREFIX;
            } else {
                rawIntArray = new int[rawByteArray.length];
            }
            for (byte b : rawByteArray) {
                rawIntArray[rawIntArrayIndex++] = b & 0xff;
            }
        }
        ControlReqCommandBody.ControlBody controlBody = getControlBody(productKey, mac, did, NotiReqControlType.write);
        controlBody.setPayload(controlBody.getPayload().setRaw(rawIntArray));
        return new ControlReqCommandBody()
                .setControlBody(Collections.singletonList(controlBody))
                .setMsgId(getCtrlMsgId());
    }
}
