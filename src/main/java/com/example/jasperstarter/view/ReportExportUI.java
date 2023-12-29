package com.example.jasperstarter.view;

import com.example.jasperstarter.service.SimpleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;

@Route("reportUi")
public class ReportExportUI extends VerticalLayout {

    @Autowired
    public ReportExportUI(SimpleService reportExportService) {

        ComboBox<String> formatComboBox = new ComboBox<>("Select Format");
        formatComboBox.setItems("HTML", "PDF", "XLSX");

        Button exportButton = new Button("Export");
        exportButton.addClickListener(e -> {
            String selectedFormat = formatComboBox.getValue();
            if (selectedFormat != null && !selectedFormat.isEmpty()) {
                String result;
                try {
                    result = reportExportService.exportReport(selectedFormat.toLowerCase());
                } catch (FileNotFoundException | JRException ex) {
                    throw new RuntimeException(ex);
                }
                Notification.show(result);
            } else {
                Notification.show("Please select a format");
            }
        });

        add(formatComboBox, exportButton);
    }
}
