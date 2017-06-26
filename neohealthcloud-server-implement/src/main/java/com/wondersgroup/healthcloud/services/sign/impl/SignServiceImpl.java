package com.wondersgroup.healthcloud.services.sign.impl;

import com.google.common.collect.Lists;
import com.wondersgroup.healthcloud.dict.DictCache;
import com.wondersgroup.healthcloud.jpa.constant.CommonConstant;
import com.wondersgroup.healthcloud.jpa.entity.diabetes.DoctorTubeSignUser;
import com.wondersgroup.healthcloud.jpa.entity.group.SignUserDoctorGroup;
import com.wondersgroup.healthcloud.jpa.entity.user.Address;
import com.wondersgroup.healthcloud.jpa.repository.group.SignUserDoctorGroupRepository;
import com.wondersgroup.healthcloud.jpa.repository.user.AddressRepository;
import com.wondersgroup.healthcloud.services.disease.constant.DiseaseTypeConstant;
import com.wondersgroup.healthcloud.services.disease.constant.PeopleTypeConstant;
import com.wondersgroup.healthcloud.services.disease.constant.ResidentConstant;
import com.wondersgroup.healthcloud.services.sign.SignDTO;
import com.wondersgroup.healthcloud.services.sign.SignService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ZZX on 2017/6/9.
 */
@Service
public class SignServiceImpl implements SignService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private SignUserDoctorGroupRepository sudgRepo;

    @Autowired
    private DictCache dictCache;

    @Override
    public List<SignDTO> userLists(String personcard, String name, String diseaseType, String peopleType, int pageNo, int pageSize) {

        StringBuffer sql = new StringBuffer();

        // SELECT
        sql.append("SELECT a.* FROM fam_doctor_tube_sign_user a");

        // WHERE
        sql.append(" WHERE a.sign_status = '1'");

        // personcard
        if (StringUtils.isNotEmpty(personcard)) {
            sql.append(" AND a.sign_doctor_personcard = '" + personcard + "'");
        }

        // name 搜索
        if (StringUtils.isNotEmpty(name)) {
            sql.append(" AND LOCATE('" + name + "', a.name) > 0");
        }
        // 慢病种类搜索
        String apo = null, diabetes = null, hyp = null;
        if (StringUtils.isNotEmpty(diseaseType)) {
            String[] diseaseArray = diseaseType.split(",");
            for (String s : diseaseArray) {
                switch (s) {
                    case DiseaseTypeConstant.APO:
                        apo = "1";
                        break;
                    case DiseaseTypeConstant.DIABETES:
                        diabetes = "1";
                        break;
                    case DiseaseTypeConstant.HYP:
                        hyp = "1";
                        break;
                }
            }
        }
        // 人群分类搜索
        if (StringUtils.isNotBlank(peopleType)) {
            switch (peopleType) {
                case PeopleTypeConstant.RISK:// 高危人群
                    sql.append(" AND a.is_risk = '1'");
                    if ("1".equals(apo))
                        sql.append(" AND a.apo_c_type = 1");
                    if ("1".equals(diabetes))
                        sql.append(" AND a.diabetes_c_type = 1");
                    if ("1".equals(hyp))
                        sql.append(" AND a.hyp_c_type = 1");
                    break;
                case PeopleTypeConstant.DISEASE:// 疾病人群
                    if (apo == null && diabetes == null && hyp == null) {
                        sql.append(" AND (a.apo_type = '1' OR a.diabetes_type = '1' OR a.hyp_type = '1')");
                    } else {
                        if ("1".equals(apo))
                            sql.append(" AND a.apo_type = '1'");
                        if ("1".equals(diabetes))
                            sql.append(" AND a.diabetes_type = '1'");
                        if ("1".equals(hyp))
                            sql.append(" AND a.hyp_type = '1'");
                    }
                    break;
                case PeopleTypeConstant.HEALTHY:// 健康人群
                    if (StringUtils.isNotEmpty(diseaseType)) {
                        return null;
                    }
                    sql.append(" AND a.apo_type <> '1' AND a.diabetes_type <> '1' AND a.hyp_type <> '1' AND a.is_risk <> '1'");
                    break;
            }
        } else {
            if (apo != null || diabetes != null || hyp != null) {
                if ("1".equals(apo))
                    sql.append(" AND (a.apo_type = '1' OR a.apo_c_type = 1)");
                if ("1".equals(diabetes))
                    sql.append(" AND (a.diabetes_type = '1' OR a.diabetes_c_type = 1)");
                if ("1".equals(hyp))
                    sql.append(" AND (a.hyp_type = '1' OR a.hyp_c_type = 1)");
            }
        }

        // ORDER BY
        sql.append(" ORDER BY CONVERT(a.name USING gbk) COLLATE gbk_chinese_ci");
        if (StringUtils.isNotEmpty(name)) {
            sql.append(", LOCATE('" + name + "', a.name), length(a.name)");
        }

        // LIMIT
        sql.append(" LIMIT " + (pageNo * (pageSize - 1)) + ", " + pageSize);
        List<DoctorTubeSignUser> rtnList = jdbcTemplate.query(sql.toString(), new Object[]{}, new BeanPropertyRowMapper<>(DoctorTubeSignUser.class));

        if (rtnList != null && rtnList.size() > 0) {
            List<SignDTO> dtoList = Lists.newArrayList();
            for (DoctorTubeSignUser doctorTubeSignUser : rtnList) {
                SignDTO signDTO = copyResidentInfo(doctorTubeSignUser);
                dtoList.add(signDTO);
            }
            return dtoList;
        }
        return null;
    }

    private SignDTO copyResidentInfo(DoctorTubeSignUser dtsu) {
        SignDTO dto = new SignDTO();
        BeanUtils.copyProperties(dtsu, dto);
        dto.setRegisterId(dtsu.getId());

        // 高糖脑危标签设置
        if (StringUtils.isNotBlank(dtsu.getHypType())) {
            if (!ResidentConstant.NORMAL.equals(dtsu.getHypType())) {
                dto.setHypType(true);
            }
        }
        if (StringUtils.isNotBlank(dtsu.getDiabetesType())) {
            if (!ResidentConstant.NORMAL.equals(dtsu.getDiabetesType())) {
                dto.setDiabetesType(true);
            }
        }
        if (StringUtils.isNotBlank(dtsu.getApoType())) {
            if (!ResidentConstant.NORMAL.equals(dtsu.getApoType())) {
                dto.setApoType(true);
            }
        }
        if ("1".equals(dtsu.getIsRisk())) {
            dto.setIsRisk(true);
        }
        // 是否签约
        if (StringUtils.isNotBlank(dtsu.getSignStatus())) {
            if (ResidentConstant.IDENTIFIED.equals(dtsu.getSignStatus())) {
                dto.setSignStatus(true);
            }
        }

        // 是否实名
        if (StringUtils.isNotBlank(dtsu.getIdentifytype())) {
            if (!ResidentConstant.NORMAL.equals(dtsu.getIdentifytype())) {
                dto.setIdentifyType(true);
            }
        }

        // 设置地址信息
        Address address = addressRepository.queryFirst1ByDelFlagAndPersoncard(dtsu.getCardNumber());
        if (address != null) {
            String adr = String.format("%s%s%s%s%s%s",
                    StringUtils.trimToEmpty(dictCache.queryArea(address.getProvince())),
                    StringUtils.trimToEmpty(dictCache.queryArea(address.getCity())),
                    StringUtils.trimToEmpty(dictCache.queryArea(address.getCounty())),
                    StringUtils.trimToEmpty(dictCache.queryArea(address.getTown())),
                    StringUtils.trimToEmpty(dictCache.queryArea(address.getCommittee())),
                    StringUtils.trimToEmpty(address.getOther()));
            dto.setAddress(adr);
        }
        // 是否分组
        SignUserDoctorGroup signUserDoctorGroup = sudgRepo.queryFirst1ByDelFlagAndUid(CommonConstant.USED_DEL_FLAG, dtsu.getId());
        if (signUserDoctorGroup != null) {
            dto.setIfGrouped(true);
        } else {
            dto.setIfGrouped(false);
        }

        return dto;
    }
}
