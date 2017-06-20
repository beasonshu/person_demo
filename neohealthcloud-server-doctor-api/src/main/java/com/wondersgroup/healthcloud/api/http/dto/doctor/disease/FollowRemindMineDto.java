package com.wondersgroup.healthcloud.api.http.dto.doctor.disease;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wondersgroup.common.image.utils.ImagePath;
import com.wondersgroup.healthcloud.jpa.entity.assessment.Assessment;
import com.wondersgroup.healthcloud.jpa.entity.user.RegisterInfo;
import com.wondersgroup.healthcloud.jpa.entity.user.UserInfo;
import com.wondersgroup.healthcloud.services.doctor.dto.BaseResidentDto;
import com.wondersgroup.healthcloud.utils.DateFormatter;
import com.wondersgroup.healthcloud.utils.IdcardUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.Map;

/**
 * Created by zhuchunliu on 2017/5/24.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowRemindMineDto extends BaseResidentDto {
    private String followDate;//随访开始时间
    private String followDetailUrl;//随访详情

    public FollowRemindMineDto(Map<String, Object> map,  RegisterInfo register, UserInfo userInfo,String baseUrl) {

        this.setRegisterId(map.get("registerid").toString());
        this.setAvatar(StringUtils.isEmpty(register.getHeadphoto())?"":register.getHeadphoto()+ ImagePath.avatarPostfix());
        this.setName(register.getName());
        this.setGender(register.getGender());
        this.setIdentifyType("0".equals(register.getIdentifytype()) ?false:true);
        this.setIsRisk(null == map.get("is_risk") || "0".equals(map.get("is_risk").toString()) ?false:true);
        this.setDiabetesType(null == map.get("diabetes_type") || "0".equals(map.get("diabetes_type").toString()) ?false:true);
        this.setHypType(null == map.get("hyp_type") || "0".equals(map.get("hyp_type").toString()) ?false:true);
        this.setApoType(null == map.get("apo_type") || "0".equals(map.get("apo_type").toString()) ?false:true);
        this.setSignStatus(null == map.get("sign_status") || "0".equals(map.get("sign_status").toString()) ?false:true);


        if(null != register && null != register.getPersoncard()){
            Date birthday = DateFormatter.parseIdCardDate(IdcardUtils.getBirthByIdCard(register.getPersoncard()));
            this.setAge( new DateTime().getYear() - new DateTime(birthday).getYear());
        }else if(null != userInfo){
            this.setAge(userInfo.getAge());
        }else if(null != register && null != register.getBirthday()){
            this.setAge(new DateTime().getYear() - new DateTime(register.getBirthday()).getYear());
        }
        if(null != map.get("report_date")){
            this.followDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S").parseDateTime(map.get("report_date").toString()).toString("yyyy-MM-dd");
        }
        this.followDetailUrl = baseUrl+"/FollowUpReport/"+this.getRegisterId()+"/"+this.followDate;

    }
}
