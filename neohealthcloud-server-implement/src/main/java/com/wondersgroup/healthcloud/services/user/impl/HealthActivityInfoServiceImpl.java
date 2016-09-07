package com.wondersgroup.healthcloud.services.user.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wondersgroup.healthcloud.jpa.entity.activity.HealthActivityDetail;
import com.wondersgroup.healthcloud.jpa.entity.activity.HealthActivityInfo;
import com.wondersgroup.healthcloud.jpa.repository.activity.HealthActivityDetailRepository;
import com.wondersgroup.healthcloud.jpa.repository.activity.HealthActivityInfoRepository;
import com.wondersgroup.healthcloud.services.user.HealthActivityInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Service()
public class HealthActivityInfoServiceImpl implements HealthActivityInfoService {
    @Autowired
    HealthActivityInfoRepository   activityRepo;

    @Autowired
    HealthActivityDetailRepository activityDetailRepo;

    @Autowired
    private DataSource             dataSource;

    private JdbcTemplate           jt;

    @Override
    public List<HealthActivityInfo> getHealthActivityInfos(String area, String type) {
        if (type.equals("0")) {
            //如果type=0只筛选区域，否则筛选条件为区域和类型
            return activityRepo.findActivitiesByArea(area);
        } else {
            return activityRepo.findActivitiesByAreaAndType(area, type);
        }
    }

    @Override
    public HealthActivityInfo getHealthActivityInfo(String activityid) {
        return activityRepo.findOne(activityid);
    }

    @Override
    public HealthActivityDetail findActivityDetailByAidAndRid(String activityId, String registerId) {
        return activityDetailRepo.findActivityDetailByAidAndRid(activityId, registerId);
    }

    @Override
    public List<HealthActivityInfo> getHealthActivityInfos(String province,String city, String county, Integer status, int pageNo, int pageSize) {

        String sql = "select *,case when (endtime < now()) THEN 1 else 0 end as overdue from app_tb_healthactivity_info where del_flag = '0' and online_status = 1 ";
        if(!StringUtils.isEmpty(status)){
            if(status == 1){//活动进行中
                sql += "and endtime >= now() ";
            }else if(status == 2){//活动已结束
                sql += " and endtime < now()";
            }
        }
        if(!StringUtils.isEmpty(province)){
            sql += " and province like '" + province + "%'";
        }
        if(!StringUtils.isEmpty(city)){
            sql +=  " and city like '" + city + "%'";
        }
        if (!StringUtils.isEmpty(county)) {
            sql += " and county like '" + county + "%'";
        }
        
        sql += " ORDER BY overdue asc ,starttime desc limit " + (pageNo - 1) * pageSize + "," + (pageSize);
        List<Map<String, Object>> resourceList = getJt().queryForList(sql);
        List<HealthActivityInfo> list = Lists.newArrayList();
        for (Map<String, Object> map : resourceList) {
            list.add(new Gson().fromJson(new Gson().toJson(map), HealthActivityInfo.class));
        }
        return list;
    }

    /**
     * 获取jdbc template
     * @return
     */
    private JdbcTemplate getJt() {
        if (jt == null) {
            jt = new JdbcTemplate(dataSource);
        }
        return jt;
    }

    @Override
    public List<HealthActivityInfo> getHealthActivityInfos(String status, String title, String onlineTime, String offlineTime, int pageNo, int pageSize) {
        String sql = "select * from app_tb_healthactivity_info where del_flag = '0'";
        if(!StringUtils.isEmpty(status)){
            sql += " and online_status = '" + status + "'";
        }
        if(!StringUtils.isEmpty(onlineTime)){
            sql += " and online_time >= '" + onlineTime + "'";
        }
        if(!StringUtils.isEmpty(offlineTime)){
            sql += " and offline_time <= '" + offlineTime + "'";
        }
        if(!StringUtils.isEmpty(title)){
            sql += " and title like '%" + title + "%'";
        }
        sql += " ORDER BY update_date desc limit " + (pageNo - 1) * pageSize + "," + (pageSize);
        List<Map<String, Object>> resourceList = getJt().queryForList(sql);
        List<HealthActivityInfo> list = Lists.newArrayList();
        for (Map<String, Object> map : resourceList) {
            list.add(new Gson().fromJson(new Gson().toJson(map), HealthActivityInfo.class));
        }
        return list;
    }

    @Override
    public int getHealthActivityInfoCount(String status, String title, String onlineTime, String offlineTime) {
        String sql = "select count(*) from app_tb_healthactivity_info where del_flag = '0'";
        if(!StringUtils.isEmpty(status)){
            sql += " and online_status = '" + status + "'";
        }
        if(!StringUtils.isEmpty(onlineTime)){
            sql += " and online_time >= '" + onlineTime + "'";
        }
        if(!StringUtils.isEmpty(offlineTime)){
            sql += " and offline_time <= '" + offlineTime + "'";
        }
        if(!StringUtils.isEmpty(title)){
            sql += " and title like '%" + title + "%'";
        }
        return getJt().queryForObject(sql,Integer.class);
    }

}
