package com.wondersgroup.healthcloud.jpa.entity.push;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by zhuchunliu on 2016/8/26.
 */
@Data
@Entity
@Table(name = "app_tb_push_plan")
public class PushPlan {
    @Id
    @GeneratedValue
    private Integer id;
    private String creator; // 管理员
    private String area; // 管理员对应的区域
    private String title; // 推送标题
    private String content;// 推送内容
    private String url; // 连接地址
    private Integer target_type;// 推送类型，0：个人，1：标签
    private String target; // 对应的人员或者标签主键
    private Integer status ;//'0:待审核、1:待推送、2:已推送、3:已取消、4:已驳回、5:已过期
    @Column(name="article_id")
    private Integer articleId; // 连接地址
    @Column(name = "plan_time")
    private Date planTime; //计划推送时间
    @Column(name = "create_time")
    private Date createTime; // 创建时间
    @Column(name = "update_time")
    private Date updateTime; // 更新时间
}
