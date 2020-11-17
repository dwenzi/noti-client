package com.gizwits.noti.noticlient.util;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti.noticlient.bean.req.NotiCtrlDTO;
import com.gizwits.noti.noticlient.bean.req.NotiReqControlType;
import com.gizwits.noti.noticlient.bean.req.body.AbstractCommandBody;
import com.gizwits.noti.noticlient.bean.req.body.ControlReqCommandBody;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

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
     * @param msgId    the msg id
     * @param ctrlDTOs the ctrl dt os
     * @return the abstract command body
     */
    public static AbstractCommandBody parseCtrl(String msgId, NotiCtrlDTO... ctrlDTOs) {
        checkArgument(ctrlDTOs != null && ctrlDTOs.length != 0, "控制参数不能为空");

        if (ctrlDTOs[0].getData() instanceof Map) {
            return parseKvCtrl(msgId, ctrlDTOs);
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
     * @param productKey  the product key
     * @param mac         the mac
     * @param did         the did
     * @param controlType the control type
     * @return the control body
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
                .map(ControlUtils::convertControlBody)
                .collect(Collectors.toList());

        return new ControlReqCommandBody()
                .setControlBody(controlBodies)
                .setMsgId(StringUtils.isBlank(msgId) ? getCtrlMsgId() : msgId);
    }

    private static ControlReqCommandBody.ControlBody convertControlBody(NotiCtrlDTO it) {
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
            if (rawByteArray.length == 0 || rawByteArray[0] != RAW_PREFIX) {
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
    }

    /**
     * 生成控制设备指令
     *
     * @param msgId msgID
     * @return 控制体, 通过getOrder()方法获取具体的控制指令
     */
    private static AbstractCommandBody parseKvCtrl(String msgId, NotiCtrlDTO... ctrlDTOs) {
        //如果自定义msgId为空则使用云端的规范生成msgId
        msgId = StringUtils.isBlank(msgId) ? getCtrlMsgId() : msgId;

        List<ControlReqCommandBody.ControlBody> bodyList = getControlBodies(ctrlDTOs);
        return new ControlReqCommandBody()
                .setControlBody(bodyList)
                .setMsgId(msgId);
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

}
