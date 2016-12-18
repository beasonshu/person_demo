package com.wondersgroup.healthcloud.services.diabetes.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.wondersgroup.common.http.HttpRequestExecutorManager;
import com.wondersgroup.common.http.builder.RequestBuilder;
import com.wondersgroup.common.http.entity.JsonNodeResponseWrapper;
import com.wondersgroup.healthcloud.services.diabetes.DiabetesService;
import com.wondersgroup.healthcloud.services.diabetes.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 调用web端慢病接口方法
 * Created by zhuchunliu on 2016/12/8.
 */
@Service("diabetesService")
public class DiabetesServiceImpl implements DiabetesService {

    private Logger logger = LoggerFactory.getLogger("ex");
    @Autowired
    private Environment environment;

    private String url; //   "http://10.1.93.111:8380/hds";

    private final static HttpRequestExecutorManager httpRequestExecutorManager = new HttpRequestExecutorManager(new OkHttpClient());

    private final static String TUBE_PATIENT_NUMBER="/api/diabetes/management/num";//在管人数
    private final static String TUBE_PATIENT_LIST="/api/diabetes/management/list";//在管人群列表
    private final static String TUBE_PATIENT_DETAIL="/api/diabetes/management";//在管人群详情
    private final static String REPORT_SCREENING_LIST = "/api/diabetes/filter";//筛查报告列表
    private final static String REPORT_INSPECT_LIST = "/api/inspection/reports";//检查报告列表
    private final static String REPORT_INSPECT_DETAIL = "/api/inspection/report";//检查报告详情
    private final static String REPORT_FOLLOW_LIST = "/api/diabetes/follow";//随访报告列表

    @PostConstruct
    private void setUrl(){
        this.url = environment.getProperty("diabetes.web.url");
    }
    /**
     * 根据医生获取在管人群数
     * @param hospitalCode
     * @param doctorName
     * @return
     */
    @Override
    public Integer getTubePatientNumber(String hospitalCode, String doctorName,String patientName) {

        String[] param = new String[]{"hospitalId",hospitalCode,"docName",doctorName};
        if(null != patientName && !StringUtils.isEmpty(patientName)){
            param = new String[]{"hospitalId",hospitalCode,"docName",doctorName,"name",patientName};
        }

        Request request = new RequestBuilder().get().url(url+this.TUBE_PATIENT_NUMBER).params(param).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            return jsonNode.get("data").asInt();
        }
        return 0;
    }

    /**
     * 在管人群列表
     * @param hospitalCode
     * @param doctorName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public List<TubePatientDTO> getTubePatientList(String hospitalCode, String doctorName, String patientName,Integer pageNo , Integer pageSize) {
        String[] param = new String[]{"hospitalId",hospitalCode,"docName",doctorName};
        if(null != patientName && !StringUtils.isEmpty(patientName)){
            param = new String[]{"hospitalId",hospitalCode,"docName",doctorName,"name",patientName};
        }
        Request request = new RequestBuilder().get().url(url+this.TUBE_PATIENT_LIST).params(param).build();

        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, TubePatientDTO.class);
            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), javaType);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    /**
     * 根据身份证获取在管人群详情
     */
    @Override
    public TubePatientDetailDTO getTubePatientDetail(String cardType,String cardNumber){
        Request request = new RequestBuilder().get().url(url+this.TUBE_PATIENT_DETAIL).
                params(new String[]{"personcardType",cardType,"personcardNo",cardNumber}).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), TubePatientDetailDTO.class);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    /**
     * 筛查报告
     * @return
     * @throws Exception
     */
    @Override
    public List<ReportScreeningDTO> getReportScreening(String cardType,String cardNumber){
        Request request = new RequestBuilder().get().url(url+this.REPORT_SCREENING_LIST).
                params(new String[]{"personcardType",cardType,"personcardNo",cardNumber}).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ReportScreeningDTO.class);
            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), javaType);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    /**
     * 检查报告
     * @return
     * @throws Exception
     */
    @Override
    public List<ReportInspectDTO> getReportInspectList(String cardType,String cardNumber){
        Request request = new RequestBuilder().get().url(url+this.REPORT_INSPECT_LIST).
                params(new String[]{"personcardType",cardType,"personcardNo",cardNumber}).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ReportInspectDTO.class);
            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), javaType);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    /**
     * 检查报告详情
     * @return
     * @throws Exception
     */
    @Override
    public List<ReportInspectDetailDTO> getReportInspectDetail(String reportNum,Date reportDate) {
        Request request = new RequestBuilder().get().url(url+this.REPORT_INSPECT_DETAIL).
                params(new String[]{"reportNo",reportNum,"reportDate",new DateTime(reportDate).toString("yyyyMMdd")}).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ReportInspectDetailDTO.class);
            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), javaType);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    /**
     * 随访报告详情
     * @return
     * @throws Exception
     */
    @Override
    public List<ReportFollowDTO> getReportFollowList(String cardType,String cardNumber){
        Request request = new RequestBuilder().get().url(url+this.REPORT_FOLLOW_LIST).
                params(new String[]{"personcardType",cardType,"personcardNo",cardNumber}).build();
        JsonNodeResponseWrapper response = (JsonNodeResponseWrapper)httpRequestExecutorManager.newCall(request).run().as(JsonNodeResponseWrapper.class);
        JsonNode jsonNode = response.convertBody();
        if(200 == response.code() && 0 == jsonNode.get("code").asInt()){
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ReportFollowDTO.class);

            try {
                return new ObjectMapper().readValue(jsonNode.get("data").toString(), javaType);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
        return null;
    }

    public static void main(String[] args){
        DiabetesServiceImpl diabetesService = new DiabetesServiceImpl();
        diabetesService.url = "http://10.1.93.111:8380/hds";
//        int total = diabetesService.getTubePatientNumber("42509835700","王庆杰");
//        System.err.println(total);
//
//        List<TubePatientDTO> list = diabetesService.getTubePatientList("42509835700","王庆杰",1,10);
//        System.err.println(list.size());
//
//        TubePatientDetailDTO detailDTO = diabetesService.getTubePatientDetail("01","310110193606134623");
//        System.err.println(detailDTO.getName()+"  "+detailDTO.getHospitalCode()+"   "+detailDTO.getDoctorName());

        List<ReportScreeningDTO> screening = diabetesService.getReportScreening("01","420621198811200612");
        System.err.println(screening.size());
//
//        List<ReportInspectDTO> inspect = diabetesService.getReportInspectList("123456789");
//        System.err.println(inspect.size());
//
//        List<ReportInspectDetailDTO> inspectDetail = diabetesService.getReportInspectDetail("4250983570010000000001",new DateTime("20160907").toDate());
//        System.err.println(inspectDetail.size());
//
//        List<ReportFollowDTO> follow = diabetesService.getReportFollowList("310223195206141425");
//        System.err.println(follow.size());

    }
}