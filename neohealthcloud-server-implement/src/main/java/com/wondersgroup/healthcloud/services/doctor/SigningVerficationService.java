package com.wondersgroup.healthcloud.services.doctor;

import com.wondersgroup.common.http.utils.JsonConverter;
import com.wondersgroup.healthcloud.common.utils.IdGen;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorAccount;
import com.wondersgroup.healthcloud.jpa.entity.doctor.DoctorInvitation;
import com.wondersgroup.healthcloud.jpa.entity.user.RegisterInfo;
import com.wondersgroup.healthcloud.jpa.repository.doctor.DoctorInvitationRepository;
import com.wondersgroup.healthcloud.jpa.repository.user.RegisterInfoRepository;
import com.wondersgroup.healthcloud.services.doctor.exception.ErrorDoctorAccountNoneException;
import com.wondersgroup.healthcloud.services.doctor.exception.ErrorVerificationCodeException;
import com.wondersgroup.healthcloud.services.doctor.exception.VerificationUsedException;
import com.wondersgroup.healthcloud.services.user.UserService;
import com.wondersgroup.healthcloud.services.user.exception.ErrorIdcardException;
import com.wondersgroup.healthcloud.services.user.exception.IdcardExistException;
import com.wondersgroup.healthcloud.utils.DateFormatter;
import com.wondersgroup.healthcloud.utils.IdcardUtils;
import com.wondersgroup.healthcloud.utils.sms.SMS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ░░░░░▄█▌▀▄▓▓▄▄▄▄▀▀▀▄▓▓▓▓▓▌█
 * ░░░▄█▀▀▄▓█▓▓▓▓▓▓▓▓▓▓▓▓▀░▓▌█
 * ░░█▀▄▓▓▓███▓▓▓███▓▓▓▄░░▄▓▐█▌
 * ░█▌▓▓▓▀▀▓▓▓▓███▓▓▓▓▓▓▓▄▀▓▓▐█
 * ▐█▐██▐░▄▓▓▓▓▓▀▄░▀▓▓▓▓▓▓▓▓▓▌█▌
 * █▌███▓▓▓▓▓▓▓▓▐░░▄▓▓███▓▓▓▄▀▐█
 * █▐█▓▀░░▀▓▓▓▓▓▓▓▓▓██████▓▓▓▓▐█
 * ▌▓▄▌▀░▀░▐▀█▄▓▓██████████▓▓▓▌█▌
 * ▌▓▓▓▄▄▀▀▓▓▓▀▓▓▓▓▓▓▓▓█▓█▓█▓▓▌█▌
 * █▐▓▓▓▓▓▓▄▄▄▓▓▓▓▓▓█▓█▓█▓█▓▓▓▐█
 * <p>
 * Created by zhangzhixiu on 8/25/16.
 */
@Component("signingVerificationService")
public class SigningVerficationService {

    private static final long monthAgo = 30 * 24 * 60 * 60 * 1000L;

    @Autowired
    private DoctorInvitationRepository doctorInvitationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterInfoRepository registerInfoRepository;

    @Autowired
    private DoctorAccountService doctorAccountService;

    @Resource(name = "verification")
    private SMS sms;


    public Boolean doctorInvitationSend(String doctorId, String name, String idCard, String mobile, Boolean isDefault) {
        DoctorAccount doctorInfo = doctorAccountService.findOne(doctorId);
        idCard = StringUtils.upperCase(idCard);
        if (doctorInfo == null) {
            throw new ErrorDoctorAccountNoneException();
        }
        if (!IdcardUtils.validateCard(idCard)) {
            throw new ErrorIdcardException();
        }
        List<RegisterInfo> register = userService.findRegisterInfoByIdcard(idCard);
        if (!register.isEmpty()) {
            throw new IdcardExistException();
        }

        Random random = new Random();
        int value = random.nextInt(1000000) + 1000000;
        String code = String.valueOf(value).substring(1);

        List<DoctorInvitation> list = doctorInvitationRepository.findExist(doctorId, mobile, name, idCard, new Date(System.currentTimeMillis() - monthAgo));
        if (!list.isEmpty()) {
            doctorInvitationRepository.delete(list);
        }
        DoctorInvitation invitation = new DoctorInvitation();
        invitation.setId(IdGen.uuid());
        invitation.setCode(code);
        invitation.setDoctorId(doctorId);
        invitation.setMobile(mobile);
        invitation.setPersoncard(idCard);
        invitation.setName(name);
        invitation.setSendDate(new Date());
        invitation.setCreateBy(doctorId);
        invitation.setCreateDate(invitation.getSendDate());
        invitation.setUpdateBy(doctorId);
        invitation.setUpdateDate(invitation.getCreateDate());
        doctorInvitationRepository.saveAndFlush(invitation);
        String content = isDefault ? "邀请码%s。%s医生诚邀您安装“上海健康云”App，输入邀请码即成为实名用户，开启您的健康管理新模式。详询热线：4009216519，竭诚呵护您的健康。" : "邀请码%s。%s医生诚邀您安装“上海健康云”App，输入邀请码即成为实名用户，开启您的健康管理新模式。详询热线：4009216519，竭诚呵护您的健康。";
        sms.send(mobile, String.format(content, code, doctorInfo.getName()));

        return true;
    }

    public Boolean externalDoctorInvitationSend(Map<String, String> doctorInfo, String name, String idCard, String mobile, Boolean isDefault) {
        idCard = StringUtils.upperCase(idCard);
        if (!IdcardUtils.validateCard(idCard)) {
            throw new ErrorIdcardException();
        }
        List<RegisterInfo> register = userService.findRegisterInfoByIdcard(idCard);
        if (!register.isEmpty()) {
            throw new IdcardExistException();
        }

        Random random = new Random();
        int value = random.nextInt(1000000) + 1000000;
        String code = String.valueOf(value).substring(1);

        List<DoctorInvitation> list = doctorInvitationRepository.findExternalExist(mobile, name, idCard, new Date(System.currentTimeMillis() - monthAgo));
        if (!list.isEmpty()) {
            doctorInvitationRepository.delete(list);
        }
        DoctorInvitation invitation = new DoctorInvitation();
        invitation.setId(IdGen.uuid());
        invitation.setCode(code);
        invitation.setDoctorInfo(JsonConverter.toJson(doctorInfo));
        invitation.setMobile(mobile);
        invitation.setPersoncard(idCard);
        invitation.setName(name);
        invitation.setSendDate(new Date());
        invitation.setCreateDate(invitation.getSendDate());
        invitation.setUpdateDate(invitation.getCreateDate());
        doctorInvitationRepository.saveAndFlush(invitation);
        String content = "尊敬的先生/女士，您已完成上海市社区综改1+1+1签约服务。签约医生为%s,健康云认证码%s,请您尽快登录[上海健康云App]（下载地址：http://t.cn/RLp6ow8）完成身份认证,即可享受对应家庭医生提供的医疗服务。";
        sms.send(mobile, String.format(content, doctorInfo.get("doctor_name"), code));

        return true;
    }

    public Boolean doctorInvitation(String userId, String mobile, String name, String idCard, String code) {
        DoctorInvitation result = doctorInvitationRepository.find(mobile, name, StringUtils.upperCase(idCard), code, new Date(System.currentTimeMillis() - monthAgo));
        if (result == null) {
            throw new ErrorVerificationCodeException();
        } else if (result.getSuccessDate() != null) {
            throw new VerificationUsedException();
        } else {
            List<RegisterInfo> idCards = userService.findRegisterInfoByIdcard(idCard);
            if (!idCards.isEmpty()) {
                throw new IdcardExistException();
            }
            Date time = new Date();
            result.setUserId(userId);
            result.setSuccessDate(time);
            result.setUpdateBy(userId);
            result.setUpdateDate(time);
            doctorInvitationRepository.saveAndFlush(result);
            RegisterInfo register = userService.getOneNotNull(userId);
            register.setIdentifytype("2");
            register.setName(name);
            register.setPersoncard(idCard);
            register.setGender(IdcardUtils.getGenderByIdCard(idCard));
            register.setBirthday(DateFormatter.parseIdCardDate(IdcardUtils.getBirthByIdCard(idCard)));
            register.setUpdateBy(userId);
            register.setUpdateDate(time);
            registerInfoRepository.saveAndFlush(register);
        }
        return true;
    }
}
