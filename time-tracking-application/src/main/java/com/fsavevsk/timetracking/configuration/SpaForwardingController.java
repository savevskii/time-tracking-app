package com.fsavevsk.timetracking.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController {

    @RequestMapping(value = {"/{path:^(?!api|actuator|assets|static|favicon\\.ico|v3|swagger-ui).*}",
            "/**/{path:^(?!api|actuator|assets|static).*}"})
    public String forward() {
        return "forward:/index.html";
    }

}

