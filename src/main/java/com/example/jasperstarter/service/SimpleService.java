package com.example.jasperstarter.service;

import com.example.jasperstarter.entity.Employee;
import com.example.jasperstarter.entity.Report;
import com.example.jasperstarter.repository.EmployeeRepository;
import com.example.jasperstarter.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SimpleService {
    private final ReportRepository reportRepository;
    private final EmployeeRepository employeeRepository;

    public ResponseEntity<byte[]> downloadReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(EntityNotFoundException::new);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        byte[] reportBytes = report.getReportData();
        headers.setContentDispositionFormData("attachment", report.getName() + "." + report.getFormat());
        headers.setContentLength(reportBytes.length);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    public ResponseEntity<String> saveReport(String reportFormat) throws JRException, IOException {
        List<Employee> employees = getAllEmployees()
                .stream().sorted(Comparator.comparing(Employee::getId)).toList();
        InputStream inputStream = getClass().getResourceAsStream("/employees.jrxml");

        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "arsproger");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+6"));
        Date currentDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
        dateFormat.setTimeZone(calendar.getTimeZone());
        String reportName = "report_" + dateFormat.format(currentDate);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        switch (reportFormat.toLowerCase()) {
            case "xlsx":
                JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                xlsxExporter.exportReport();
                break;
            case "pdf":
                JRPdfExporter pdfExporter = new JRPdfExporter();
                pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                pdfExporter.exportReport();
                break;
            case "html":
                HtmlExporter htmlExporter = new HtmlExporter();
                htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                htmlExporter.setExporterOutput(new SimpleHtmlExporterOutput(byteArrayOutputStream));
                htmlExporter.exportReport();
                break;
            case "txt":
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Employee Report\n\n");
                String format = "%-5s | %-15s | %-15s | %-10s | %-15s%n";
                stringBuilder.append(String.format(format, "ID", "First name", "Last name", "Salary", "Position"));
                stringBuilder.append("-".repeat(70)).append("\n");
                for (Employee employee : employees) {
                    stringBuilder.append(String.format(format,
                            employee.getId(), employee.getFirstName(), employee.getLastName(),
                            employee.getSalary(), employee.getPosition()));
                }
                String textReport = stringBuilder.toString();
                byteArrayOutputStream.write(textReport.getBytes());
                break;
            case "json":
                ObjectMapper jsonObjectMapper = new ObjectMapper();
                jsonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String jsonReport = jsonObjectMapper.writeValueAsString(employees);
                byteArrayOutputStream.write(jsonReport.getBytes());
                break;
            default:
                String message = "this format " + reportFormat + " is not supported!";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        byte[] reportBytes = byteArrayOutputStream.toByteArray();

        Report report = Report.builder()
                .format(reportFormat)
                .name(reportName)
                .size((long) reportBytes.length + " bytes")
                .reportData(reportBytes)
                .build();
        reportRepository.save(report);

        String message = "report " + reportName + " created!";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Employee> findBySearchTerm(String searchTerm) {
        return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Long saveEmployee(Employee employee) {
        return employeeRepository.save(employee).getId();
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    public Long updateEmployeeById(Long id, Employee updatedEmployee) {
        Employee employee = getEmployeeById(id);
        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setPosition(updatedEmployee.getPosition());
        employee.setSalary(updatedEmployee.getSalary());
        return employeeRepository.save(employee).getId();
    }

}
