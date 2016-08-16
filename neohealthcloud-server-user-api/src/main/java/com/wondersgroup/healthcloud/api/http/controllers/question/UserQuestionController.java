package com.wondersgroup.healthcloud.api.http.controllers.question;

import com.wondersgroup.healthcloud.common.http.annotations.WithoutToken;
import com.wondersgroup.healthcloud.common.http.dto.JsonListResponseEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.version.VersionRange;
import com.wondersgroup.healthcloud.common.utils.PropertiesUtils;
import com.wondersgroup.healthcloud.jpa.entity.question.Question;
import com.wondersgroup.healthcloud.jpa.entity.question.Reply;
import com.wondersgroup.healthcloud.jpa.entity.question.ReplyGroup;
import com.wondersgroup.healthcloud.services.question.QuestionService;
import com.wondersgroup.healthcloud.services.question.dto.QuestionDetail;
import com.wondersgroup.healthcloud.services.question.dto.QuestionInfoForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
public class UserQuestionController {

	@Autowired
	private QuestionService questionService;

	/**
	 * 提问
	 * @param question
	 * @return
	 */
	@VersionRange
	@WithoutToken
	@RequestMapping(value="/ask",method= RequestMethod.POST)
	public Object ask(@RequestBody Question question){
		JsonResponseEntity<Object> response=new JsonResponseEntity<>();
		String id="";
		question.setAnswerId("");
		id=questionService.saveQuestion(question);
		//PropertiesUtils.get("");

		response.setMsg("您的问题已提交，请耐心等待医生回复");
		return response;
	}

	/**
	 * 问诊列表
	 * @param flag
	 * @param userId
	 * @return
	 */
	@VersionRange
	@RequestMapping(value="/list",method= RequestMethod.GET)
	public Object getQuestionInfoList(@RequestParam(required = false, defaultValue = "0") Integer flag,
            @RequestParam String userId){
		JsonListResponseEntity<QuestionInfoForm> response=new JsonListResponseEntity<>();
		List<QuestionInfoForm> data=questionService.queryQuerstionList(userId, flag);
		
		List<QuestionInfoForm> list=questionService.queryQuerstionList(userId, flag+1);		
		if(null != list&&list.size()>0){
			response.setContent(data, true, "", String.valueOf(flag + 1));
		}else{
			response.setContent(data, false, "", "");
		}
			
		return response;
	}
	/**
	 * 问诊详情
	 * @param questionId
	 * @return
	 */
	@VersionRange
	@RequestMapping(value="/detail",method= RequestMethod.GET)
	public Object getQuestionInfoList(@RequestParam String questionId){
		JsonResponseEntity<Object> response=new JsonResponseEntity<>();
		QuestionDetail data=questionService.queryQuestionDetail(questionId);
		response.setData(data);
		return response;
	}
	/**
	 * 回复医生回答
	 * @param reply
	 * @return
	 */
	@VersionRange
	@RequestMapping(value="/reply",method= RequestMethod.POST)
	public Object reply(@RequestBody Reply reply){
		JsonResponseEntity<Object> response=new JsonResponseEntity<>();
		questionService.saveReplay(reply);		
		ReplyGroup group=questionService.queryAnswerId(reply.getGroupId());
		String doctorId=group.getAnswer_id();

		Map<String,String> extras=new HashMap<>();
		String scam=String.format("com.wondersgroup.hs.healthja://doctor/question_detail?doctorId=%s&questionId=%s", doctorId, group.getQuestion_id());
		extras.put("page",scam);
		extras.put("for_type", "question_detail");
		response.setMsg("回复成功");
		return response;
	}
	/**
	 * 是否有新的未看回复
	 * @param userId
	 * @return
	 */
	@VersionRange
	@RequestMapping(value="/hasNewReply",method= RequestMethod.GET)
	public JsonResponseEntity<Boolean> hasNewReply(@RequestParam String userId){
		JsonResponseEntity<Boolean> response=new JsonResponseEntity<>();
		Boolean is=questionService.queryHasNewReply(userId);
		response.setData(is);
		return response;
	}
}
