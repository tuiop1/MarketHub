package com.tuiop.markethub.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1/test")
@RestController
public class TestController {

    @GetMapping
    public ResponseEntity<String> doSth(){
        return ResponseEntity.ok("hi from test");
    }


}
