package com.suplin.demo.req;

public class UserQueryReq extends PageReq {

    private String longinName;

    public String getLonginName() {
        return longinName;
    }

    public void setLonginName(String longinName) {
        this.longinName = longinName;
    }

    @Override
    public String toString() {
        return "UserQueryReq{" +
                "longinName='" + longinName + '\'' +
                "} " + super.toString();
    }
}