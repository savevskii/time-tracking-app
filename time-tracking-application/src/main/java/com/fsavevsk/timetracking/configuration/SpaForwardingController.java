package com.fsavevsk.timetracking.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Controller
public class SpaForwardingController {

    @GetMapping(value = "/{*path}", produces = MediaType.TEXT_HTML_VALUE)
    public String forward(@PathVariable String path) {
        if (path.contains(".")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var first = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
        if (Set.of("api","actuator","assets","static","v3","swagger-ui").contains(first)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "forward:/index.html";
    }

}

