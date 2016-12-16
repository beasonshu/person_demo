package com.wondersgroup.healthcloud.api.http.controllers.message;

import com.google.common.collect.Maps;
import com.wondersgroup.healthcloud.common.http.dto.JsonListResponseEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.misc.JsonKeyReader;
import com.wondersgroup.healthcloud.common.http.support.version.VersionRange;
import com.wondersgroup.healthcloud.services.user.message.MessageCenterServiceImpl;
import com.wondersgroup.healthcloud.services.user.message.dto.MessageCenterDto;
import com.wondersgroup.healthcloud.services.user.message.enums.MsgTypeEnum;
import com.wondersgroup.healthcloud.services.user.message.exception.EnumMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 1. 首页消息中心入口红点提示接口 <br/>
 * 2. 消息中心根列表接口 <br/>
 * 3. 消息状态设置接口 <br/>
 * Created by jialing.yao on 2016-12-12.
 */
@RestController
@RequestMapping(path = "/api/msgcenter")
public class MsgCenterController {
    @Autowired
    private MessageCenterServiceImpl messageCenterService;

    @GetMapping(path = "/prompt")
    @VersionRange
    public JsonResponseEntity<Map<String, Object>> prompt(@RequestParam String uid) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("has_unread", messageCenterService.hasUnread(uid));
        return new JsonResponseEntity<>(0, null, map);
    }

    @GetMapping(path = "/index")
    @VersionRange
    public JsonListResponseEntity<MessageCenterDto> rootList(@RequestHeader("main-area") String area,
                                                       @RequestParam String uid) {
        List<MessageCenterDto> rootList=messageCenterService.getRootList(area,uid);
        JsonListResponseEntity<MessageCenterDto> response = new JsonListResponseEntity();
        response.setContent(rootList);
        return response;
    }

    @PostMapping(path = "/message/status")
    @VersionRange
    public JsonResponseEntity<Map<String, Object>> status(@RequestBody String body) {
        JsonKeyReader reader = new JsonKeyReader(body);
        String msgType = reader.readString("msgType", false);
        String msgID = reader.readString("msgID", false);

        //此接口只跟系统消息、我的咨询、家庭消息有关
        MsgTypeEnum.fromTypeCode(msgType);
        if(!msgType.equals(MsgTypeEnum.msgType0.getTypeCode())
                && !msgType.equals(MsgTypeEnum.msgType1.getTypeCode())
                && !msgType.equals(MsgTypeEnum.msgType2.getTypeCode())){
            throw new EnumMatchException("消息类型["+msgType+"]不匹配.");
        }
        messageCenterService.setAsRead(msgType,msgID);
        Map<String, Object> map = Maps.newHashMap();
        map.put("read", true);
        return new JsonResponseEntity<>(0, null, map);
    }
}
