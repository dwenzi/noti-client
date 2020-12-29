package com.gizwits.noti.noticlient.bean.req;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
public class NotiReqPushEventsTest {

    @Test
    public void codeOf() {
        NotiReqPushEvents eReset = NotiReqPushEvents.codeOf("device.reset");
        assertEquals(NotiReqPushEvents.RESET, eReset);

        NotiReqPushEvents eNull = NotiReqPushEvents.codeOf("null");
        assertNull(eNull);
    }
}