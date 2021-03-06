package com.wondersgroup.healthcloud.services.appointment;

import com.wondersgroup.healthcloud.jpa.entity.appointment.*;
import com.wondersgroup.healthcloud.registration.entity.response.SegmentNumberInfo;
import com.wondersgroup.healthcloud.services.appointment.dto.OrderDto;
import com.wondersgroup.healthcloud.services.appointment.dto.ScheduleDto;

import java.util.List;
import java.util.Map;

/**
 * Created by longshasha on 16/12/5.
 *
 * 提供给客户端接口使用的service
 */
public interface AppointmentApiService {

    List<Map<String,Object>> findAppointmentAreaByUpperCode(String areaCode);

    List<AppointmentHospital> findAllHospitalListByAreaCodeOrKw(String kw,String areaCode,Integer flag,int pageSize);

    int countDoctorNumByHospitalId(String id);

    List<AppointmentHospital> findAllHospitalListByKw(String kw,Integer flag,int pageSize);

    List<AppointmentDoctor> findDoctorListByKw(String kw, int pageSize, int pageNum,Boolean hasDepartRegistration,String departmentL2Id);

    Map<String,Object> countDoctorReserveOrderNumByDoctorId(String doctorId);

    AppointmentHospital findHospitalById(String hospitalId);

    List<AppointmentL1Department> findAllAppointmentL1Department(String hospital_id);

    List<AppointmentL2Department> findAppointmentL2Department(String hospital_id, String department_l1_id);

    Map<String,Object> countDepartmentReserveOrderNumByDepartmentId(String department_l2_id);

    AppointmentL2Department findAppointmentL2DepartmentById(String department_l2_id);

    AppointmentHospital findHospitalByDepartmentL2Id(String department_l2_id);

    List<ScheduleDto> findScheduleByDepartmentL2IdAndScheduleDate(String department_l2_id, String schedule_date, Integer pageNum, int pageSize);

    AppointmentDoctor findDoctorById(String id);

    AppointmentL2Department findL2DepartmentById(String id);

    List<ScheduleDto> findScheduleByDepartmentL2IdOrDoctorId(String id,String type, Integer flag, int pageSize);

    List<OrderDto> findOrderByUidOrId(String id, Integer pageNum, Integer pageSize,Boolean isList);

    OrderDto submitUserReservation(String contactId, String scheduleId, String orderType);

    OrderDto cancelReservationOrderById(String id);

    Boolean getRegistrationIsOn(String mainArea);

    AppointmentDoctorSchedule findScheduleById(String scheduleId);

    void saveOrUpdateAppointmentScheduleByScheduleId(String scheduleId);

    void saveOrUpdateAppointmentScheduleByDoctorId(String id,String type);

    int countAllDoctorReservationNumByDepartmentL2Id(String department_l2_id);

    void closeNumberSourceByOrderId(String orderId);

    List<OrderDto> findOrderListByScheduleId(String scheduleId);

    void updateOrderWhencloseNumberSource(String closeSms,String id);

    void saveOrUpdateAppointmentScheduleByDepartmentId(String id);
}
