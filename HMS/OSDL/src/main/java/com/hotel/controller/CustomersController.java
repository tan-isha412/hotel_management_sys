package com.hotel.controller;

import com.hotel.model.Customer;
import com.hotel.service.DataStore;
import com.hotel.util.NavUtil;
import com.hotel.util.AlertUtil;
import com.hotel.util.Validator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * CustomersController - add and view customers.
 */
public class CustomersController implements Initializable {

    // Table
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, String> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colNationality;
    @FXML private TableColumn<Customer, String> colDOB;
    @FXML private TableColumn<Customer, String> colRegistered;
    @FXML private Label lblCount;

    // Form fields
    @FXML private TextField tfName;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEmail;
    @FXML private TextField tfAddress;
    @FXML private TextField tfIdNumber;
    @FXML private ComboBox<String> cbIdProof;
    @FXML private ComboBox<String> cbNationality;
    @FXML private DatePicker dpDOB;

    private DataStore dataStore;
    private ObservableList<Customer> customerData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore = DataStore.getInstance();
        setupTable();
        setupForm();
        loadCustomers();
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colNationality.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNationality()));
        colDOB.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDateOfBirth() != null ? c.getValue().getDateOfBirth().toString() : "-"));
        colRegistered.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRegistrationDate().toString()));

        customerData = FXCollections.observableArrayList();
        customersTable.setItems(customerData);
    }

    private void setupForm() {
        cbIdProof.setItems(FXCollections.observableArrayList(
            "Aadhaar Card", "Passport", "Driving License", "PAN Card", "Voter ID"));
        cbIdProof.setValue("Aadhaar Card");
        cbNationality.setItems(FXCollections.observableArrayList(
            "Indian", "American", "British", "Canadian", "Australian",
            "German", "French", "Japanese", "Chinese", "Other"));
        cbNationality.setValue("Indian");
        dpDOB.setValue(LocalDate.of(1990, 1, 1));
    }

    private void loadCustomers() {
        customerData.setAll(dataStore.getAllCustomers());
        lblCount.setText(customerData.size() + " customers registered");
    }

    @FXML
    private void addCustomer() {
        String name = tfName.getText().trim();
        String phone = tfPhone.getText().trim();
        String email = tfEmail.getText().trim();
        String address = tfAddress.getText().trim();
        String idProof = cbIdProof.getValue();
        String idNumber = tfIdNumber.getText().trim();
        String nationality = cbNationality.getValue();
        LocalDate dob = dpDOB.getValue();

        if (!Validator.isValidName(name)) {
            AlertUtil.showError("Validation Error", "Please enter a valid name (min 2 characters).");
            return;
        }
        if (!Validator.isValidPhone(phone)) {
            AlertUtil.showError("Validation Error", "Please enter a valid 10-digit phone number.");
            return;
        }
        if (!Validator.isValidEmail(email)) {
            AlertUtil.showError("Validation Error", "Please enter a valid email address.");
            return;
        }
        if (!Validator.isNotBlank(idNumber)) {
            AlertUtil.showError("Validation Error", "Please enter ID proof number.");
            return;
        }
        // Check duplicate phone
        if (dataStore.getCustomerByPhone(phone).isPresent()) {
            AlertUtil.showError("Duplicate", "A customer with this phone number already exists.");
            return;
        }

        String customerId = dataStore.generateCustomerId();
        Customer customer = new Customer(customerId, name, email, phone,
            address, idProof, idNumber, dob, nationality);
        dataStore.addCustomer(customer);

        // Birthday check
        if (customer.isBirthday()) {
            AlertUtil.showOffer("🎂 Happy Birthday, " + name + "!\n\n" +
                "You're eligible for a FREE room upgrade or 10% discount on your booking!\n" +
                "Use promo code: BIRTHDAY when booking.");
        } else {
            AlertUtil.showInfo("Success", "Customer " + name + " registered with ID: " + customerId);
        }
        clearForm();
        loadCustomers();
    }

    private void clearForm() {
        tfName.clear(); tfPhone.clear(); tfEmail.clear();
        tfAddress.clear(); tfIdNumber.clear();
        cbIdProof.setValue("Aadhaar Card");
        cbNationality.setValue("Indian");
        dpDOB.setValue(LocalDate.of(1990, 1, 1));
    }

    @FXML private void clearFormAction() { clearForm(); }
    @FXML private void goBack() { NavUtil.navigateTo(customersTable, "Rooms.fxml"); }
}
