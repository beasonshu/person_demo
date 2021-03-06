package com.wondersgroup.healthcloud.api.http.controllers.template;

import com.google.common.collect.Lists;
import com.wondersgroup.healthcloud.api.http.dto.doctor.template.MyTemplateDTO;
import com.wondersgroup.healthcloud.api.http.dto.doctor.template.TemplateDTO;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.misc.JsonKeyReader;
import com.wondersgroup.healthcloud.common.http.support.version.VersionRange;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorTemplate;
import com.wondersgroup.healthcloud.services.doctor.DoctorTemplateService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/doctor/template")
public class DoctorTemplateController {

    @Autowired
    private DoctorTemplateService doctorTemplateService;

    private String doctorTemplateType = "1";

    public static final int LIMIT_COUNT = 30;



    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @VersionRange
    public JsonResponseEntity<MyTemplateDTO> listAll(@RequestParam("doctorId") String doctorId) {
        JsonResponseEntity<MyTemplateDTO> response = new JsonResponseEntity<>();
        List<DoctorTemplate> templates = doctorTemplateService.findByDoctorIdAndType(doctorId, doctorTemplateType);
        MyTemplateDTO dto = new MyTemplateDTO();
        dto.setTotalCount(LIMIT_COUNT);
        dto.setCurrentIndex(CollectionUtils.isEmpty(templates)?0:templates.size());

        List<TemplateDTO> dtos = Lists.newLinkedList();
        for (DoctorTemplate template : templates) {
            dtos.add(new TemplateDTO(template));
        }
        dto.setTemplates(dtos);

        response.setData(dto);
        return response;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @VersionRange
    public JsonResponseEntity<MyTemplateDTO> lastUsedList(@RequestParam("doctorId") String doctorId) {
        String defaultType = "1";
        JsonResponseEntity<MyTemplateDTO> response = new JsonResponseEntity<>();

        List<DoctorTemplate> templates = doctorTemplateService.findByDoctorIdAndType(doctorId, defaultType);
        MyTemplateDTO dto = new MyTemplateDTO();
        dto.setTotalCount(LIMIT_COUNT);
        dto.setCurrentIndex(CollectionUtils.isEmpty(templates)?0:templates.size());

        List<TemplateDTO> lastUsed = Lists.newLinkedList();

        List<DoctorTemplate> last = doctorTemplateService.findLastUsedTemplate(doctorId);
        if(CollectionUtils.isNotEmpty(last)){
            for (DoctorTemplate template : last) {
                if(null != template){
                    lastUsed.add(new TemplateDTO(template));
                }
            }
            if(CollectionUtils.isNotEmpty(lastUsed)){
                dto.setLastUsed(lastUsed);
            }
        }


        List<TemplateDTO> dtos = Lists.newLinkedList();
        for (DoctorTemplate template : templates) {
            dtos.add(new TemplateDTO(template));
        }
        dto.setTemplates(dtos);

        response.setData(dto);
        return response;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @VersionRange
    public JsonResponseEntity<TemplateDTO> detail(@RequestParam("doctorId") String doctorId,String id) {
        DoctorTemplate template = doctorTemplateService.findOne(id);
        JsonResponseEntity<TemplateDTO> response = new JsonResponseEntity<>();
        if(null != template){
            response.setData(new TemplateDTO(template));
        }else{
            response.setCode(-1);
            response.setMsg("无数据");
        }
        return response;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @VersionRange
    public JsonResponseEntity add(@RequestBody String body) {
        JsonResponseEntity response = new JsonResponseEntity<>();
        JsonKeyReader reader = new JsonKeyReader(body);
        String doctorId = reader.readString("doctorId", false);
        String title = reader.readString("title", false);
        String content = reader.readString("content", false);

        Integer count = doctorTemplateService.findDoctorTemplateCount(doctorId);
        if(count >= LIMIT_COUNT){
            response.setMsg("添加失败,超过 "+LIMIT_COUNT+" 条");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(doctorId)){
            response.setMsg("添加失败,doctorId 不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(title)){
            response.setMsg("添加失败,title 不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(content)){
            response.setMsg("添加失败, 内容不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(title.length() > 15){
            response.setMsg("添加失败,title 长度超过15个字符 ");
            response.setCode(-1);
            return response;
        }

        if(content.length() > 300){
            response.setMsg("添加失败,title 内容长度超过300个字符 ");
            response.setCode(-1);
            return response;
        }


        DoctorTemplate entity = new DoctorTemplate();
        entity.setTitle(title);
        entity.setContent(content);
        entity.setDoctorId(doctorId);
        entity.setType(doctorTemplateType);
        entity.setUpdateTime(new Date());
        entity.setCreateTime(new Date());

        doctorTemplateService.saveTemplate(entity);
        response.setMsg("添加成功");
        response.setCode(0);
        return response;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @VersionRange
    public JsonResponseEntity edit(@RequestBody String body) {
        JsonKeyReader reader = new JsonKeyReader(body);
        String id = reader.readString("id", false);
        String doctorId = reader.readString("doctorId", false);
        String title = reader.readString("title", false);
        String content = reader.readString("content", false);
        JsonResponseEntity response = new JsonResponseEntity<>();


        if(StringUtils.isBlank(id)){
            response.setMsg("编辑失败,id 不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(doctorId)){
            response.setMsg("编辑失败,doctorId 不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(title)){
            response.setMsg("编辑失败,title 不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(StringUtils.isBlank(content)){
            response.setMsg("编辑失败, 内容不能为空 ");
            response.setCode(-1);
            return response;
        }

        if(title.length() > 15){
            response.setMsg("编辑失败,title 长度超过15个字符 ");
            response.setCode(-1);
            return response;
        }

        if(content.length() > 300){
            response.setMsg("编辑失败,title 内容长度超过300个字符 ");
            response.setCode(-1);
            return response;
        }

        doctorTemplateService.update(id, doctorId, doctorTemplateType, title, content);

        response.setMsg("编辑成功");
        response.setCode(0);
        return response;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @VersionRange
    public JsonResponseEntity<String> delete(@RequestBody String body) {
        JsonKeyReader reader = new JsonKeyReader(body);
        String id = reader.readString("id", false);
        String doctorId = reader.readString("doctorId", false);
        doctorTemplateService.deleteOne(id);
        JsonResponseEntity<String> response = new JsonResponseEntity<>();
        response.setMsg("删除成功");
        return response;
    }

}


