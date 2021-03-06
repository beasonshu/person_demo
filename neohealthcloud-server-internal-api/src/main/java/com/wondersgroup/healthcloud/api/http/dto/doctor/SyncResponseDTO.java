package com.wondersgroup.healthcloud.api.http.dto.doctor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorAccount;


/**
 * Created by longshasha on 16/8/2.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncResponseDTO {

    @JsonProperty("register_id")
    private String registerId;

    private String talkid;

    private String talkpwd;

    private String talkgroupid;

    public SyncResponseDTO(DoctorAccount doctorAccount) {
        this.setRegisterId(doctorAccount.getId());
        this.setTalkid(doctorAccount.getTalkid());
        this.setTalkpwd(doctorAccount.getTalkpwd());
        this.setTalkgroupid(doctorAccount.getTalkgroupid());
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getTalkid() {
        return talkid;
    }

    public void setTalkid(String talkid) {
        this.talkid = talkid;
    }

    public String getTalkpwd() {
        return talkpwd;
    }

    public void setTalkpwd(String talkpwd) {
        this.talkpwd = talkpwd;
    }

    public String getTalkgroupid() {
        return talkgroupid;
    }

    public void setTalkgroupid(String talkgroupid) {
        this.talkgroupid = talkgroupid;
    }
}
