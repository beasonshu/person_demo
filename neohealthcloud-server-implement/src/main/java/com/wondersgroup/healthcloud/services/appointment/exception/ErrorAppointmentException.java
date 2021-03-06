package com.wondersgroup.healthcloud.services.appointment.exception;

import com.wondersgroup.healthcloud.exceptions.BaseException;

/**
 * Created by longshasha on 16/8/1.
 */
public class ErrorAppointmentException extends BaseException {
    public ErrorAppointmentException(){
        super(3001, "欲享受预约挂号服务,请先绑定手机号", null);
    }

    public ErrorAppointmentException(String msg){
        super(3002, msg, null);
    }
}
