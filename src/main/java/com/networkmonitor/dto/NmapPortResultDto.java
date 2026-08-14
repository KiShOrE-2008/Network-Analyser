package com.networkmonitor.dto;

public class NmapPortResultDto {

    private int port;
    private String protocol;
    private String state;
    private String serviceName;
    private String serviceProduct;
    private String serviceVersion;

    public NmapPortResultDto() {
    }

    public NmapPortResultDto(int port, String protocol, String state, String serviceName, String serviceProduct, String serviceVersion) {
        this.port = port;
        this.protocol = protocol;
        this.state = state;
        this.serviceName = serviceName;
        this.serviceProduct = serviceProduct;
        this.serviceVersion = serviceVersion;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceProduct() {
        return serviceProduct;
    }

    public void setServiceProduct(String serviceProduct) {
        this.serviceProduct = serviceProduct;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
