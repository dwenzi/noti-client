package com.gizwits.noti.noticlient.bean.resp.body;


import com.gizwits.noti.noticlient.bean.resp.NotiRespPushEvents;

/**
 * 设备重置事件
 *
 * @author Jcxcc
 * @since 1.0
 */
public class DataPointChangedEventBody extends AbstractPushEventBody {
    @Override
    public String pushEvent() {
        return NotiRespPushEvents.DATA_POINTS_CHANGED.getCode();
    }
}
