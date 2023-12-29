package com.example.jasperstarter.controller;

import com.example.jasperstarter.service.SimpleService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
@RequiredArgsConstructor
public class SimpleController {
    private final SimpleService simpleService;

    @GetMapping("/report/{format}")
    public String exportReport(@PathVariable String format) throws JRException, FileNotFoundException {
        return simpleService.exportReport(format);
    }

}
