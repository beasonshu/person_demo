package com.wondersgroup.healthcloud.api.http.dto.family;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 家人信息
 * Created by sunhaidi on 2016年12月14日
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FamilyInfoDTO {
    private String id;
    private String relation_name;
    private String height;
    private String weight;
    private String birthDate;
    private String sex;
    private Integer age;
    private String mobile;
    private String nickname;
    private String avatar;
    private Boolean isStandalone;
    private Boolean isVerification;
    private Boolean isAccess;
    private Boolean isChild;
    
    
}
