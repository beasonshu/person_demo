package com.wondersgroup.healthcloud.services.doctor.impl;

import com.wondersgroup.healthcloud.common.utils.IdGen;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorTemplate;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorUsedTemplate;
import com.wondersgroup.healthcloud.jpa.repository.doctor.DoctorTemplateRepository;
import com.wondersgroup.healthcloud.jpa.repository.doctor.DoctorUsedTemplateRepository;
import com.wondersgroup.healthcloud.services.doctor.DoctorTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DoctorTemplateServiceImpl implements DoctorTemplateService {

    private DoctorTemplateRepository doctorTemplateRepository;
    @Autowired
    private DoctorUsedTemplateRepository doctorUsedTemplateRepository;

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<DoctorTemplate> findByDoctorIdAndType(String doctorId, String type) {
//        return doctorTemplateRepository.findByDoctorIdAndType(doctorId, type);
        int defaultCount = findDefaultDoctorTemplateCount(doctorId, "0");
        if (defaultCount <= 0) { //设置默认模板
            DoctorTemplate entity = new DoctorTemplate();
            entity.setTitle("默认模板");
            entity.setContent("您好,您血糖首次异常,请及时到医院就诊或咨询医生");
            entity.setDoctorId(doctorId);
            entity.setType("0");
            entity.setUpdateTime(new Date());
            entity.setCreateTime(new Date());
            saveTemplate(entity);
        }
        return doctorTemplateRepository.findByDoctorId(doctorId);
    }

    @Override
    public DoctorTemplate update(String id, String doctorId, String type, String title, String content) {
        DoctorTemplate toSave;
        if (id == null) {
            if (doctorId == null) {
                throw new RuntimeException();
            }
            toSave = new DoctorTemplate();
            toSave.setId(IdGen.uuid());
            toSave.setDoctorId(doctorId);
        } else {
            toSave = doctorTemplateRepository.findOne(id);
        }
        toSave.setTitle(title);
        toSave.setContent(content);
//        toSave.setType(type);
        toSave.setUpdateTime(new Date());

        return doctorTemplateRepository.save(toSave);
    }

    @Override
    public DoctorTemplate findOne(String id) {
        return doctorTemplateRepository.findOne(id);
    }

    @Override
    public void deleteOne(String id) {
//        doctorTemplateRepository.delete(id);

        DoctorTemplate toSave = null;
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException();
        } else {
            toSave = doctorTemplateRepository.findOne(id);
        }

        if (null == toSave) {
            throw new RuntimeException();
        }

        toSave.setUpdateTime(new Date());
        toSave.setDelFlag("1");
        doctorTemplateRepository.save(toSave);
    }

    @Override
    public void saveTemplate(DoctorTemplate entity) {
        if (StringUtils.isBlank(entity.getId())) {
            entity.setId(IdGen.uuid());
            entity.setDelFlag("0");
        }
        doctorTemplateRepository.save(entity);
    }

    @Override
    public void saveDoctorUsedTemplate(DoctorUsedTemplate entity) {
        if (StringUtils.isBlank(entity.getDoctorId()) || StringUtils.isBlank(entity.getTemplateId())) {
            return;
        }
        if (StringUtils.isBlank(entity.getId())) {
            entity.setId(IdGen.uuid());
        }
        doctorUsedTemplateRepository.save(entity);
    }

    @Override
    public List<DoctorTemplate> findLastUsedTemplate(String doctorId) {
        final String sql = "select c.* from app_tb_doctor_used_template as a  " +
                "left join app_tb_doctor_template as c on a.template_id = c.id where NOT EXISTS( " +
                " select 1 from app_tb_doctor_used_template as b where b.doctor_id = a.doctor_id " +
                " and a.template_id = b.template_id " +
                " and b.create_time > a.create_time " +
                " and b.doctor_id = '" + doctorId + "' " +
                ") " +
                " and c.del_flag = '0' " +
                "and a.doctor_id = '" + doctorId + "' " +
                "ORDER BY a.create_time desc limit 0,3";


        List<DoctorTemplate> list = template.query(sql, new RowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                DoctorTemplate entity = null;
                String id = rs.getString("id");
                if (StringUtils.isNotBlank(id)) {
                    entity = new DoctorTemplate();
                    entity.setId(id);
                    entity.setDoctorId(rs.getString("doctor_id"));
                    entity.setTitle(rs.getString("title"));
                    entity.setContent(rs.getString("content"));
                    entity.setCreateTime(rs.getTime("create_time"));
                }
                return entity;
            }
        });
        return list;
    }

    @Override
    public Integer findDoctorTemplateCount(String doctorId) {
        Integer count = template.queryForObject("select count(1) from app_tb_doctor_template as a where a.doctor_id = '" + doctorId + "' and a.del_flag = '0' ", Integer.class);
        return count;
    }

    @Override
    public Integer findDefaultDoctorTemplateCount(String doctorId, String type) {
        Integer count = template.queryForObject("select count(1) from app_tb_doctor_template as a where a.doctor_id = '" + doctorId + "' and a.type  = '" + type + "' ", Integer.class);
        return count;
    }


    @Autowired
    public void setDoctorTemplateRepository(DoctorTemplateRepository doctorTemplateRepository) {
        this.doctorTemplateRepository = doctorTemplateRepository;
    }
}
