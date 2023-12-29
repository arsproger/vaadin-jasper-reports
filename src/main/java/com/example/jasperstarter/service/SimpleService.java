package com.example.jasperstarter.service;

import com.example.jasperstarter.entity.Employee;
import com.example.jasperstarter.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimpleService {
    private final EmployeeRepository employeeRepository;

    public String exportReport(String reportFormat) throws FileNotFoundException, JRException {
        String path = "C:\\Users\\user\\Downloads\\report";
        List<Employee> employees = employeeRepository.findAll();
        File file = ResourceUtils.getFile("classpath:employees.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "arsproger");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        switch (reportFormat.toLowerCase()) {
            case "html":
                JasperExportManager.exportReportToHtmlFile(jasperPrint, path + ".html");
                break;
            case "pdf":
                JasperExportManager.exportReportToPdfFile(jasperPrint, path + ".pdf");
                break;
            case "xlsx":
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path + ".xlsx"));
                exporter.exportReport();
                break;
            default:
                return "this format " + reportFormat + " is not supported";
        }
        return "report is created in " + reportFormat + " format along the path " + path + "." + reportFormat;
    }

}
