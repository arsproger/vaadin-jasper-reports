package com.example.jasperstarter.view;

import com.example.jasperstarter.entity.Employee;
import com.example.jasperstarter.service.SimpleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@Route(value = "employeeUi", layout = MainView.class)
public class EmployeeView extends VerticalLayout {
    private final SimpleService simpleService;
    private final Grid<Employee> grid = new Grid<>(Employee.class);

    @Autowired
    public EmployeeView(SimpleService simpleService) {
        this.simpleService = simpleService;

        Button addButton = new Button("Add Employee", event -> {
            Employee newEmployee = new Employee();
            Dialog createDialog = createCreateDialog(newEmployee, grid);
            createDialog.open();
        });
        add(addButton);

        grid.setItems(simpleService.getAllEmployees());
        grid.setHeightFull();
        setHeightFull();

        TextField searchField = new TextField("Search");
        searchField.addValueChangeListener(event -> grid.setItems(simpleService.findBySearchTerm(event.getValue())));
        add(searchField);

        grid.addComponentColumn(employee -> {
            Button updateButton = new Button("Update", event -> {
                Dialog updateDialog = createUpdateDialog(employee, grid);
                updateDialog.open();
            });
            updateButton.getElement().getStyle().set("background-color", "blue");
            updateButton.getStyle().set("color", "black");
            Button deleteButton = new Button("Delete", event -> {
                simpleService.deleteEmployee(employee.getId());
                grid.setItems(simpleService.getAllEmployees());
            });
            deleteButton.getElement().getStyle().set("background-color", "red");
            deleteButton.getStyle().set("color", "black");
            return new HorizontalLayout(updateButton, deleteButton);
        }).setHeader("Actions");

        add(grid);
    }

    private void configureFormFields(Binder<Employee> binder, TextField firstNameField, TextField lastNameField, TextField positionField, NumberField salaryField) {
        binder.forField(firstNameField)
                .asRequired("First Name is required")
                .bind(Employee::getFirstName, Employee::setFirstName);
        binder.forField(lastNameField)
                .asRequired("Last Name is required")
                .bind(Employee::getLastName, Employee::setLastName);
        binder.forField(positionField)
                .asRequired("Position is required")
                .bind(Employee::getPosition, Employee::setPosition);
        binder.forField(salaryField)
                .asRequired("Salary is required")
                .bind(Employee::getSalary, Employee::setSalary);
    }

    private Dialog createDialog(Employee employee, String buttonCaption, Consumer<Employee> actionOnValid, Grid<Employee> grid) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        TextField positionField = new TextField("Position");
        NumberField salaryField = new NumberField("Salary");

        Binder<Employee> binder = new Binder<>(Employee.class);
        binder.setBean(employee);

        configureFormFields(binder, firstNameField, lastNameField, positionField, salaryField);

        Button actionButton = new Button(buttonCaption, event -> {
            if (binder.validate().isOk()) {
                binder.writeBeanIfValid(employee);
                actionOnValid.accept(employee);
                grid.setItems(simpleService.getAllEmployees());
                dialog.close();
            }
        });

        formLayout.add(firstNameField, lastNameField, positionField, salaryField, actionButton);
        dialog.add(formLayout);
        return dialog;
    }

    private Dialog createUpdateDialog(Employee employee, Grid<Employee> grid) {
        return createDialog(employee, "Update", (updatedEmployee)
                -> simpleService.updateEmployeeById(updatedEmployee.getId(), updatedEmployee), grid);
    }

    private Dialog createCreateDialog(Employee employee, Grid<Employee> grid) {
        return createDialog(employee, "Create", simpleService::saveEmployee, grid);
    }

}
