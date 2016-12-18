package com.wondersgroup.healthcloud.jpa.entity.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;
import javax.persistence.*;

/**
 * Created by zhaozhenxing on 2016/12/07.
 */

@Data
@Entity
@Table(name = "app_tb_doctor_intervention")
public class DoctorIntervention {
    @Id
    @Column(name = "id")
    private String id;// 流水ID
    @Column(name = "doctor_id")
    private String doctorId;// 医生ID
    @Column(name = "patient_id")
    private String patientId;// 患者ID
    @Column(name = "type")
    private String type;// 建议类型 1:异常血糖干预
    @Column(name = "content")
    private String content;// 医生建议内容
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
    private Date createTime;// 创建时间
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
    private Date updateTime;// 更新时间
    @Column(name = "del_flag")
    private String delFlag;// 删除标示,0-未删除,1-已删除
}