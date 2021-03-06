package com.wondersgroup.healthcloud.registration.entity.request;


import org.springframework.core.env.Environment;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by longshasha on 16/5/22.
 */
@XmlRootElement(name = "MessageHeader")
public class RequestMessageHeader {

    private String frontproviderId;

    private String inputCharset;

    private String signType;

    private String sign;

    private String version = "3.0.26";

    public RequestMessageHeader() {

    }
    public RequestMessageHeader(Environment environment) {
        this.frontproviderId = environment.getProperty("web-service.frontproviderId");
        this.inputCharset = environment.getProperty("web-service.inputCharset");
        this.signType = environment.getProperty("web-service.signType");
    }

    public RequestMessageHeader(String frontproviderId, String sign) {
        this.frontproviderId = frontproviderId;
        this.sign = sign;
    }

    public String getFrontproviderId() {
        return frontproviderId;
    }

    public void setFrontproviderId(String frontproviderId) {
        this.frontproviderId = frontproviderId;
    }

    public String getInputCharset() {
        return inputCharset;
    }

    public void setInputCharset(String inputCharset) {
        this.inputCharset = inputCharset;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
