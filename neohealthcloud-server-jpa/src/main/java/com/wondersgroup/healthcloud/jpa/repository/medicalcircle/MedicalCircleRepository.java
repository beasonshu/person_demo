package com.wondersgroup.healthcloud.jpa.repository.medicalcircle;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.wondersgroup.healthcloud.jpa.entity.medicalcircle.MedicalCircle;
import org.springframework.transaction.annotation.Transactional;

public interface MedicalCircleRepository extends JpaRepository<MedicalCircle,String>{

    @Query(value = "select m from MedicalCircle m where m.type in ?1 and  m.delFlag='0' and m.sendtime<?2  ")
    Page<MedicalCircle> findAllMedicalCircle(Integer[] type,Date flag,Pageable pageable);


    @Query(value = "select m from MedicalCircle m where m.doctorid=?1 and m.type in ?2 and m.delFlag=0 and m.sendtime<?3 ")
    Page<MedicalCircle> findUserMedicalCircle(String doctorId,Integer[] type ,Date flag,Pageable pageable);

    @Query(value = "select m from MedicalCircle m where m.doctorid=?1 and m.type in ?2 and m.delFlag='0' ")
    Page<MedicalCircle> findUserMedicalCircleNewest(String doctorId,Integer[] type, Pageable pageable);

    @Query(value = "select m from MedicalCircle m where m.doctorid=?1 and m.id=?2 and m.delFlag='0' ")
    MedicalCircle findMedicalCircleWithUser(String doctorId,String circleId);

    @Query(value = "select count(1) from MedicalCircle m where m.type in ?1 and m.doctorid=?2 and m.delFlag=0 ")
    Long getCircleNum(List<Integer> types,String doctor);


    @Query(value = "select m from MedicalCircle m,MedicalCircleCollect c where m.id=c.circleid and c.doctorid=?1 and m.type in ?2 and c.collecttime<?3 and m.delFlag='0' and c.delFlag='0' order by c.collecttime desc ")
    Page<MedicalCircle> findUserCollectMedicalCircle(String doctorId,Integer[] type, Date flag, Pageable pageable);

    MedicalCircle findById(String id);

    @Transactional
    @Modifying
    @Query(value = "update MedicalCircle set isVisible = ?2 where id in ?1")
    void updateIsVisible(List<String> ids, String isVisible);


    @Query(value = "select m from MedicalCircle m where m.delFlag='0' ")
    List<MedicalCircle> findMedicalCircleListByPager(Pageable pageable);

    @Query(value = "select count(m) from MedicalCircle m where m.delFlag='0' ")
    int countTag();

    @Transactional
    @Modifying
    @Query(value = "update MedicalCircle set tagid = ?2 where id in ?1")
    void batchUpdateTagByIds(List<String> ids, String tagId);
}
