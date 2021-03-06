package com.wondersgroup.healthcloud.api.http.controllers.bbs;

import com.wondersgroup.healthcloud.common.http.dto.JsonListResponseEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.version.VersionRange;
import com.wondersgroup.healthcloud.jpa.constant.UserConstant;
import com.wondersgroup.healthcloud.jpa.entity.bbs.Circle;
import com.wondersgroup.healthcloud.jpa.entity.bbs.UserCircle;
import com.wondersgroup.healthcloud.jpa.entity.user.RegisterInfo;
import com.wondersgroup.healthcloud.services.bbs.*;
import com.wondersgroup.healthcloud.services.bbs.dto.CommentListDto;
import com.wondersgroup.healthcloud.services.bbs.dto.UserHomeDto;
import com.wondersgroup.healthcloud.services.bbs.dto.topic.TopicListDto;
import com.wondersgroup.healthcloud.services.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  1. 个人主页 基本信息
 *  2. 个人主页 话题列表 / 我发布的话题
 *  3. 个人主页 回复列表 / 我回复的
 *  4. 删除话题
 *  5. 检测是否可以发帖
 *  6. 检测用户是否可以发私信
 * @author ys
 */
@RestController
@RequestMapping("/api/bbs/user")
public class BbsUserController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserFansService fansService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private UserBbsService userBbsService;

    private final static int pageSize = 10;

    /**
     * 用户个人主页
     */
    @VersionRange
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public JsonResponseEntity<UserHomeDto> home(@RequestParam String uid,
                                                @RequestParam(required=false) String targetUid) {
        targetUid = StringUtils.isEmpty(targetUid) ? uid : targetUid;
        JsonResponseEntity<UserHomeDto> jsonResponseEntity = new JsonResponseEntity();
        RegisterInfo userInfo = userService.getOneNotNull(targetUid);
        UserHomeDto userHomeDto = new UserHomeDto();
        userHomeDto.mergeOwnerUserInfo(userInfo);
        userHomeDto.setAttentCount(fansService.countAttentNum(targetUid));
        userHomeDto.setFansCount(fansService.countFansNum(targetUid));
        userHomeDto.setAttentStatus(fansService.getMyAttentStatus(uid, targetUid));
        jsonResponseEntity.setData(userHomeDto);
        return jsonResponseEntity;
    }


    /**
     * 个人主页/个人中心
     * 话题列表 / 我发布的话题
     */
    @VersionRange
    @RequestMapping(value = "/topics", method = RequestMethod.GET)
    public JsonListResponseEntity<TopicListDto> topics(@RequestParam String uid,
                                                       @RequestParam(required=false) String targetUid,
                                                       @RequestParam(defaultValue="1",required = false) Integer flag){
        targetUid = StringUtils.isEmpty(targetUid) ? uid : targetUid;
        JsonListResponseEntity<TopicListDto> responseEntity = new JsonListResponseEntity<>();
        Boolean isWatchMine = uid.equals(targetUid);
        List<TopicListDto> listInfo = topicService.getTopicsByUid(targetUid, isWatchMine, flag, pageSize);
        Boolean hasMore = false;
        if (listInfo != null && listInfo.size() > pageSize){
            listInfo = listInfo.subList(0, pageSize);
            hasMore = true;
        }
        responseEntity.setContent(listInfo, hasMore, null, String.valueOf(flag + 1));
        return responseEntity;
    }

    /**
     * 个人主页/个人中心
     * 回复列表 / 我回复的
     */
    @VersionRange
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public JsonListResponseEntity<CommentListDto> comments(@RequestParam String uid,
                                                           @RequestParam(required=false) String targetUid,
                                                           @RequestParam(defaultValue="1",required = false) Integer flag){
        targetUid = StringUtils.isEmpty(targetUid) ? uid : targetUid;
        JsonListResponseEntity<CommentListDto> responseEntity = new JsonListResponseEntity<>();
        Boolean isWatchMine = uid.equals(targetUid);
        List<CommentListDto> listInfo = commentService.getUserCommentsList(targetUid, flag, pageSize);
        Boolean hasMore = false;
        if (listInfo != null && listInfo.size() > pageSize){
            listInfo = listInfo.subList(0, pageSize);
            hasMore = true;
        }
        responseEntity.setContent(listInfo, hasMore, null, String.valueOf(flag + 1));
        return responseEntity;
    }

    /**
     *  删除话题
     */
    @VersionRange
    @RequestMapping(value = "/delTopic", method = RequestMethod.DELETE)
    public JsonResponseEntity<Object> delTopic(@RequestParam String uid,
                                                @RequestParam Integer topicId) {
        JsonResponseEntity<Object> jsonResponseEntity = new JsonResponseEntity();

        Boolean isOK = userBbsService.delTopic(uid, topicId);
        if (!isOK){
            jsonResponseEntity.setCode(2021);
        }
        jsonResponseEntity.setMsg(isOK ? "删除成功" : "删除失败");
        return jsonResponseEntity;
    }

    @VersionRange
    @RequestMapping(value = "/isCanPublishTopic", method = RequestMethod.GET)
    public JsonResponseEntity<Map<String, Object>> isCanPublishTopic(@RequestParam String uid, @RequestParam Integer circleId) {
        JsonResponseEntity<Map<String, Object>> entity = new JsonResponseEntity();
        Map<String, Object> info = new HashMap<>();

        RegisterInfo account = userService.getOneNotNull(uid);
        if (account.getBanStatus() != UserConstant.BanStatus.OK){
            info.put("status", 1);
            info.put("info", "您已被禁言，不能发布话题");
            entity.setData(info);
            return entity;
        }
        Circle circle = circleService.getCircleInfoById(circleId);
        if (null == circle || circle.getDelFlag().equals("1")){
            info.put("status", 2);
            info.put("info", "圈子被禁用");
            entity.setData(info);
            return entity;
        }
        UserCircle userCircle = circleService.getAndCheckIsDefaultJoin(circleId, uid);
        if (userCircle == null){
            info.put("status", 3);
            info.put("info", "请先加入该圈子");
            entity.setData(info);
            return entity;
        }
        info.put("status", 0);
        info.put("info", "ok");
        entity.setData(info);
        return entity;
    }

    @VersionRange
    @RequestMapping(value = "/isCanPublishLetter", method = RequestMethod.GET)
    public JsonResponseEntity<Map<String, Object>> isCanPublishLetter(@RequestParam String uid, @RequestParam String letterUid) {
        JsonResponseEntity<Map<String, Object>> entity = new JsonResponseEntity();
        Map<String, Object> info = new HashMap<>();
        RegisterInfo account = userService.getOneNotNull(uid);
        if (account.getBanStatus() != UserConstant.BanStatus.OK){
            info.put("info", "您已被禁言，不能发布私信");
            info.put("status", 1);
            entity.setData(info);
            return entity;
        }
        info.put("status", 0);
        info.put("info", "ok");
        entity.setData(info);
        return entity;
    }

}
