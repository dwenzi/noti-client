package com.gizwits.noti.noticlient.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.NotiCtrlDTO;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.req.NotiReqControlType;
import com.gizwits.noti.noticlient.bean.req.SourceCategory;
import com.gizwits.noti.noticlient.bean.req.body.*;
import com.gizwits.noti.noticlient.bean.resp.body.ControlV2CallbackEventBody;
import com.gizwits.noti.noticlient.enums.ProtocolType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
     * Parse ctrl abstract command body.
     *
     * @param msgId        the msg id
     * @param protocolType the protocol type
     * @param ctrlDTOs     the ctrl dt os
     * @return the abstract command body
     */
    public static AbstractCommandBody parseCtrl(String msgId, ProtocolType protocolType, NotiCtrlDTO... ctrlDTOs) {
        checkArgument(ctrlDTOs != null, "控制参数不能为空");

        if (ctrlDTOs[0].getData() instanceof Map) {
            return parseKvCtrl(msgId, protocolType, ctrlDTOs);
        } else {
            return parseRawCtrl(msgId, ctrlDTOs);
        }
    }

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

    /**
     * 获取控制body
     *
     * @param productKey
     * @param mac
     * @param did
     * @param controlType
     * @return
     */
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
     * 透传
     *
     * @param msgId    the msg id
     * @param ctrlDTOS the ctrl dtos
     * @return the abstract command body
     */
    private static AbstractCommandBody parseRawCtrl(String msgId, NotiCtrlDTO... ctrlDTOS) {
        List<ControlReqCommandBody.ControlBody> controlBodies = Arrays.stream(ctrlDTOS)
                .map(it -> {
                    String productKey = it.getProductKey();
                    String mac = it.getMac();
                    String did = it.getDid();
                    Object raw = it.getData();
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
                    return controlBody;
                })
                .collect(Collectors.toList());

        return new ControlReqCommandBody()
                .setControlBody(controlBodies)
                .setMsgId(StringUtils.isBlank(msgId) ? getCtrlMsgId() : msgId);
    }

    /**
     * 生成控制设备指令
     * <p>
     * 设备可以分为 lora, nb, wifi_gprs设备{@link ProtocolType}, snoti服务端定义了不同的协议要走不同的控制体.
     * snoti在登录信息添加了 protocolType 参数{@link AuthorizationData#getProtocolType()}, 从而用户只需要调用控制方法, snoti客户端会自行转换对应的控制体.
     * <p>
     * 关于v2
     * snoti服务端意识到控制接口分散, 对于扩展数据点的控制与企业API不统一等不足,
     * 2019-07-08开始提供了控制接口第二版,
     * 此接口可以统一控制 lora, nb, wifi_gprs设备, 兼容了扩展类型数据点控制.
     * 为了较好的过渡, snoti客户端新增了一个协议类型V2 {@link ProtocolType#V2},
     * 使用V2控制接口仅需要声明{@link AuthorizationData#setProtocolType(ProtocolType)}为V2即可.
     *
     * @param msgId        msgID
     * @param protocolType 协议类型
     * @return 控制体, 通过getOrder()方法获取具体的控制指令
     */
    private static AbstractCommandBody parseKvCtrl(String msgId, ProtocolType protocolType, NotiCtrlDTO... ctrlDTOs) {
        //如果自定义msgId为空则使用云端的规范生成msgId
        msgId = StringUtils.isBlank(msgId) ? getCtrlMsgId() : msgId;

        switch (protocolType) {
            case NB_IoT:
                // NB_IoT 不支持批量控制
                checkArgument(ctrlDTOs.length == 1, "NB-IoT 协议不支持批量控制, 需要批量控制请设置产品协议为 V2");
                NotiCtrlDTO nbDto = ctrlDTOs[0];
                InternalControlReqCommandBody nbBody = getInternalControlReqCommandBody(msgId, nbDto.getProductKey(), nbDto.getMac(), nbDto.getDid(), (Map<String, Object>) nbDto.getData());
                nbBody.setSource(SourceCategory.huawei);
                nbBody.setCmd(NotiGeneralCommandType.remote_control_nb_req);
                return nbBody;

            case LORA:
                // LoRa 不支持批量控制
                checkArgument(ctrlDTOs.length == 1, "LoRa 协议不支持批量控制, 需要批量控制请设置产品协议为 V2");
                NotiCtrlDTO loraDto = ctrlDTOs[0];
                InternalControlReqCommandBody loraBody = getInternalControlReqCommandBody(msgId, loraDto.getProductKey(), loraDto.getMac(), loraDto.getDid(), (Map<String, Object>) loraDto.getData());
                loraBody.setCmd(NotiGeneralCommandType.remote_control_lora_req);
                return loraBody;

            case V2:
                //控制接口v2, 统一了Nb_IoT, LoRa, Wi-Fi, GPRS控制
                List<ControlReqCommandBody.ControlBody> vtBodyList = getControlBodies(ctrlDTOs);
                return new ControlV2ReqCommandBody()
                        .setControlBody(vtBodyList)
                        .setMsgId(msgId);

            //默认用WiFi GPRS协议
            case WiFi_GPRS:
            default:
                List<ControlReqCommandBody.ControlBody> wifiBodyList = getControlBodies(ctrlDTOs);
                return new ControlReqCommandBody()
                        .setControlBody(wifiBodyList)
                        .setMsgId(msgId);
        }
    }

    private static List<ControlReqCommandBody.ControlBody> getControlBodies(NotiCtrlDTO[] ctrlDTO) {
        return Arrays.stream(ctrlDTO)
                .map(it -> {
                    ControlReqCommandBody.ControlBody v2Body = getControlBody(it.getProductKey(), it.getMac(), it.getDid(), NotiReqControlType.write_attrs);
                    v2Body.setPayload(v2Body.getPayload().setAttrs(new JSONObject((Map<String, Object>) it.getData())));
                    return v2Body;
                })
                .collect(Collectors.toList());
    }

    private static void checkArgument(boolean expression, String errorMsg) {
        if (!expression) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * 获取内部控制请求体
     * <p>
     * lora， nb的控制为内部支持
     * 需要使用内部请求体
     *
     * @param msgId      the msg id
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param dataPoint  the data point
     * @return the internal control req command body
     */
    private static InternalControlReqCommandBody getInternalControlReqCommandBody(String msgId, String productKey, String mac, String did, Map<String, Object> dataPoint) {
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(dataPoint, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty));

        InternalControlReqCommandBody body = new InternalControlReqCommandBody(productKey, mac, did, json);
        body.setMsgId(msgId);

        return body;
    }
}
