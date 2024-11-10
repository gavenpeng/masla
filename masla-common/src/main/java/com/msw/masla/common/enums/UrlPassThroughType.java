package com.msw.masla.common.enums;

public enum UrlPassThroughType {

    /**透传contextRoot和path*/
    CONTEXT_ROOT_AND_PATH_PASS_THROUGH(0),
    /**不透传contextRoot和path*/
    NO_PASS_THROUGH(1),
    /**只透传contextRoot*/
    CONTEXT_ROOT_PASS_THROUGH(2),
    /**只透传path*/
    PATH_PASS_THROUGH(3);

    int code;

    UrlPassThroughType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
