module uni.gaben.iscat {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens uni.gaben.iscat to javafx.fxml;
    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.rapporto_aureo;
    opens uni.gaben.iscat.utils to javafx.fxml;
    exports uni.gaben.iscat.login.controller;
    exports uni.gaben.iscat.login.model;
    exports uni.gaben.iscat.login.view;
}