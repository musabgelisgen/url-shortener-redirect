package com.cs443.bilshortredirect.link.models;

public enum BrowserType{
    CHROME("chrome"),
    FIREFOX("firefox"),
    SAFARI("safari"),
    INTERNET_EXPLORER("internet explorer"),
    OTHER_BROWSER("other_browser");

    private final String value;

    BrowserType(String value){
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
