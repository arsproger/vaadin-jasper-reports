package com.example.jasperstarter.service;

import com.example.jasperstarter.entity.Employee;
import com.example.jasperstarter.entity.Report;
import com.example.jasperstarter.enums.ReportFormat;
import com.example.jasperstarter.repository.EmployeeRepository;
import com.example.jasperstarter.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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

        log.info("The report with ID {} has been downloaded", reportId);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    public ResponseEntity<String> saveReport(String reportFormat) {
        List<Employee> employees = getAllEmployees()
                .stream().sorted(Comparator.comparing(Employee::getId)).toList();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+6"));
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm");
        dateFormat.setTimeZone(calendar.getTimeZone());

        String reportName = "report_" + dateFormat.format(currentDate);

        JasperPrint jasperPrint = getJasperPrint(employees);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ReportFormat reportFormatEnum = ReportFormat.valueOf(reportFormat.toUpperCase());

        switch (reportFormatEnum) {
            case XLSX -> generateXlsx(jasperPrint, byteArrayOutputStream);
            case PDF -> generatePdf(jasperPrint, byteArrayOutputStream);
            case HTML -> generateHtml(jasperPrint, byteArrayOutputStream);
            case TXT -> generateTxt(jasperPrint, byteArrayOutputStream);
            case JSON -> generateJson(employees, byteArrayOutputStream);
            default -> {
                String message = "this format " + reportFormat.toLowerCase() + " is not supported!";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
        }

        byte[] reportBytes = byteArrayOutputStream.toByteArray();

        Report report = Report.builder()
                .format(reportFormat.toLowerCase())
                .name(reportName)
                .size((long) reportBytes.length + " bytes")
                .reportData(reportBytes)
                .build();
        reportRepository.save(report);

        String message = "report " + reportName + " created!";
        log.info(message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @SneakyThrows
    public JasperPrint getJasperPrint(List<Employee> employees) {
        InputStream inputStream = getClass().getResourceAsStream("/employees.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "arsproger");

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    @SneakyThrows
    public void generateXlsx(JasperPrint jasperPrint, ByteArrayOutputStream byteArrayOutputStream) {
        JRXlsxExporter xlsxExporter = new JRXlsxExporter();
        xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
        xlsxExporter.exportReport();
    }

    @SneakyThrows
    public void generatePdf(JasperPrint jasperPrint, ByteArrayOutputStream byteArrayOutputStream) {
        JRPdfExporter pdfExporter = new JRPdfExporter();
        pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
        pdfExporter.exportReport();
    }

    @SneakyThrows
    public void generateHtml(JasperPrint jasperPrint, ByteArrayOutputStream byteArrayOutputStream) {
        HtmlExporter htmlExporter = new HtmlExporter();
        htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        htmlExporter.setExporterOutput(new SimpleHtmlExporterOutput(byteArrayOutputStream));
        htmlExporter.exportReport();
    }

    @SneakyThrows
    public void generateTxt(JasperPrint jasperPrint, ByteArrayOutputStream byteArrayOutputStream) {
        JRTextExporter exporter = new JRTextExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(byteArrayOutputStream));
        SimpleTextReportConfiguration configuration = new SimpleTextReportConfiguration();
        configuration.setCharWidth((float) 8);
        configuration.setCharHeight((float) 12);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }

    @SneakyThrows
    public void generateJson(List<Employee> employees, ByteArrayOutputStream byteArrayOutputStream) {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonReport = jsonObjectMapper.writeValueAsString(employees);
        byteArrayOutputStream.write(jsonReport.getBytes());
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Employee> findBySearchTerm(String searchTerm) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm);
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
