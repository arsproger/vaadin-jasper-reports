package com.example.jasperstarter.view;

import com.example.jasperstarter.entity.Report;
import com.example.jasperstarter.service.SimpleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Route(value = "reportUi", layout = MainView.class)
public class ReportView extends VerticalLayout {
    private final SimpleService simpleService;
    private final Grid<Report> reportGrid = new Grid<>(Report.class);
    private final ComboBox<String> formatComboBox = new ComboBox<>("Select report format");

    @Autowired
    public ReportView(SimpleService simpleService) {
        this.simpleService = simpleService;

        formatComboBox.setItems("HTML", "PDF", "XLSX", "TXT", "JSON");

        Button generateButton = new Button("Generate Report");
        generateButton.addClickListener(e -> generateReport());

        reportGrid.setColumns("name", "size", "format");
        reportGrid.addComponentColumn(this::reportDownload).setHeader("Download");

        add(formatComboBox, generateButton, reportGrid);

        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.START);

        updateReportGrid();
    }

    private void generateReport() {
        try {
            String selectedFormat = formatComboBox.getValue();
            if (selectedFormat != null) {
                String result = simpleService.saveReport(selectedFormat.toLowerCase()).getBody();
                Notification.show(result);
                updateReportGrid();
            } else {
                Notification.show("Please select a report format.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Notification.show("Error generating report.");
        }
    }

    private void updateReportGrid() {
        List<Report> reports = simpleService.getAllReports();
        reportGrid.setItems(reports);
    }

    private Button reportDownload(Report report) {
        RestTemplate restTemplate = new RestTemplate();
        return new Button("Download", e -> {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUri().toString();
            String downloadUrl = baseUrl + "/report/download/" + report.getId();
            getUI().ifPresent(ui -> {
                ResponseEntity<?> response = restTemplate.getForEntity(downloadUrl, String.class);
                String statusCode = response.getStatusCode().toString();
                Notification.show("Report download result: " + statusCode);
                ui.getPage().executeJs("window.open('" + downloadUrl + "','_self');");
            });
        });
    }

}