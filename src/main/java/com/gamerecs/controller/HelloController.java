package com.gamerecs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple controller to verify the API is working.
 */
@RestController
@RequestMapping("/api")
public class HelloController {
    
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    /**
     * Basic endpoint that returns a greeting message.
     * @return String A simple greeting message
     */
    @GetMapping("/hello")
    public String sayHello() {
        logger.info("Hello endpoint called");
        logger.debug("Preparing to return greeting message");
        return "Hello World from GameRecs API!";
    }
} 