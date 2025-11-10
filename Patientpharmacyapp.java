import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Patient Billing & Pharmacy Management - Single-file JavaFX example
 * Save as PatientPharmacyApp.java
 *
 * Requirements: JavaFX SDK on module path when running (or configure in IDE)
 */
public class PatientPharmacyApp extends Application {

    private ObservableList<BillItem> billItems = FXCollections.observableArrayList();
    private ObservableList<Medicine> inventory = FXCollections.observableArrayList();

    private Label subtotalLabel = new Label("0.00");
    private Label taxLabel = new Label("0.00");
    private Label totalLabel = new Label("0.00");

    private final double TAX_RATE = 0.05; // 5% tax as example

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Patient Billing & Pharmacy Management");

        TabPane tabs = new TabPane();
        Tab billingTab = new Tab("Billing", createBillingPane(primaryStage));
        Tab pharmacyTab = new Tab("Pharmacy", createPharmacyPane());
        billingTab.setClosable(false);
        pharmacyTab.setClosable(false);

        tabs.getTabs().addAll(billingTab, pharmacyTab);

        Scene scene = new Scene(tabs, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // sample inventory
        inventory.addAll(
                new Medicine("Paracetamol 500mg", 1.50, 200),
                new Medicine("Amoxicillin 250mg", 0.80, 150),
                new Medicine("Cetrizine 10mg", 0.40, 300)
        );
    }

    /* -------------------- Billing Pane -------------------- */
    private BorderPane createBillingPane(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // Top: patient details
        GridPane patientGrid = new GridPane();
        patientGrid.setHgap(10);
        patientGrid.setVgap(8);

        Label lblPatientName = new Label("Patient Name:");
        TextField tfPatientName = new TextField();

        Label lblPatientId = new Label("Patient ID:");
        TextField tfPatientId = new TextField();

        Label lblDoctor = new Label("Doctor:");
        TextField tfDoctor = new TextField();

        patientGrid.add(lblPatientName, 0, 0);
        patientGrid.add(tfPatientName, 1, 0);
        patientGrid.add(lblPatientId, 2, 0);
        patientGrid.add(tfPatientId, 3, 0);
        patientGrid.add(lblDoctor, 0, 1);
        patientGrid.add(tfDoctor, 1, 1);

        root.setTop(patientGrid);

        // Center: Bill items table and add form
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(8));

        TableView<BillItem> billTable = new TableView<>(billItems);
        TableColumn<BillItem, String> colName = new TableColumn<>("Item");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(320);

        TableColumn<BillItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(80);

        TableColumn<BillItem, Double> colPrice = new TableColumn<>("Unit Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colPrice.setPrefWidth(100);

        TableColumn<BillItem, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(120);

        billTable.getColumns().addAll(colName, colQty, colPrice, colTotal);
        billTable.setPrefHeight(300);

        // Add item form (auto-suggest from inventory)
        HBox addBox = new HBox(8);
        TextField tfItemName = new TextField();
        tfItemName.setPromptText("Item name / medicine");
        TextField tfQty = new TextField();
        tfQty.setPromptText("Qty");
        TextField tfPrice = new TextField();
        tfPrice.setPromptText("Unit price");

        Button btnAdd = new Button("Add to Bill");
        Button btnRemove = new Button("Remove Selected Item");

        addBox.getChildren().addAll(new Label("Item:"), tfItemName, new Label("Qty:"), tfQty, new Label("Price:"), tfPrice, btnAdd, btnRemove);

        centerVBox.getChildren().addAll(billTable, addBox);

        root.setCenter(centerVBox);

        // Right: totals and actions
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(8));

        GridPane totalsGrid = new GridPane();
        totalsGrid.setVgap(8);
        totalsGrid.setHgap(8);

        totalsGrid.add(new Label("Subtotal:"), 0, 0);
        totalsGrid.add(subtotalLabel, 1, 0);
        totalsGrid.add(new Label("Tax (" + (int)(TAX_RATE*100) + "%):"), 0, 1);
        totalsGrid.add(taxLabel, 1, 1);
        totalsGrid.add(new Label("Total:"), 0, 2);
        totalsGrid.add(totalLabel, 1, 2);

        Button btnGenerate = new Button("Generate Invoice");
        Button btnSaveInvoice = new Button("Save Invoice (CSV)");
        Button btnClear = new Button("Clear Bill");

        rightBox.getChildren().addAll(totalsGrid, btnGenerate, btnSaveInvoice, btnClear);
        root.setRight(rightBox);

        // event handlers
        btnAdd.setOnAction(e -> {
            String name = tfItemName.getText().trim();
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Item name required.");
                return;
            }
            int qty;
            double price;
            try {
                qty = Integer.parseInt(tfQty.getText().trim());
                price = Double.parseDouble(tfPrice.getText().trim());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Enter valid qty and price.");
                return;
            }
            BillItem item = new BillItem(name, qty, price);
            billItems.add(item);
            recalcTotals();
            tfItemName.clear(); tfQty.clear(); tfPrice.clear();
        });

        btnRemove.setOnAction(e -> {
            BillItem sel = billTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                billItems.remove(sel);
                recalcTotals();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Remove", "Select an item to remove.");
            }
        });

        btnGenerate.setOnAction(e -> {
            if (billItems.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Invoice", "Bill is empty.");
                return;
            }
            String invoice = buildInvoice(tfPatientName.getText().trim(), tfPatientId.getText().trim(), tfDoctor.getText().trim());
            TextArea ta = new TextArea(invoice);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefWidth(600);
            ta.setPrefHeight(400);

            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle("Invoice Preview");
            dlg.getDialogPane().setContent(ta);
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dlg.showAndWait();
        });

        btnSaveInvoice.setOnAction(e -> {
            if (billItems.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Save", "Nothing to save.");
                return;
            }
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("invoice_" + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()) + ".csv");
            File file = fc.showSaveDialog(primaryStage);
            if (file != null) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.append("PatientName,PatientID,Doctor,Item,Qty,UnitPrice,Total\n");
                    for (BillItem b : billItems) {
                        fw.append(String.format("%s,%s,%s,%s,%d,%.2f,%.2f\n",
                                escapeCsv(tfPatientName.getText()), escapeCsv(tfPatientId.getText()), escapeCsv(tfDoctor.getText()),
                                escapeCsv(b.getName()), b.getQuantity(), b.getUnitPrice(), b.getTotal()));
                    }
                    fw.append(String.format(",,,%s,,%.2f,%.2f\n","Subtotal", Double.parseDouble(subtotalLabel.getText()), Double.parseDouble(totalLabel.getText())));
                    showAlert(Alert.AlertType.INFORMATION, "Saved", "Invoice saved to " + file.getAbsolutePath());
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save invoice: " + ex.getMessage());
                }
            }
        });

        btnClear.setOnAction(e -> {
            billItems.clear();
            recalcTotals();
        });

        // double-click inventory suggestion: when user double-clicks a row in inventory (not shown here),
        // they could copy name/price into billing fields. For simplicity we provide context menu below.

        // Add context menu to table for quick "add selected medicine to bill" (if matching inventory found)
        ContextMenu tableMenu = new ContextMenu();
        MenuItem suggestFromInventory = new MenuItem("Suggest from Inventory");
        suggestFromInventory.setOnAction(ev -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Find Medicine");
            dialog.setHeaderText("Enter medicine name to find in inventory (partial match allowed)");
            dialog.setContentText("Name:");
            dialog.showAndWait().ifPresent(query -> {
                String q = query.toLowerCase();
                for (Medicine m : inventory) {
                    if (m.getName().toLowerCase().contains(q)) {
                        tfItemName.setText(m.getName());
                        tfPrice.setText(String.format("%.2f", m.getPrice()));
                        tfQty.setText("1");
                        return;
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Not Found", "No matching medicine in inventory.");
            });
        });
        tableMenu.getItems().add(suggestFromInventory);
        billTable.setContextMenu(tableMenu);

        return root;
    }

    /* -------------------- Pharmacy Pane -------------------- */
    private BorderPane createPharmacyPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        TableView<Medicine> medTable = new TableView<>(inventory);
        medTable.setPrefHeight(420);

        TableColumn<Medicine, String> colName = new TableColumn<>("Medicine");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(400);

        TableColumn<Medicine, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<Medicine, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(100);

        medTable.getColumns().addAll(colName, colPrice, colQty);

        root.setCenter(medTable);

        // bottom: form to add/update/delete
        HBox form = new HBox(8);
        form.setPadding(new Insets(8));
        TextField tfName = new TextField();
        tfName.setPromptText("Medicine name");
        TextField tfPrice = new TextField();
        tfPrice.setPromptText("Price");
        TextField tfQty = new TextField();
        tfQty.setPromptText("Qty");

        Button btnAddMed = new Button("Add");
        Button btnUpdateMed = new Button("Update Selected");
        Button btnDeleteMed = new Button("Delete Selected");

        form.getChildren().addAll(new Label("Name:"), tfName, new Label("Price:"), tfPrice, new Label("Qty:"), tfQty, btnAddMed, btnUpdateMed, btnDeleteMed);
        root.setBottom(form);

        // fill fields when selecting a row
        medTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfName.setText(newSel.getName());
                tfPrice.setText(String.format("%.2f", newSel.getPrice()));
                tfQty.setText(String.valueOf(newSel.getQuantity()));
            }
        });

        btnAddMed.setOnAction(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Name required."); return; }
            try {
                double p = Double.parseDouble(tfPrice.getText().trim());
                int q = Integer.parseInt(tfQty.getText().trim());
                inventory.add(new Medicine(name, p, q));
                tfName.clear(); tfPrice.clear(); tfQty.clear();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Enter valid price and quantity.");
            }
        });

        btnUpdateMed.setOnAction(e -> {
            Medicine sel = medTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.INFORMATION, "Update", "Select a medicine to update."); return; }
            try {
                sel.setName(tfName.getText().trim());
                sel.setPrice(Double.parseDouble(tfPrice.getText().trim()));
                sel.setQuantity(Integer.parseInt(tfQty.getText().trim()));
                medTable.refresh();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Enter valid values.");
            }
        });

        btnDeleteMed.setOnAction(e -> {
            Medicine sel = medTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                inventory.remove(sel);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Delete", "Select a medicine to delete.");
            }
        });

        return root;
    }

    /* -------------------- Helpers -------------------- */
    private void recalcTotals() {
        double subtotal = billItems.stream().mapToDouble(BillItem::getTotal).sum();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        subtotalLabel.setText(String.format("%.2f", subtotal));
        taxLabel.setText(String.format("%.2f", tax));
        totalLabel.setText(String.format("%.2f", total));
    }

    private String buildInvoice(String patientName, String patientId, String doctor) {
        StringBuilder sb = new StringBuilder();
        sb.append("Clinic Invoice\n");
        sb.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        sb.append("Patient: ").append(patientName).append("\n");
        sb.append("Patient ID: ").append(patientId).append("\n");
        sb.append("Doctor: ").append(doctor).append("\n\n");
        sb.append(String.format("%-40s %6s %10s %10s\n", "Item", "Qty", "Unit", "Total"));
        sb.append("-----------------------------------------------------------------\n");
        for (BillItem b : billItems) {
            sb.append(String.format("%-40s %6d %10.2f %10.2f\n", b.getName(), b.getQuantity(), b.getUnitPrice(), b.getTotal()));
        }
        sb.append("-----------------------------------------------------------------\n");
        sb.append(String.format("%-58s %10.2f\n", "Subtotal:", Double.parseDouble(subtotalLabel.getText())));
        sb.append(String.format("%-58s %10.2f\n", "Tax:", Double.parseDouble(taxLabel.getText())));
        sb.append(String.format("%-58s %10.2f\n", "Total:", Double.parseDouble(totalLabel.getText())));

        return sb.toString();
    }

    private static void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    /* -------------------- Data classes -------------------- */
    public static class BillItem {
        private String name;
        private int quantity;
        private double unitPrice;

        public BillItem(String name, int quantity, double unitPrice) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotal() { return quantity * unitPrice; }

        public void setName(String name) { this.name = name; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }

    public static class Medicine {
        private String name;
        private double price;
        private int quantity;

        public Medicine(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }

        public void setName(String name) { this.name = name; }
        public void setPrice(double price) { this.price = price; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
