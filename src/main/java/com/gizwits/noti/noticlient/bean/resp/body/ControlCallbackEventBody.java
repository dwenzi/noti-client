package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 设备控制回调信息
 *
 * @author Jcxcc
 * @since 1.0
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ControlCallbackEventBody {
    @JSONField(name = "cmd")
    private String cmd;

    @JSONField(name = "msg_id")
    private String msgId;

    @JSONField(name = "result")
    private ResultBean result;

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ResultBean {
        /**
         * [{"FTAv7h95YESJTtepZ7GChq":"Did is offline"}]
         */
        private JSONArray failed;

        private JSONArray succeed;
    }
}

