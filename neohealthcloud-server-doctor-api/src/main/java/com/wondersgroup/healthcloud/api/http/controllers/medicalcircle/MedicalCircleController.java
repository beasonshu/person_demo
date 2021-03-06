package com.wondersgroup.healthcloud.api.http.controllers.medicalcircle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.CaseAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.CommentAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.DoctorAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.DynamicAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.ImageAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.MedicalCircleAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.MedicalCircleDependence;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.MedicalCircleDetailAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.NoteAPIEntity;
import com.wondersgroup.healthcloud.api.http.dto.doctor.medicalcircle.ShareAPIEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonListResponseEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.misc.JsonKeyReader;
import com.wondersgroup.healthcloud.common.http.support.version.VersionRange;
import com.wondersgroup.healthcloud.dict.DictCache;
import com.wondersgroup.healthcloud.jpa.entity.circle.ArticleAttach;
import com.wondersgroup.healthcloud.jpa.entity.circle.ArticleTransmit;
import com.wondersgroup.healthcloud.jpa.entity.medicalcircle.MedicalCircle;
import com.wondersgroup.healthcloud.jpa.entity.medicalcircle.MedicalCircleAttention;
import com.wondersgroup.healthcloud.jpa.entity.medicalcircle.MedicalCircleCommunity;
import com.wondersgroup.healthcloud.jpa.entity.medicalcircle.MedicalCircleReply;
import com.wondersgroup.healthcloud.jpa.repository.doctor.DoctorAccountRepository;
import com.wondersgroup.healthcloud.services.doctor.DoctorService;
import com.wondersgroup.healthcloud.services.doctor.entity.Doctor;
import com.wondersgroup.healthcloud.services.medicalcircle.MedicalCircleService;
import com.wondersgroup.healthcloud.utils.ImageUtils;
import com.wondersgroup.healthcloud.utils.TimeAgoUtils;
import com.wondersgroup.healthcloud.utils.circle.CircleLikeUtils;

/**
 * 
 * Created by sunhaidi on 2016.8.29
 */
@RestController
@RequestMapping("/api/medicalcircle")
public class MedicalCircleController {

    @Autowired
    private MedicalCircleService    mcService;
    @Autowired
    private DoctorService           docinfoService;
    @Autowired
    private DictCache               dictCache;
    @Autowired
    private DoctorAccountRepository doctorAccountRepository;
    @Autowired
    private CircleLikeUtils circleLikeUtils;
    @Autowired
    private MedicalCircleService cedicalCircleService;
    @Autowired
    private ImageUtils imageUtils;
    @Autowired
    private Environment environment;

    private JsonListResponseEntity<MedicalCircleAPIEntity> getMedicalCircleList(String screen_width,
            Integer[] circle_type, String doctor_id, String uid, String order, String flag, Boolean collect) {

        JsonListResponseEntity<MedicalCircleAPIEntity> result = new JsonListResponseEntity<>();
        List<MedicalCircleAPIEntity> list = new ArrayList<>();
        Boolean more = true;
        Date sendtime = new Date();
        if (StringUtils.isNotEmpty(flag)) {
            sendtime = new Date(Long.valueOf(flag));
        }
        if (StringUtils.isEmpty(order)) {
            order = "sendtime:desc";
        }
        List<MedicalCircle> mcList;
        if (collect) {
            mcList = mcService.getCollectCircleList(doctor_id, sendtime, circle_type);
        } else {
            if (StringUtils.isNotEmpty(doctor_id)) {
                mcList = mcService.getUserMedicalCircle(doctor_id, circle_type, sendtime, order); //获取个人健康圈列表
            } else {
                mcList = mcService.getAllMedicalCircle(circle_type, order, sendtime); //获取全部健康圈列表
            }
        }

        for (MedicalCircle mc : mcList) {
            MedicalCircleAPIEntity entity = new MedicalCircleAPIEntity();
            Doctor doctorInfo = getDoctorByDocotrId(mc.getDoctorid());
            if (doctorInfo == null) {
                continue;
            }
            entity.setAgo(TimeAgoUtils.ago(mc.getSendtime()));
            entity.setComment_num(mcService.getCommentsNum(mc.getId()));
            entity.setTag(dictCache.queryTagName(mc.getTagid()));
            entity.setColor(dictCache.queryTagColor(mc.getTagid()));
            entity.setAvatar(doctorInfo.getAvatar());
            entity.setCircle_id(mc.getId());
            entity.setDoctor_id(mc.getDoctorid());
            entity.setHospital(doctorInfo.getHospitalName());
            if (StringUtils.isNotEmpty(uid)) {
                entity.setIs_liked(circleLikeUtils.isLikeOne(mc.getId(), uid));//redis
            }
            entity.setLike_num(mc.getPraisenum());
            entity.setName(doctorInfo.getName());
            Integer type = mc.getType();
            entity.setCircle_type(type);
            List<ArticleAttach> images = mcService.getCircleAttachs(mc.getId());
            List<ImageAPIEntity> imageAPIEntities = new ArrayList<>();
            if (images != null && images.size() > 0) {
                if (images.size() == 1) {
                    ImageAPIEntity imageAPIEntity = new ImageAPIEntity();
                    ImageUtils.Image image = imageUtils.getImage(images.get(0).getAttachid());
                    if (image != null) {
                        imageAPIEntity.setRatio(imageUtils.getImgRatio(image));
                        imageAPIEntity.setUrl(image.getUrl());
                        imageAPIEntity.setThumb(imageUtils.getBigThumb(image, screen_width));
                        imageAPIEntity.setHeight(imageUtils.getUsefulImgHeight(image, screen_width));
                        imageAPIEntity.setWidth(imageUtils.getUsefulImgWidth(image, screen_width));
                        imageAPIEntities.add(imageAPIEntity);
                    }
                } else {
                    for (ArticleAttach image : images) {
                        ImageAPIEntity imageAPIEntity = new ImageAPIEntity();
                        imageAPIEntity.setUrl(image.getAttachid());
                        imageAPIEntity.setThumb(imageUtils.getSquareThumb(image.getAttachid(), screen_width));
                        imageAPIEntities.add(imageAPIEntity);
                    }
                }
            }
            String content = mc.getContent();
            if (StringUtils.isNotBlank(content) && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            if (type == 1) {//帖子
                NoteAPIEntity note = new NoteAPIEntity();
                note.setContent(content);
                if (images != null && images.size() > 0) {
                    note.setHas_images(true);
                } else {
                    note.setHas_images(false);
                }
                note.setTitle(mc.getTitle());
                entity.setNote(note);
            } else if (type == 2) {//病例
                CaseAPIEntity cases = new CaseAPIEntity();
                cases.setTitle(mc.getTitle());
                cases.setContent(content);
                cases.setImages(imageAPIEntities);
                entity.setCases(cases);
            } else if (type == 3) {//动态
                DynamicAPIEntity dynamic = new DynamicAPIEntity();
                dynamic.setContent(content);
                dynamic.setImages(imageAPIEntities);
                ArticleTransmit share = mcService.getMedicalCircleForward(mc.getId());
                if (share != null) {
                    ShareAPIEntity shareAPIEntity = new ShareAPIEntity();
                    shareAPIEntity.setTitle(share.getTitle());
                    shareAPIEntity.setDesc(share.getSubtitle());
                    shareAPIEntity.setThumb(share.getPic());
                    shareAPIEntity.setUrl(share.getUrl());
                    dynamic.setShare(shareAPIEntity);
                }
                entity.setDynamic(dynamic);
            }

            if (mcList.get(mcList.size() - 1).equals(mc)) {
                flag = String.valueOf(mc.getSendtime().getTime());
            }
            list.add(entity);
        }
        if (mcList.size() < 20) {
            more = false;
        }

        result.setContent(list, more, order, flag);
        return result;
    }

    /**
     * 医学圈子列表
     * @param screen_width
     * @param doctor_id
     * @param order
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(method = RequestMethod.GET, value = "allCircle")
    public JsonListResponseEntity<MedicalCircleAPIEntity> getAllCircle(
            @RequestHeader(value = "screen-width", defaultValue = "100") String screen_width,
            @RequestParam(value = "doctor_id", required = false) String doctor_id,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {
        return getMedicalCircleList(screen_width, new Integer[] { 1, 2, 3 }, null, doctor_id, order, flag, false);
    }

    /**
     * 医学圈子内容详情
     * @param screen_width
     * @param circle_id
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public JsonResponseEntity<MedicalCircleDetailAPIEntity> getCircleDetail(
            @RequestHeader(value = "screen-width", defaultValue = "100") String screen_width,
            @RequestParam(value = "circle_id", required = true) String circle_id,
            @RequestParam(value = "doctor_id", required = true) String doctor_id
            ) {
        JsonResponseEntity<MedicalCircleDetailAPIEntity> responseEntity = new JsonResponseEntity<>();
        MedicalCircle mc = mcService.getMedicalCircle(circle_id);
        responseEntity.setData(newMedicalCircleDetailAPIEntity(new MedicalCircleDependence(mcService, dictCache),
                mc, screen_width, doctor_id));
        mcService.view(circle_id, doctor_id);//redis
        return responseEntity;
    }

    /**
     * 评论列表
     * @param circle_id
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public JsonListResponseEntity<CommentAPIEntity> getCommentList(
            @RequestParam(value = "circle_id", required = true) String circle_id,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {
        
        JsonListResponseEntity<CommentAPIEntity> responseEntity = new JsonListResponseEntity<>();
        List<CommentAPIEntity> commentAPIEntities = new ArrayList<>();
        Boolean more = true;
        Date discusstime = new Date(0l);
        if (StringUtils.isNotEmpty(flag)) {
            discusstime = new Date(Long.valueOf(flag));
        }
        if (StringUtils.isEmpty(order)) {
            order = "discusstime:asc";
        }

        List<MedicalCircleCommunity> comments = mcService.getMedicalCircleComments(circle_id, order, discusstime);
        int cfloor = 1;
        for (MedicalCircleCommunity comment : comments) {
            CommentAPIEntity commentEntity = new CommentAPIEntity();
            Doctor doctorInfo = getDoctorByDocotrId(comment.getDoctorid());
            if (doctorInfo == null) {
                continue;
            }
            commentEntity.setAgo(TimeAgoUtils.ago(comment.getDiscusstime()));
            commentEntity.setAvatar(doctorInfo.getAvatar());
            commentEntity.setContent(comment.getContent());
            commentEntity.setFloor(mcService.getFloor(cfloor));
            commentEntity.setDoctor_id(comment.getDoctorid());
            commentEntity.setName(doctorInfo.getName());
            commentEntity.setComment_id(comment.getId());
            int rfloor = 1;
            List<CommentAPIEntity> replyEntitylist = new ArrayList<CommentAPIEntity>();
            List<MedicalCircleReply> commentReplyList = mcService.getCommentReplyList(comment.getId(), new Date(0),
                    "discusstime:asc", 5);
            for (MedicalCircleReply reply : commentReplyList) {
                CommentAPIEntity replyEntity = new CommentAPIEntity();
                Doctor doctor = getDoctorByDocotrId(reply.getDoctorid());
                if (doctor == null) {
                    continue;
                }
                replyEntity.setName(doctor.getName());
                replyEntity.setDoctor_id(reply.getDoctorid());
                replyEntity.setAgo(TimeAgoUtils.ago(reply.getDiscusstime()));
                replyEntity.setAvatar(doctor.getAvatar());
                replyEntity.setContent(reply.getContent());
                replyEntity.setFloor(mcService.getFloor(rfloor));
                Doctor replyDoctor = getDoctorByDocotrId(reply.getReplyid());
                if (replyDoctor != null) {
                    replyEntity.setReply_name(replyDoctor.getName() != null ? replyDoctor.getName() : replyDoctor
                            .getNickname());
                }
                rfloor++;
                replyEntitylist.add(replyEntity);
            }
            if (commentReplyList.size() < 5) {
                commentEntity.setReply_more(false);
            } else {
                commentEntity.setReply_more(true);
            }
            cfloor++;
            commentEntity.setReply_list(replyEntitylist);
            if (comment.equals(comments.get(comments.size() - 1))) {
                flag = String.valueOf(comment.getDiscusstime().getTime());
            }
            commentAPIEntities.add(commentEntity);
        }
        if (comments.size() < 20) {
            more = false;
        }
        responseEntity.setContent(commentAPIEntities, more, order, flag);
        return responseEntity;
    }

    /**
     * 回复列表
     * @param comment_id 评论id
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comments/reply", method = RequestMethod.GET)
    public JsonListResponseEntity<CommentAPIEntity> getCommentReplyList(
            @RequestParam(value = "comment_id") String comment_id,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {
        
        JsonListResponseEntity<CommentAPIEntity> responseEntity = new JsonListResponseEntity<>();
        Boolean more = true;
        Date discusstime = new Date(0l);
        if (StringUtils.isNotEmpty(flag)) {
            discusstime = new Date(Long.valueOf(flag));
        }
        if (StringUtils.isEmpty(order)) {
            order = "discusstime:asc";
        }
        int rfloor = 1;
        List<CommentAPIEntity> replyEntitylist = new ArrayList<>();
        List<MedicalCircleReply> commentReplyList = mcService.getCommentReplyList(comment_id, discusstime, order, 20);
        for (MedicalCircleReply reply : commentReplyList) {
            CommentAPIEntity replyEntity = new CommentAPIEntity();
            Doctor doctorInfo = getDoctorByDocotrId(reply.getDoctorid());
            if (doctorInfo == null) {
                continue;
            }
            replyEntity.setName(doctorInfo.getName());
            replyEntity.setDoctor_id(reply.getDoctorid());
            replyEntity.setAgo(TimeAgoUtils.ago(reply.getDiscusstime()));
            replyEntity.setAvatar(doctorInfo.getAvatar());
            replyEntity.setContent(reply.getContent());
            replyEntity.setFloor(mcService.getFloor(rfloor));
            Doctor replyDoctorInfo = getDoctorByDocotrId(reply.getReplyid());
            if (replyDoctorInfo != null) {
                replyEntity.setReply_name(replyDoctorInfo.getName() != null ? replyDoctorInfo.getName()
                        : replyDoctorInfo.getNickname());
            }
            rfloor++;
            replyEntitylist.add(replyEntity);
            if (reply.equals(commentReplyList.get(commentReplyList.size() - 1))) {
                flag = String.valueOf(reply.getDiscusstime().getTime());
            }
        }
        if (commentReplyList.size() < 20) {
            more = false;
        }
        responseEntity.setContent(replyEntitylist, more, order, flag);
        return responseEntity;
    }

    /**
     * 发布
     * @param doctor_id
     * @param circle_type
     * @param title
     * @param content
     * @param images
     * @return
     * @throws IOException
     */
    @VersionRange
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public JsonResponseEntity<String> publish(@RequestBody String body) {
       
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        Integer circle_type = reader.readInteger("circle_type", false);
        String title = reader.readString("title", true);
        String content = reader.readString("content", true);
        String images = reader.readString("files", true);
        //        if (!SensitiveWordsFilterUtils.isIncludeSenstiveWords(content)) {
        List<String> imageURLs = new ArrayList<String>();
        if(!StringUtils.isBlank(images)){
            imageURLs = Arrays.asList(images.split(","));
        }
        mcService.publish(doctor_id, title, content, circle_type, imageURLs);
        entity.setMsg("发布成功");
        //        } else {
        //            entity.setCode(1299);
        //            entity.setMsg("内容含有敏感词汇");
        //        }
        return entity;
    }

    /**
     * 评论
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public JsonResponseEntity<CommentAPIEntity> comment(@RequestBody String body) {
        
        JsonResponseEntity<CommentAPIEntity> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String content = reader.readString("content", false);
        String circle_id = reader.readString("circle_id", false);
        //                if (!SensitiveWordsFilterUtils.isIncludeSenstiveWords(content)) {
        MedicalCircleCommunity comment = mcService.comment(doctor_id, circle_id, content);
        CommentAPIEntity commentEntity = new CommentAPIEntity();
        Doctor doctorInfo = getDoctorByDocotrId(doctor_id);
        if (doctorInfo != null) {
            commentEntity.setName(doctorInfo.getName());
            commentEntity.setAvatar(doctorInfo.getAvatar());
        }
        commentEntity.setAgo(TimeAgoUtils.ago(comment.getDiscusstime()));
        commentEntity.setContent(comment.getContent());
        commentEntity.setDoctor_id(comment.getDoctorid());
        commentEntity.setComment_id(comment.getId());
        entity.setData(commentEntity);
        entity.setMsg("评论成功");
        //                } else {
        //                    entity.setCode(1299);
        //                    entity.setMsg("内容含有敏感词汇");
        //                }
        return entity;
    }

    /**
     * 回复
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comment/reply", method = RequestMethod.POST)
    public JsonResponseEntity<CommentAPIEntity> reply(@RequestBody String body) {
       
        JsonResponseEntity<CommentAPIEntity> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String comment_id = reader.readString("comment_id", false);
        String reply_doctor_id = reader.readString("reply_doctor_id", false);
        String content = reader.readString("content", false);
        //                if (!SensitiveWordsFilterUtils.isIncludeSenstiveWords(content)) {
        mcService.reply(comment_id, doctor_id, reply_doctor_id, content);
        entity.setMsg("回复成功");
        CommentAPIEntity replyEntity = new CommentAPIEntity();
        Doctor replyDoctorInfo = getDoctorByDocotrId(doctor_id);
        if (replyDoctorInfo != null) {
            replyEntity.setName(replyDoctorInfo.getName());
            replyEntity.setAvatar(replyDoctorInfo.getAvatar());
        }
        replyEntity.setDoctor_id(doctor_id);
        replyEntity.setAgo("刚刚");
        replyEntity.setContent(content);
        Doctor doctorInfo = getDoctorByDocotrId(reply_doctor_id);
        if (doctorInfo != null) {
            replyEntity.setReply_name(doctorInfo.getName() != null ? doctorInfo.getName() : doctorInfo.getNickname());
        }
        entity.setData(replyEntity);
        //                } else {
        //                    entity.setCode(1299);
        //                    entity.setMsg("内容含有敏感词汇");
        //                }
        return entity;
    }

    /**
     * 点赞
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/like", method = RequestMethod.POST)
    public JsonResponseEntity<String> like(@RequestBody String body) {
       
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String circle_id = reader.readString("circle_id", false);
        Boolean success = mcService.like(doctor_id, circle_id);
        if (success) {
            entity.setMsg("点赞成功");
            Doctor doctor = getDoctorByDocotrId(doctor_id);
            if (doctor != null) {
                entity.setData(doctor_id + ":" + doctor.getName() != null ? doctor.getName() : doctor.getNickname());
            }
        } else {
            entity.setCode(1320);
            entity.setMsg("点赞失败");
        }
        return entity;
    }

    /**
     * 取消点赞
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/unlike", method = RequestMethod.POST)
    public JsonResponseEntity<String> unlike(@RequestBody String body) {
       
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String circle_id = reader.readString("circle_id", false);
        Boolean success = mcService.unlike(doctor_id, circle_id);
        if (success) {
            entity.setMsg("取消点赞成功");
            Doctor doctor = getDoctorByDocotrId(doctor_id);
            if (doctor != null) {
                entity.setData(doctor_id + ":" + doctor.getName() != null ? doctor.getName() : doctor.getNickname());
            }
        } else {
            entity.setCode(1320);
            entity.setMsg("取消点赞失败");
        }
        return entity;
    }

    /**
     * 分享
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/forward", method = RequestMethod.POST)
    public JsonResponseEntity<String> forward(@RequestBody String body) {
        
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String content = reader.readString("content", true);
        String title = reader.readString("title", false);
        String desc = reader.readString("desc", false);
        String url = reader.readString("url", false);
        String thumb = reader.readString("thumb", true);
        mcService.forward(doctor_id, title, desc, thumb, url, content);
        entity.setMsg("分享成功");
        return entity;
    }

    /**
     * 举报
     * @param body
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public JsonResponseEntity<String> report(@RequestBody String body) {
        
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String uid = reader.readString("doctor_id", false);
        String reportid = reader.readString("reportid", false);
        Integer content_type = reader.readInteger("content_type", false);
        Integer reporttype = reader.readInteger("report_type", false);
        Boolean success = mcService.report(uid, reportid, content_type, reporttype);
        if (success) {
            entity.setMsg("举报成功");
        } else {
            entity.setCode(1320);
            entity.setMsg("已举报，请不要重复举报");
        }
        return entity;
    }

    /**
     * 删除帖子
     * @return
     */
    @VersionRange
    @RequestMapping(method = RequestMethod.DELETE, value = "delCircle")
    public JsonResponseEntity<String> delCircle(
            @RequestParam("doctor_id") String doctor_id,
            @RequestParam("circle_id") String circle_id
        ) {
        
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        Boolean success = mcService.delMedicalCircle(doctor_id, circle_id);
        if (success) {
            entity.setMsg("删除成功");
        } else {
            entity.setCode(1320);
            entity.setMsg("删除失败");
        }
        return entity;
    }

    /**
     * 删除评论
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comment", method = RequestMethod.DELETE)
    public JsonResponseEntity<String> delComment(
            @RequestParam("doctor_id") String doctor_id,
            @RequestParam("comment_id") String comment_id
        ) {
        
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        Boolean success = mcService.delComment(doctor_id, comment_id);
        if (success) {
            entity.setMsg("删除成功");
        } else {
            entity.setCode(1320);
            entity.setMsg("删除失败");
        }
        return entity;
    }

    /**
     * 删除回复
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/comment/reply", method = RequestMethod.DELETE)
    public JsonResponseEntity<String> delReply(
            @RequestParam("doctor_id") String doctor_id,
            @RequestParam("reply_id") String reply_id
        ) {
        
        JsonResponseEntity<String> entity = new JsonResponseEntity<>();
        Boolean success = mcService.delReply(reply_id, doctor_id);
        if (success) {
            entity.setMsg("删除成功");
        } else {
            entity.setCode(1320);
            entity.setMsg("删除失败");
        }
        return entity;
    }

   

    /**
     * 收藏
     */
    @VersionRange
    @RequestMapping(value = "/collect", method = RequestMethod.POST)
    public JsonResponseEntity<String> collect(@RequestBody String body) {

        JsonResponseEntity<String> result = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String circle_id = reader.readString("circle_id", false);
        Boolean collect = mcService.collect(circle_id, doctor_id, 1);
        if (collect) {
            result.setMsg("收藏成功");
        } else {
            result.setCode(1320);
            result.setMsg("已收藏过");
        }
        return result;
    }

    /**
     * 取消收藏
     */
    @VersionRange
    @RequestMapping(value = "/cancelCollect", method = RequestMethod.POST)
    public JsonResponseEntity<String> collectDel(@RequestBody String body) {

        JsonResponseEntity<String> result = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctor_id = reader.readString("doctor_id", false);
        String circle_id = reader.readString("circle_id", false);
        Boolean success = mcService.delCollect(circle_id, doctor_id, 1);
        if (success) {
            result.setCode(0);
            result.setMsg("取消收藏成功");
        } else {
            result.setCode(1321);
            result.setMsg("取消收藏失败");
        }
        return result;
    }

    /**
     * 关注
     * attention_id 关注人id
     * followed_id 被关注人id
     */
    @VersionRange
    @RequestMapping(value = "/doctor/follow", method = RequestMethod.POST)
    public JsonResponseEntity<String> follow(@RequestBody String body) {

        JsonResponseEntity<String> result = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String attention_id = reader.readString("attention_id", false);
        String followed_id = reader.readString("followed_id", false);
        Boolean success = mcService.attention(attention_id, followed_id);
        if (success) {
            result.setMsg("关注成功");
        } else {
            result.setCode(1320);
            result.setMsg("已关注过");
        }
        return result;
    }

    /**
     * 取消关注
     */
    @VersionRange
    @RequestMapping(value = "/doctor/unfollow", method = RequestMethod.POST)
    public JsonResponseEntity<String> unfollow(@RequestBody String body) {

        JsonResponseEntity<String> result = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String attention_id = reader.readString("attention_id", false);
        String followed_id = reader.readString("followed_id", false);
        Boolean success = mcService.cancelAttention(attention_id, followed_id);
        if (success) {
            result.setMsg("取消关注成功");
        } else {
            result.setCode(1320);
            result.setMsg("未关注");
        }
        return result;
    }

    /**
     * 医生详情
     * @param uid
     * @param doctor_id
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/doctor", method = RequestMethod.GET)
    public JsonResponseEntity<DoctorAPIEntity> doctorinfo(
            @RequestParam("uid") String uid,
            @RequestParam("doctor_id") String doctor_id) {
        
        JsonResponseEntity<DoctorAPIEntity> result = new JsonResponseEntity<>();
        DoctorAPIEntity doctor = new DoctorAPIEntity();
        Doctor d = getDoctorByDocotrId(doctor_id);
        if (d != null) {
            doctor.setDoctor_id(doctor_id);
            doctor.setAvatar(d.getAvatar());
            doctor.setHospital(d.getHospitalName());
            doctor.setName(d.getName());
            if (!uid.equals(doctor_id)) {
                doctor.setIs_attention(mcService.isAttention(uid, doctor_id));
            }
            doctor.setAttention_num(mcService.getDocFollowedNum(doctor_id));
            doctor.setFans_num(mcService.getDocFansNum(doctor_id));
            doctor.setNotecase_num(mcService.getNoteCaseNum(doctor_id));
            doctor.setDynamic_num(mcService.getDynamicNum(doctor_id));
        }
        result.setData(doctor);
        return result;
    }

    /**
     * 某医生帖子/病例列表
     * @param screen_width
     * @param doctor_id
     * @param order
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/doctor/notecase", method = RequestMethod.GET)
    public JsonListResponseEntity<MedicalCircleAPIEntity> getOneNoteCaseCircle(
            @RequestHeader(value = "screen-width", defaultValue = "100") String screen_width,
            @RequestParam(value = "doctor_id", required = false) String doctor_id,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "collect") Boolean collect,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {

        return getMedicalCircleList(screen_width, new Integer[] { 1, 2 }, doctor_id, uid, order, flag, collect);
    }

    /**
     * 某医生动态列表
     * @param screen_width
     * @param doctor_id
     * @param order
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/doctor/dynamic", method = RequestMethod.GET)
    public JsonListResponseEntity<MedicalCircleAPIEntity> getOneDynamicCircle(
            @RequestHeader(value = "screen-width", defaultValue = "100") String screen_width,
            @RequestParam(value = "doctor_id", required = false) String doctor_id,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "collect") Boolean collect,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {

        return getMedicalCircleList(screen_width, new Integer[] { 3 }, doctor_id, uid, order, flag, collect);
    }

    /**
     * 某医生关注列表
     * @param doctor_id
     * @param order
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/doctor/attention", method = RequestMethod.GET)
    public JsonListResponseEntity<DoctorAPIEntity> getOneAttentionList(
            @RequestParam("uid") String uid,
            @RequestParam(value = "doctor_id") String doctor_id,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {
       
        JsonListResponseEntity<DoctorAPIEntity> result = new JsonListResponseEntity();
        Boolean more = true;
        Date attentiontime = new Date();
        if (StringUtils.isNotEmpty(flag)) {
            attentiontime = new Date(Long.valueOf(flag));
        }
        if (StringUtils.isEmpty(order)) {
            order = "attentiontime:desc";
        }
        List<MedicalCircleAttention> attlist = mcService.getDocFollowedList(doctor_id, order, attentiontime);
        List<DoctorAPIEntity> list = new ArrayList<>();
        for (MedicalCircleAttention att : attlist) {
            DoctorAPIEntity entity = new DoctorAPIEntity();
            Doctor d = getDoctorByDocotrId(att.getConcernedid());
            if (d == null) {
                continue;
            }
            entity.setDoctor_id(att.getConcernedid());
            entity.setAvatar(d.getAvatar());
            entity.setHospital(d.getHospitalName());
            entity.setName(d.getName());
            entity.setIs_attention(mcService.isAttention(uid, d.getUid()));
            list.add(entity);
        }
        if (attlist.size() < 20) {
            more = false;
        } else {
            flag = String.valueOf(attlist.get(attlist.size() - 1).getAttentiontime().getTime());
        }
        result.setContent(list, more, order, flag);
        return result;
    }

    /**
     * 某医生粉丝列表
     * @param doctor_id
     * @param order
     * @param flag
     * @return
     */
    @VersionRange
    @RequestMapping(value = "/doctor/fans", method = RequestMethod.GET)
    public JsonListResponseEntity<DoctorAPIEntity> getOneFansList(
            @RequestParam("uid") String uid,
            @RequestParam(value = "doctor_id") String doctor_id,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "flag", required = false) String flag) {

        JsonListResponseEntity<DoctorAPIEntity> result = new JsonListResponseEntity();
        Boolean more = true;
        Date attentiontime = new Date();
        if (StringUtils.isNotEmpty(flag)) {
            attentiontime = new Date(Long.valueOf(flag));
        }
        if (StringUtils.isEmpty(order)) {
            order = "attentiontime:desc";
        }
        List<MedicalCircleAttention> attlist = mcService.getDocFansList(doctor_id, order, attentiontime);
        List<DoctorAPIEntity> list = new ArrayList<>();
        for (MedicalCircleAttention att : attlist) {
            DoctorAPIEntity entity = new DoctorAPIEntity();
            Doctor d = getDoctorByDocotrId(att.getDoctorid());
            if (d == null) {
                continue;
            }
            entity.setDoctor_id(att.getDoctorid());
            entity.setAvatar(d.getAvatar());
            entity.setHospital(d.getHospitalName());
            entity.setName(d.getName());
            entity.setIs_attention(mcService.isAttention(uid, d.getUid()));
            list.add(entity);
        }
        //获取最后一个对象的时间作为flag
        if(attlist != null && !attlist.isEmpty()){
            flag = String.valueOf(attlist.get(attlist.size() - 1) == null ? "" : attlist.get(attlist.size() - 1)
                    .getAttentiontime().getTime());
        }
        if (attlist.size() < 20) {
            more = false;
        }
        result.setContent(list, more, order, flag);
        return result;
    }

    public Doctor getDoctorByDocotrId(String doctorId) {
        return docinfoService.findDoctorByUid(doctorId);
    }
    
    public List<Doctor> getDoctorByDocotrIds(String[] doctorIds) {
        if(doctorIds == null || doctorIds.length < 1){
            return null;
        }
        String ids = "";
        for (String id : doctorIds) {
            ids+=id + ",";
        }
        return docinfoService.findDoctorByIds(ids.substring(0, ids.length() - 1));
    }
    
    public MedicalCircleDetailAPIEntity newMedicalCircleDetailAPIEntity(MedicalCircleDependence dep, MedicalCircle mc, String screen_width, String uid) {
        MedicalCircleDetailAPIEntity entity = new MedicalCircleDetailAPIEntity();
        if (dep == null && mc == null) {
            return entity;
        }
        DictCache dictCache = dep.getDictCache();
        MedicalCircleService mcService = dep.getMcService();
        Doctor doctor = getDoctorByDocotrId(mc.getDoctorid());
        if (doctor != null) {
            String circle_id = mc.getId();
            String doctor_id = doctor.getUid();
            entity.setDoctor_id(doctor_id);
            entity.setAgo(TimeAgoUtils.ago(mc.getSendtime()));
            entity.setAvatar(doctor.getAvatar());
            entity.setCircle_id(circle_id);
            entity.setCircle_type(mc.getType());
            entity.setComment_num(mcService.getCommentsNum(circle_id));
            entity.setLike_num(null != mc.getPraisenum() ? mc.getPraisenum() : 0);
                    entity.setHospital(doctor.getHospitalName());
            if (StringUtils.isNotEmpty(uid)) {
                entity.setIs_liked(circleLikeUtils.isLikeOne(circle_id, uid));
            }
            entity.setName(doctor.getName());
            entity.setTag(dictCache.queryTagName(mc.getTagid()));
            entity.setColor(dictCache.queryTagColor(mc.getTagid()));
            entity.setViews(mcService.getCircleViews(circle_id));//redis
            entity.setIs_collected(mcService.checkCollect(circle_id, uid, 1));
            cedicalCircleService.getMedicalCircle(circle_id);
            List<Doctor> doctors = getDoctorByDocotrIds(circleLikeUtils.likeUserIds(circle_id));
            if(doctors != null && doctors.size() > 0){
                String[] docLikeNames = new String[doctors.size()];
                for (int i = 0; i < doctors.size(); i++) {
                    Doctor doc = doctors.get(i);
                    if(doc != null) {
                        String name = doc.getName();
                        if(StringUtils.isEmpty(name)){
                            name = doc.getNickname();
                        }
                        docLikeNames[i] = doc.getUid() + ":" + name;
                        entity.setLike_doc_names("");
                        if (i == doctors.size() - 1) {
                            entity.setLike_doc_names(entity.getLike_doc_names() + name);
                        } else {
                            entity.setLike_doc_names(entity.getLike_doc_names() + name + ",");
                        }
                    }
                }
                entity.setLiked_doc_name(docLikeNames);
            }
            Integer type = mc.getType();
            entity.setCircle_type(type);
            List<ArticleAttach> images = mcService.getCircleAttachs(mc.getId());
            List<ImageAPIEntity> imageAPIEntities = new ArrayList<>();
            if (images != null && images.size() > 0) {
                if (images.size() == 1) {
                    ImageAPIEntity imageAPIEntity = new ImageAPIEntity();
                    ImageUtils.Image image = imageUtils.getImage(images.get(0).getAttachid());
                    if (image != null) {
                        imageAPIEntity.setRatio(imageUtils.getImgRatio(image));
                        imageAPIEntity.setUrl(image.getUrl());
                        imageAPIEntity.setThumb(imageUtils.getBigThumb(image, screen_width));
                        imageAPIEntity.setHeight(imageUtils.getUsefulImgHeight(image, screen_width));
                        imageAPIEntity.setWidth(imageUtils.getUsefulImgWidth(image, screen_width));
                        imageAPIEntities.add(imageAPIEntity);
                    }
                } else {
                    for (ArticleAttach image : images) {
                        ImageAPIEntity imageAPIEntity = new ImageAPIEntity();
                        imageAPIEntity.setUrl(image.getAttachid());
                        imageAPIEntity.setThumb(imageUtils.getSquareThumb(image.getAttachid(), screen_width));
                        imageAPIEntities.add(imageAPIEntity);
                    }
                }
            }
            if (type == 1) {//帖子
                NoteAPIEntity note = new NoteAPIEntity();
                note.setContent(mc.getContent());
                note.setImages(imageAPIEntities);
                note.setTitle(mc.getTitle());
                entity.setNote(note);
            } else if (type == 2) {//病例
                CaseAPIEntity cases = new CaseAPIEntity();
                cases.setTitle(mc.getTitle());
                cases.setContent(mc.getContent());
                cases.setImages(imageAPIEntities);
                entity.setCases(cases);
            } else if (type == 3) {//动态
                DynamicAPIEntity dynamic = new DynamicAPIEntity();
                dynamic.setContent(mc.getContent());
                dynamic.setImages(imageAPIEntities);
                ArticleTransmit share = mcService.getMedicalCircleForward(mc.getId());
                if (share != null) {
                    ShareAPIEntity shareAPIEntity = new ShareAPIEntity();
                    shareAPIEntity.setTitle(share.getTitle());
                    shareAPIEntity.setDesc(share.getSubtitle());
                    shareAPIEntity.setThumb(share.getPic());
                    shareAPIEntity.setUrl(share.getUrl());
                    dynamic.setShare(shareAPIEntity);
                }
                entity.setDynamic(dynamic);
            }

            List<MedicalCircleCommunity> comments = mcService.getMedicalCircleComments(circle_id, "discusstime:asc",
                    new Date(0));
            List<CommentAPIEntity> commentlist = new ArrayList<CommentAPIEntity>();
            int cfloor = 1;
            for (MedicalCircleCommunity comment : comments) {
                CommentAPIEntity commentEntity = new CommentAPIEntity();
                Doctor commentDoctor =  getDoctorByDocotrId(comment.getDoctorid());
                if(commentDoctor==null){
                    continue;
                }
                commentEntity.setAgo(TimeAgoUtils.ago(comment.getDiscusstime()));
                commentEntity.setAvatar(commentDoctor.getAvatar());
                commentEntity.setContent(comment.getContent());
                commentEntity.setFloor(mcService.getFloor(cfloor));
                commentEntity.setDoctor_id(comment.getDoctorid());
                commentEntity.setName(commentDoctor.getName());

                int rfloor = 1;
                List<CommentAPIEntity> replyEntitylist = new ArrayList<CommentAPIEntity>();
                List<MedicalCircleReply> commentReplyList = mcService.getCommentReplyList(comment.getId(), new Date(0),
                        "discusstime:asc", 5);
                for (MedicalCircleReply reply : commentReplyList) {
                    CommentAPIEntity replyEntity = new CommentAPIEntity();
                    Doctor replyDoctor =  getDoctorByDocotrId(comment.getDoctorid());
                    if(replyDoctor == null){
                        continue;
                    }
                    replyEntity.setName(replyDoctor.getName());
                    replyEntity.setDoctor_id(reply.getDoctorid());
                    replyEntity.setAgo(TimeAgoUtils.ago(reply.getDiscusstime()));
                    replyEntity.setAvatar(replyDoctor.getAvatar());
                    replyEntity.setContent(reply.getContent());
                    replyEntity.setFloor(mcService.getFloor(rfloor));
                    rfloor++;
                    replyEntitylist.add(replyEntity);
                }
                if (commentReplyList.size() < 5) {
                    commentEntity.setReply_more(false);
                } else {
                    commentEntity.setReply_more(true);
                }
                cfloor++;
                commentEntity.setReply_list(replyEntitylist);
                commentlist.add(commentEntity);
            }
            entity.setComment_list(commentlist);

            ShareAPIEntity shareEntity = new ShareAPIEntity();
            String content = mc.getContent();
            if (StringUtils.isNotBlank(content) && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            shareEntity.setTitle(StringUtils.defaultString(mc.getTitle(), "万达全程健康云"));
            shareEntity.setDesc(content);
            shareEntity.setThumb("http://img.wdjky.com/app/ic_launcher");
            shareEntity.setUrl(environment.getProperty("h5-web.connection.url") + "/doctorDetails/circle?circle_id=" + mc.getId() + "&doctor_id=" + mc.getDoctorid());
            entity.setShare(shareEntity);
        }
        return entity;
    }
    
}
