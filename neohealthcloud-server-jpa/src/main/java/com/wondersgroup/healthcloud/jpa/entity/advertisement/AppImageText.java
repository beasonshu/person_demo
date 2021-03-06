package com.wondersgroup.healthcloud.jpa.entity.advertisement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by zhaozhenxing on 2016/8/16.
 */
@Data
@Entity
@Table(name = "app_tb_neoimage_text")
public class AppImageText {
    @Id
    private String id;
    @Column(name = "main_title")
    private String mainTitle;
    @Column(name = "sub_title")
    private String subTitle;
    @Column(name = "img_url")
    private String imgUrl;
    private String hoplinks;
    @Column(name = "start_time")
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    private Integer durations;
    @Column(name = "allow_close")
    private Integer allowClose;
    @Column(name = "del_flag")
    private Integer delFlag;
    private Integer adcode;
    private String sequence;
    private String position;
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date update_time;
    @Column(name = "main_area")
    private String mainArea;
    @Column(name = "spec_area")
    private String specArea;
}
