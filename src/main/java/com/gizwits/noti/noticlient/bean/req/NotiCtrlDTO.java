package com.gizwits.noti.noticlient.bean.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Noti ctrl dto.
 *
 * @param <T> the type parameter
 * @author Jcxcc
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotiCtrlDTO<T> {

    /**
     * Of noti ctrl dto.
     *
     * @param <T>        the type parameter
     * @param productKey the product key
     * @param mac        the mac
     * @param did        the did
     * @param data       the data
     * @return the noti ctrl dto
     */
    public static <T> NotiCtrlDTO<T> of(String productKey, String mac, String did, T data) {
        NotiCtrlDTO<T> dto = new NotiCtrlDTO<>();
        dto.setProductKey(productKey);
        dto.setMac(mac);
        dto.setDid(did);
        dto.setData(data);
        return dto;
    }

    private String productKey;

    private String mac;

    private String did;

    private T data;
}
