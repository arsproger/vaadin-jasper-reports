package com.example.jasperstarter.controller;

import com.example.jasperstarter.service.SimpleService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SimpleController {
    private final SimpleService simpleService;

    @GetMapping("/report/{format}")
    public ResponseEntity<String> saveReport(@PathVariable String format) throws JRException, IOException {
        return simpleService.saveReport(format);
    }

    @GetMapping("/report/download/{id}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable("id") Long reportId) {
        return simpleService.downloadReport(reportId);
    }

}
