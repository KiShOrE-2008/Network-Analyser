package com.networkmonitor.dto;

public class LocalNetworkDto {
    private String interfaceName;
    private String address;
    private short prefixLength;
    private String cidr;

    public LocalNetworkDto() {
    }

    public LocalNetworkDto(String interfaceName, String address, short prefixLength, String cidr) {
        this.interfaceName = interfaceName;
        this.address = address;
        this.prefixLength = prefixLength;
        this.cidr = cidr;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public short getPrefixLength() {
        return prefixLength;
    }

    public void setPrefixLength(short prefixLength) {
        this.prefixLength = prefixLength;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }
}
