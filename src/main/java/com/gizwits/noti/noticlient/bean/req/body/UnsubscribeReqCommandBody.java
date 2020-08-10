package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.Credential;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jcxcc
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeReqCommandBody extends AbstractCommandBody {

    public UnsubscribeReqCommandBody() {
        this.cmd = NotiGeneralCommandType.unsubscribe_req;
        this.data = new ArrayList<>();
    }

    public UnsubscribeReqCommandBody(Credential credential) {
        this();
        this.data = Collections.singletonList(credential);
    }

    /**
     * 登录信息
     */
    @JSONField(name = "data")
    private List<Credential> data;

    @Override
    String getJson() {
        return JSONObject.toJSONString(this, SerializerFeature.WriteEnumUsingName, SerializerFeature.IgnoreNonFieldGetter);
    }
}
