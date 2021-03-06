package com.wondersgroup.healthcloud.api.http.dto.doctor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorTemplate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorTemplateDTO {

    public String id;
    @JsonProperty("doctor_id")
    public String doctorId;
    public String type;
    public String title;
    public String content;


    public DoctorTemplateDTO() {

    }

    public DoctorTemplateDTO(DoctorTemplate template) {
        this.id = template.getId();
        this.doctorId = template.getDoctorId();
        this.type = template.getType();
        this.title = template.getTitle();
        this.content = template.getContent();
    }
}
