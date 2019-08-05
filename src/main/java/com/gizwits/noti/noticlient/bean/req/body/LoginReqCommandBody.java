package com.gizwits.noti.noticlient.bean.req.body;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gizwits.noti.noticlient.bean.req.NotiGeneralCommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jcxcc
 * @since 1.0
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public final class LoginReqCommandBody extends AbstractCommandBody {

    public LoginReqCommandBody() {
        this.prefetchCount = 50;
    }

    /**
     * 0 < prefetch_count <= 32767, 表示推送没有 ACK 的消息
     * 的最大个数，可不填，默认值是 50
     */
    @JSONField(name = "prefetch_count")
    private int prefetchCount;

    /**
     * 产品登录验证信息
     */
    private List<AuthorizationData> data = new ArrayList<>();

    /**
     * 添加产品登录验证信息
     *
     * @param authorizes
     * @return
     */
    public void addLoginAuthorizes(AuthorizationData... authorizes) {
        data = Stream.of(data, Arrays.asList(authorizes))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

    }

    @Override
    String getJson() {
        this.setCmd(NotiGeneralCommandType.login_req);
        return JSONObject.toJSONString(this, SerializerFeature.WriteEnumUsingName, SerializerFeature.IgnoreNonFieldGetter);
    }
}
