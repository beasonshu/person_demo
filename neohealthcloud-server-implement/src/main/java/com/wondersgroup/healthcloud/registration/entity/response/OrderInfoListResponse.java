package com.wondersgroup.healthcloud.registration.entity.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by longshasha on 16/5/23.
 */

@XmlRootElement(name = "Response")
public class OrderInfoListResponse extends BaseResponse{

    @XmlElementWrapper(name = "List")
    @XmlElement(name = "Result")
    public List<OrderInfo> orderInfoList;
}
