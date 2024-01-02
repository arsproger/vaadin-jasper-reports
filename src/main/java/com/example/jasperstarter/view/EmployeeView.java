package com.example.jasperstarter.view;

import com.example.jasperstarter.entity.Employee;
import com.example.jasperstarter.service.SimpleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "employeeUi", layout = MainView.class)
public class EmployeeView extends VerticalLayout {
    private final SimpleService simpleService;

    @Autowired
    public EmployeeView(SimpleService simpleService) {
        this.simpleService = simpleService;

        Button addButton = new Button("Add Employee", event -> {
            Employee newEmployee = new Employee();
            Dialog createDialog = createCreateDialog(newEmployee);
            createDialog.open();
        });
        add(addButton);

        Grid<Employee> grid = new Grid<>(Employee.class);
        grid.setItems(EmployeeView.this.simpleService.getAllEmployees());
        grid.setHeightFull();
        setHeightFull();

        TextField searchField = new TextField("Search");
        searchField.addValueChangeListener(event -> grid.setItems(EmployeeView.this.simpleService.findBySearchTerm(event.getValue())));
        add(searchField);

        grid.addComponentColumn(employee -> {
            Button updateButton = new Button("Update", event -> {
                Dialog updateDialog = createUpdateDialog(employee, grid);
                updateDialog.open();
            });
            updateButton.getElement().getStyle().set("background-color", "blue");
            updateButton.getStyle().set("color", "black");
            Button deleteButton = new Button("Delete", event -> {
                EmployeeView.this.simpleService.deleteEmployee(employee.getId());
                grid.setItems(EmployeeView.this.simpleService.getAllEmployees());
            });
            deleteButton.getElement().getStyle().set("background-color", "red");
            deleteButton.getStyle().set("color", "black");
            return new HorizontalLayout(updateButton, deleteButton);
        }).setHeader("Actions");

        add(grid);
    }

    private Dialog createUpdateDialog(Employee employee, Grid<Employee> grid) {
        Dialog updateDialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name", employee.getFirstName());
        TextField lastNameField = new TextField("Last Name", employee.getLastName());
        TextField positionField = new TextField("Position", employee.getPosition());
        TextField salaryField = new TextField("Salary", String.valueOf(employee.getSalary()));

        Button updateButton = new Button("Update", event -> {
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setPosition(positionField.getValue());
            employee.setSalary(Double.parseDouble(salaryField.getValue()));
            simpleService.updateEmployeeById(employee.getId(), employee);
            grid.setItems(simpleService.getAllEmployees());
            updateDialog.close();
        });
        updateButton.getElement().getStyle().set("background-color", "blue");
        updateButton.getStyle().set("color", "black");

        formLayout.add(firstNameField, lastNameField, positionField, salaryField, updateButton);
        updateDialog.add(formLayout);
        return updateDialog;
    }

    private Dialog createCreateDialog(Employee employee) {
        Dialog createDialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        TextField positionField = new TextField("Position");
        TextField salaryField = new TextField("Salary");

        Button createButton = new Button("Create", event -> {
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setPosition(positionField.getValue());
            employee.setSalary(Double.parseDouble(salaryField.getValue()));
            simpleService.saveEmployee(employee);
            createDialog.close();
        });
        createButton.getElement().getStyle().set("background-color", "blue");
        createButton.getStyle().set("color", "black");

        formLayout.add(firstNameField, lastNameField, positionField, salaryField, createButton);
        createDialog.add(formLayout);
        return createDialog;
    }

}
