package com.example.jasperstarter.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;

@Route("")
public class MainView extends AppLayout {
    public MainView() {
        createHeader();
        createTabs();
    }

    private void createHeader() {
        InputStreamFactory inputStreamFactory = () ->
                VaadinService.getCurrent().getClassLoader().getResourceAsStream("report.png");
        StreamResource resource = new StreamResource("logo.png", inputStreamFactory);
        Image logo = new Image(resource, "Report image");
        logo.setHeight("50px");

        addToNavbar(logo);
    }

    private void createTabs() {
        Tabs tabs = new Tabs();

        RouterLink employeesLink = new RouterLink("Employees", EmployeeView.class);
        RouterLink reportLink = new RouterLink("Reports", ReportView.class);

        Tab employeesTab = new Tab();
        employeesTab.add(employeesLink);

        Tab reportTab = new Tab();
        reportTab.add(reportLink);

        tabs.add(employeesTab, reportTab);

        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        addToNavbar(tabs);
    }

}
