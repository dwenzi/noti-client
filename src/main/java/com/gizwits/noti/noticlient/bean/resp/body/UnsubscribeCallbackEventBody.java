package com.gizwits.noti.noticlient.bean.resp.body;

import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 取消订阅回调
 *
 * @author Jcxcc
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeCallbackEventBody extends AbstractPushEventBody {

    /**
     * data : {"result":true,"msg":"ok"}
     * cmd : subscribe_res
     */
    private DataBean data;

    /**
     * 操作成功
     *
     * @return the boolean
     */
    public boolean successful() {
        return BooleanUtils.isTrue(this.data.getResult());
    }

    @Override
    public String pushEvent() {
        return NotiRespPushEvents.UNSUBSCRIBE_CALLBACK.getCode();
    }

    /**
     * The type Data bean.
     */
    @Data
    public static class DataBean {
        /**
         * result : true
         * msg : ok
         */
        private Boolean result;
        private String msg;
    }
}
