package lk.ijse.dep11.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep11.pos.db.ItemDataAccess;
import lk.ijse.dep11.pos.db.OrderDataAccess;
import lk.ijse.dep11.pos.tm.Item;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ManageItemFormController {
    public AnchorPane root;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public TableView<Item> tblItems;
    public JFXTextField txtUnitPrice;
    public JFXButton btnAddNew;

    public void initialize(){
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        btnDelete.setDisable(true);
        btnSave.setDefaultButton(true);
        try {
            tblItems.getItems().addAll(ItemDataAccess.getAllItems());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load items, try later").show();
            e.printStackTrace();
        }
        Platform.runLater(txtCode::requestFocus);
        tblItems.getSelectionModel().selectedItemProperty().addListener((ov, prev, cur)->{
            if (cur == null){
                btnSave.setText("SAVE");
                btnDelete.setDisable(true);
                txtCode.setDisable(false);
            }else{
                btnSave.setText("UPDATE");
                btnDelete.setDisable(false);
                txtCode.setText(cur.getCode());
                txtCode.setDisable(true);
                txtDescription.setText(cur.getDescription());
                txtQtyOnHand.setText(cur.getQty()  + "");
                txtUnitPrice.setText(cur.getUnitPrice() + "");
            }
        });
    }

    public void navigateToHome(MouseEvent mouseEvent) throws IOException {
        MainFormController.navigateToMain(root);
    }

    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        for (TextField textField : new TextField[]{txtCode, txtDescription, txtQtyOnHand, txtUnitPrice})
            textField.clear();
        txtCode.requestFocus();
        tblItems.getSelectionModel().clearSelection();
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        if (!isDataValid()) return;

        try {
            Item item = new Item(txtCode.getText().strip(), txtDescription.getText().strip(),
                    Integer.parseInt(txtQtyOnHand.getText()), new BigDecimal(txtUnitPrice.getText()).setScale(2));

            if (btnSave.getText().equals("SAVE")) {
                if (ItemDataAccess.existsItem(item.getCode())) {
                    new Alert(Alert.AlertType.ERROR, "Item code already exists").show();
                    txtCode.requestFocus();
                    txtCode.selectAll();
                    return;
                }
                ItemDataAccess.saveItem(item);
                tblItems.getItems().add(item);
            }else{
                ItemDataAccess.updateItem(item);
                ObservableList<Item> itemList = tblItems.getItems();
                Item selectedItem = tblItems.getSelectionModel().getSelectedItem();
                itemList.set(itemList.indexOf(selectedItem), item);
                tblItems.refresh();
            }
            btnAddNew.fire();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save the item, try again").show();
        }
    }

    private boolean isDataValid(){
        String code = txtCode.getText().strip();
        String description = txtDescription.getText().strip();
        String qty = txtQtyOnHand.getText().strip();
        String unitPrice = txtUnitPrice.getText().strip();

        if (!code.matches("\\d{4,}")){
            txtCode.requestFocus();
            txtCode.selectAll();
            return false;
        }else if (!description.matches("[A-Za-z0-9 ]{4,}")){
            txtDescription.requestFocus();
            txtDescription.selectAll();
            return false;
        }else if(!qty.matches("\\d+") || Integer.parseInt(qty) <= 0){
            txtQtyOnHand.requestFocus();
            txtQtyOnHand.selectAll();
            return false;
        }else if(!isPrice(unitPrice)){
            txtUnitPrice.requestFocus();
            txtUnitPrice.selectAll();
            return false;
        }

        return true;
    }

    private boolean isPrice(String input){
        try {
            double price = Double.parseDouble(input);
            return price > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        Item selectedItem = tblItems.getSelectionModel().getSelectedItem();
        try {
            if (OrderDataAccess.existsOrderByItemCode(selectedItem.getCode())){
                new Alert(Alert.AlertType.ERROR, "Failed to delete, the item already associated with an order").show();
            }else{
                ItemDataAccess.deleteItem(selectedItem.getCode());
                tblItems.getItems().remove(selectedItem);
                if (tblItems.getItems().isEmpty()) btnAddNew.fire();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to delete the item, try again").show();
        }
    }
}
