package com.atwendu.gware.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

/**
 * @param
 * @return
 */

@ControllerAdvice(basePackages = {"com.atwendu.gware"})
public class JsonpController extends AbstractJsonpResponseBodyAdvice {
    public JsonpController(){
        super("callback","jsonp");
    }

}
