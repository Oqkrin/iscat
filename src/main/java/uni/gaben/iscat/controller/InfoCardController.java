package uni.gaben.iscat.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class InfoCardController {

    @FXML private Label rightCardHeader;
    @FXML private TextArea description;

    @FXML
    public void initialize() {
        description.setEditable(false);
        description.setWrapText(true);
    }

    public void updateInfo(String header, String content) {
        rightCardHeader.setText(header.toUpperCase());
        description.setText(content);
    }
}