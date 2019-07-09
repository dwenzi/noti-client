package com.gizwits.noti.noticlient.bean.resp.body;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ControlV2CallbackEventBody {

    /**
     * cmd : remote_control_v2_res
     * msg_id : MGI1MzcyZWZkMDdiNDBjYQ
     * result : {"succeed":[{"did":"nu7mMBjZZmLzAByu8fww8q"}],"failed":[]}
     */
    @JSONField(name = "cmd")
    private String cmd;

    @JSONField(name = "msg_id")
    private String msgId;

    @JSONField(name = "result")
    private ResultBean result;

    @Data
    @Accessors(chain = true)
    public static class ResultBean {

        public ResultBean() {
            this.failed = Collections.emptyList();
            this.succeed = Collections.emptyList();
        }

        private List<SucceedBean> succeed;
        private List<FailedBean> failed;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SucceedBean {

        /**
         * did : nu7mMBjZZmLzAByu8fww8q
         */
        private String did;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class FailedBean {

        /**
         * did : nu7mMBjZZmLzAByu8fww8q
         */
        private String did;

        /**
         * 失败的原因
         */
        private String reason;
    }
}
