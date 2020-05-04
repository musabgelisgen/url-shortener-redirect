package com.cs443.bilshortredirect.link.models;

public enum PlatformType {
    WINDOWS("windows"),
    LINUX("linux"),
    MAC("mac"),
    ANDROID("android"),
    IOS("iphone"),
    OTHER_OS("other_os");

    private final String value;

    PlatformType(String value){
        this.value = value;
    }

    public String value(){
        return value;
    }

    @Override
    public String toString() {
        return value();
    }
}