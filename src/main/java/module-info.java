module uni.gaben.iscat {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.desktop;

    opens uni.gaben.iscat to javafx.fxml;
    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.rapporto_aureo;
    opens uni.gaben.iscat.utils to javafx.fxml;
    exports uni.gaben.iscat.login.controller;
    exports uni.gaben.iscat.login.model;
    exports uni.gaben.iscat.login.view;
    exports uni.gaben.iscat.game.model.entities;
    exports uni.gaben.iscat.game.view;
    exports uni.gaben.iscat.game.controller;
}