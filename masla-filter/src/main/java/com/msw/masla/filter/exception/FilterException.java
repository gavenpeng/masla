package com.msw.masla.filter.exception;

/**
 * Created by Gavin.peng on 2017/10/17.
 */
public class FilterException extends Exception {

    public FilterException(String message){
        super(message);
    }

    public FilterException(Throwable cause){
        super(cause);
    }

    public FilterException(String message, Throwable cause){
        super(message, cause);
    }

}
