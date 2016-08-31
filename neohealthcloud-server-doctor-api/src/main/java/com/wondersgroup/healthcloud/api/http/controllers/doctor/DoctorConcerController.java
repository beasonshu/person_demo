package com.wondersgroup.healthcloud.api.http.controllers.doctor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wondersgroup.healthcloud.api.http.dto.doctor.DoctorDepartmentEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonListResponseEntity;
import com.wondersgroup.healthcloud.common.http.dto.JsonResponseEntity;
import com.wondersgroup.healthcloud.common.http.support.misc.JsonKeyReader;
import com.wondersgroup.healthcloud.common.utils.AppUrlH5Utils;
import com.wondersgroup.healthcloud.jpa.entity.dic.DepartGB;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorConcerned;
import com.wondersgroup.healthcloud.services.dic.DepartGbService;
import com.wondersgroup.healthcloud.services.doctor.DoctorConcerService;
import com.wondersgroup.healthcloud.services.doctor.DoctorService;
import com.wondersgroup.healthcloud.services.doctor.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by longshasha on 16/8/31.
 */

@RestController
@RequestMapping(value = "/api/doctorConcer")
public class DoctorConcerController {

    @Autowired
    private DoctorConcerService doctorConcerService;

    @Autowired
    private DepartGbService departGbService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppUrlH5Utils appUrlH5Utils;

    /**
     * 医生二维码
     * @param doctorId
     * @return
     */
    @RequestMapping(value = "/getDocotrQRCode", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponseEntity<String> getDocotrQRCode(@RequestParam String doctorId){

        JsonResponseEntity<String> body = new JsonResponseEntity();
        try{
            String qrCodeUrl = appUrlH5Utils.buildWeiXinScan(doctorId);
            body.setData(qrCodeUrl);
        }catch (Exception e){
            e.printStackTrace();
            body.setCode(3010);
            body.setMsg("调用失败");
        }
        return body;
    }

    /**
     * 科室列表
     * @return
     */
    @RequestMapping(value = "/getDepartList", method = RequestMethod.GET)
    @ResponseBody
    public JsonListResponseEntity<DoctorDepartmentEntity> getDepartments(){

        JsonListResponseEntity<DoctorDepartmentEntity> body = new JsonListResponseEntity<>();
        try{
            List<DoctorDepartmentEntity> entities = Lists.newArrayList();
            List<DepartGB> departments = departGbService.queryFirstLevelDepartments();
            if(departments!=null&&!departments.isEmpty()){
                for(DepartGB department:departments){
                    List<DepartGB> subList = departGbService.queryDoctorDepartmentsByPid(department.getId());
                    if(subList==null||subList.isEmpty()){
                        subList = Lists.newArrayList();
                        subList.add(department);
                    }
                    DoctorDepartmentEntity entity = new DoctorDepartmentEntity(department,subList);
                    entities.add(entity);
                }
            }

            body.setContent(entities,false,null,null);
        }catch (Exception e){
            e.printStackTrace();
            body.setCode(3010);
            body.setMsg("调用失败");
        }
        return body;
    }

    /**
     * 保存我关注的领域
     * @param request
     * @return
     */
    @RequestMapping(value = "/changeDepart", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponseEntity<String> changeRelationShipBtwDoctorAndDepartment(@RequestBody String request){

        JsonResponseEntity<String> body = new JsonResponseEntity();
        try{
            JsonKeyReader reader = new JsonKeyReader(request);
            String doctorId = reader.readString("doctorId",true);
            String departmentIds = reader.readString("departmentIds",true);
            doctorConcerService.updateDoctorConcerDepartment(doctorId,departmentIds);
            body.setMsg("保存成功");
        }catch (Exception e){
            e.printStackTrace();
            body.setCode(3010);
            body.setMsg("保存失败");
        }
        return body;
    }

    @RequestMapping(value = "/getDoctorConcer", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponseEntity<Map<String,Object>> getConcerDepartment(@RequestParam(required = false) String doctorId){

        JsonResponseEntity<Map<String,Object>> body = new JsonResponseEntity();
        Map<String, Object> data = Maps.newHashMap();
        try{
            List<DepartGB> departmentList = doctorConcerService.queryDoctorDepartmentsByDoctorId(doctorId);
            List<DoctorConcerned> diseaseEntities = doctorConcerService.queryDoctorConcernedsByDoctorId(doctorId, "1");
            List<DoctorConcerned> symptoms = doctorConcerService.queryDoctorConcernedsByDoctorId(doctorId, "2");

            if(departmentList==null||departmentList.isEmpty()){
                Doctor doctorInfo = doctorService.findDoctorByUid(doctorId);
                departmentList = Lists.newArrayList();
                DepartGB department = new DepartGB();
                department.setName(doctorInfo.getDepartName());
            }

            data.put("department",departmentList);

            if(diseaseEntities!=null&&!diseaseEntities.isEmpty()){
                data.put("diseaseEntity",diseaseEntities);
            }
            if(symptoms!=null&&!symptoms.isEmpty()){
                data.put("symptom",symptoms);
            }
            body.setData(data);
        }catch (Exception e){
            e.printStackTrace();
            body.setCode(3010);
            body.setMsg("调用失败");
        }
        return body;
    }
}
