package com.wondersgroup.healthcloud.api.http.controllers.doctorarticle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.wondersgroup.healthcloud.api.utils.MapToBeanUtil;
import com.wondersgroup.healthcloud.api.utils.Pager;
import com.wondersgroup.healthcloud.api.utils.PropertyFilterUtil;
import com.wondersgroup.healthcloud.common.http.annotations.Admin;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.misc.JsonKeyReader;
import com.wondersgroup.healthcloud.jpa.entity.doctorarticle.DoctorArticle;
import com.wondersgroup.healthcloud.jpa.repository.doctorarticle.DoctorArticleRepository;
import com.wondersgroup.healthcloud.services.doctor.ManageDoctorArticleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shenbin on 16/8/30.
 */
@RestController
@RequestMapping(value = "api")
public class DoctorArticleController {

    @Autowired
    private DoctorArticleRepository doctorArticleRepository;

    @Autowired
    private ManageDoctorArticleService manageDoctorArticleService;


    /**
     * 查询学院文章列表
     * @param pager
     * @return
     */
    @Admin
    @RequestMapping(value = "/doctorArticle/find", method = RequestMethod.POST)
    public Pager tabList(@RequestBody Pager pager){
        int pageNum = 1;
        if(pager.getNumber()!=0)
            pageNum = pager.getNumber();

        List<Map<String,Object>> mapList = manageDoctorArticleService.findDoctorArticleListByPager(pageNum, pager.getSize(), pager.getParameter());

        if(mapList.size()>0){
            for (Map<String,Object> map : mapList){
                String categoryIds = map.get("category_ids")==null?"":map.get("category_ids").toString();
                if(StringUtils.isNotBlank(categoryIds)){
                    String categoryNames = manageDoctorArticleService.findCategoryNamesByIds(categoryIds);
                    map.put("categoryNames",categoryNames);
                }
            }
        }

        int totalSize = manageDoctorArticleService.countDoctorArticleByParameter(pager.getParameter());
        pager.setTotalElements(totalSize);
        pager.setData(mapList);
        return pager;
    }

    /**
     * 查询学院文章详情
     * @param id
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "doctorArticleDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Admin
    public String findDoctorArticleDetail(@RequestParam int id) throws JsonProcessingException {
        DoctorArticle doctorArticle = doctorArticleRepository.findById(id);

        Map<Class, Object> filterMap = new HashMap<>();
        filterMap.put(DoctorArticle.class, new String[]{"id", "title", "category_ids", "brief", "is_visable", "thumb", "content"});
        SimpleFilterProvider filterProvider = PropertyFilterUtil.filterOutAllExceptFilter(filterMap);
        JsonResponseEntity response;
        if (doctorArticle != null) {
            response = new JsonResponseEntity(0, "查询成功", doctorArticle);
        } else {
            response = new JsonResponseEntity(-1, "查询失败");
        }

        return PropertyFilterUtil.getObjectMapper().setFilterProvider(filterProvider).writeValueAsString(response);
    }


    /**
     * 修改学院文章
     * @param
     * @return
     */
    @RequestMapping(value = "updateDoctorArticle", method = RequestMethod.POST)
    @Admin
    public JsonResponseEntity updateDoctorArticle(@RequestBody DoctorArticle doctorArticle) {
        doctorArticle.setUpdateDate(new Date());
        doctorArticleRepository.save(doctorArticle);
        return new JsonResponseEntity(0, "修改成功");
    }

    /**
     * 设置禁用与启用
     * @param request
     * @return
     */
    @PostMapping(path = "doctorArticle/setVisable")
    @Admin
    public JsonResponseEntity<String> updateDoctorArticleVisable(@RequestBody String request ){
        JsonKeyReader reader = new JsonKeyReader(request);
        int id = reader.readInteger("id", true);
        int isVisable = reader.readInteger("is_visable", true);
        JsonResponseEntity<String> response = new JsonResponseEntity<>();

        int result = doctorArticleRepository.updateDoctorArticleVisable(id,isVisable);
        if(result<=0){
            response.setCode(2001);
            response.setMsg("设置失败");
            return response;
        }
        response.setMsg("设置成功");
        return response;
    }

    /**
     * 增加阅读量
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping(value = "doctorArticle/addPv")
    @Admin
    public JsonResponseEntity<String> addPv(@RequestBody String request) throws JsonProcessingException {
        JsonKeyReader reader = new JsonKeyReader(request);
        int id = reader.readInteger("id", true);
        JsonResponseEntity<String> response = new JsonResponseEntity<>();
        DoctorArticle doctorArticle = doctorArticleRepository.findById(id);
        if(doctorArticle!=null){
            doctorArticle.setPv(doctorArticle.getPv()+1);
            doctorArticleRepository.saveAndFlush(doctorArticle);
            response.setMsg("保存成功");
        }else{
            response.setCode(3101);
            response.setMsg("保存失败");
        }

        return response;
    }

}
