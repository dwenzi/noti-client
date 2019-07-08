package com.gizwits.noti.noticlient.bean.req.body;

import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import com.gizwits.noti.noticlient.bean.req.SourceCategory;

/**
 * 私有协议
 *
 * @author Jcxcc
 * @since 1.0
 */
public interface InternalControlOrder {
    /**
     * Sets cmd.
     *
     * @param cmd the cmd
     */
    void setCmd(NotiGeneralCommandType cmd);

    /**
     * Sets source.
     *
     * @param source the source
     */
    void setSource(SourceCategory source);
}
