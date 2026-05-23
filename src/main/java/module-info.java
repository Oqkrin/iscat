module uni.gaben.iscat {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.desktop;
    requires javafx.media;
    requires org.dyn4j;
    requires jdk.unsupported.desktop;

    opens uni.gaben.iscat to javafx.fxml;
    opens uni.gaben.iscat.utils to javafx.fxml;
    opens uni.gaben.iscat.menus.skin_menu to javafx.fxml;
    opens uni.gaben.iscat.menus.score_menu to javafx.fxml;
    opens uni.gaben.iscat.menus.bestiary_menu to javafx.fxml;
    opens uni.gaben.iscat.menus.options_menu to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.components;
    exports uni.gaben.iscat.game.model;
    exports uni.gaben.iscat.game.controller;
    exports uni.gaben.iscat.game.view;
    exports uni.gaben.iscat.game.view.camera;
    exports uni.gaben.iscat.game.lib.abstracts;
    exports uni.gaben.iscat.game.lib.interfaces.model;
    exports uni.gaben.iscat.game.lib.interfaces.view;
    exports uni.gaben.iscat.game.lib.interfaces.controller;
    exports uni.gaben.iscat.game.universe;
    exports uni.gaben.iscat.game.universe.player;
    exports uni.gaben.iscat.game.universe.heart;
    exports uni.gaben.iscat.game.universe.asteroid;
    exports uni.gaben.iscat.game.universe.starfield;
    exports uni.gaben.iscat.game.universe.projectiles;
    exports uni.gaben.iscat.game.universe.enemies.iscat_mob;
    exports uni.gaben.iscat.game.universe.enemies.iscat_eater;
    exports uni.gaben.iscat.game.universe.enemies.iscat_worm;
    exports uni.gaben.iscat.menus.options_menu;
    exports uni.gaben.iscat.menus.main_menu;
    exports uni.gaben.iscat.utils.sprite;

    opens uni.gaben.iscat.utils.sprite to javafx.fxml;
    exports uni.gaben.iscat.menus.login_menu;
}
