package com.wondersgroup.healthcloud.services.step;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.wondersgroup.healthcloud.common.utils.DateUtils;

@Service
@Transactional(readOnly = true)
public class StepCountService {

	@Value("${internal.api.service.measure.url}")
	private String host;

	/**
	 * 查询昨日可领取金币
	 * <p>
	 * 默认规则：步数/200，最多可领取100金币
	 * </p>
	 * 
	 * @param userId
	 * @return
	 */
	public Integer findAwardGold(String userId) {
		Date date = DateUtils.addDay(new Date(), -1);

		JsonNode jsonNode = findStepByUserIdAndDate(userId, date);
		JsonNode step = jsonNode.get("data");
		int stepcount = step.get("stepCount").asInt(0);

		int gold = stepcount / 200;
		return gold > 100 ? 100 : gold;
	}

	public boolean isActivityTime(Date date) {
		String var = DateUtils.format(date, "yyyy-MM-dd ");
		
		Date startTime = DateUtils.parseString(var + "00:00:00", "yyyy-MM-dd HH:mm:ss");
		Date endTime = DateUtils.parseString(var + "23:59:59", "yyyy-MM-dd HH:mm:ss");
		
		if(startTime.compareTo(date) > 0){
			return false;
		}else if(date.compareTo(endTime) > 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 查询指定日期的计步数据
	 * 
	 * @param userId
	 * @param date
	 * @return
	 */
	public JsonNode findStepByUserIdAndDate(String userId, Date date) {
		RestTemplate restTemplate = new RestTemplate();
		String url = host + "/api/stepcount?userId={userId}&date={date}";
		return restTemplate.getForObject(url, JsonNode.class, userId, DateUtils.format(date, "yyyy-MM-dd"));
	}

	/**
	 * 查询历史计步数据
	 * 
	 * @param userId
	 * @return
	 */
	public JsonNode findHistoryStep(String userId) {
		RestTemplate restTemplate = new RestTemplate();
		String url = host + "/api/stepcount/history?userId={userId}";
		return restTemplate.getForObject(url, JsonNode.class, userId);
	}

	/**
	 * 新增或更新计步数据
	 * 
	 * @param request
	 * @return
	 */
	public JsonNode save(Map<String, Object> request) {
		RestTemplate restTemplate = new RestTemplate();
		String url = host + "/api/stepcount";
		return restTemplate.postForObject(url, request, JsonNode.class);
	}

}