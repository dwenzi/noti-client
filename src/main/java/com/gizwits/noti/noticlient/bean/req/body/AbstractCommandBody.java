package com.gizwits.noti.noticlient.bean.req.body;

import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractCommandBody {

    protected NotiGeneralCommandType cmd;

    /**
     * get json
     *
     * @return json字符串
     */
    abstract String getJson();

    public String getOrder() {
        return this.getJson() + StringUtils.LF;
    }
}
