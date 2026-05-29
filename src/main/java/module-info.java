module uni.gaben.iscat {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.desktop;
    requires javafx.media;
    requires org.dyn4j;
    requires jdk.unsupported.desktop;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires color.thief;

    opens uni.gaben.iscat to javafx.fxml;
    opens uni.gaben.iscat.utils to javafx.fxml;
    opens uni.gaben.iscat.screens.skin_selection to javafx.fxml;
    opens uni.gaben.iscat.screens.scores to javafx.fxml;
    opens uni.gaben.iscat.screens.bestiary to javafx.fxml;
    opens uni.gaben.iscat.screens.options to javafx.fxml;
    opens uni.gaben.iscat.screens.main_menu to javafx.fxml;
    opens uni.gaben.iscat.screens.confirmation_overlay to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.view;
    exports uni.gaben.iscat.screens.game.model;
    exports uni.gaben.iscat.screens.game.controller;
    exports uni.gaben.iscat.universe.rendering;
    exports uni.gaben.iscat.universe.camera;
    exports uni.gaben.iscat.universe.lib.abstracts;
    exports uni.gaben.iscat.universe.lib.interfaces.model;
    exports uni.gaben.iscat.universe.lib.interfaces.view;
    exports uni.gaben.iscat.universe.lib.interfaces.controller;
    exports uni.gaben.iscat.universe.player;
    exports uni.gaben.iscat.universe.consumables.heart;
    exports uni.gaben.iscat.universe.enviroment.asteroid;
    exports uni.gaben.iscat.universe.enviroment.starfield;
    exports uni.gaben.iscat.universe.projectiles;
    exports uni.gaben.iscat.universe.enemies.mob;
    exports uni.gaben.iscat.universe.enemies.eater;
    exports uni.gaben.iscat.universe.enemies.worm;
    exports uni.gaben.iscat.screens.options;
    exports uni.gaben.iscat.screens.main_menu;
    exports uni.gaben.iscat.utils.sprite;
    exports uni.gaben.iscat.controller;
    exports uni.gaben.iscat.universe.lib.implementations.attacks;
    exports uni.gaben.iscat.universe.brain;
    exports uni.gaben.iscat.universe.brain.actions;
    exports uni.gaben.iscat.universe.brain.modifiers;
    exports uni.gaben.iscat.universe.brain.goals;


    opens uni.gaben.iscat.utils.sprite to javafx.fxml;
    exports uni.gaben.iscat.screens.login;
    opens uni.gaben.iscat.view to javafx.fxml;
    opens uni.gaben.iscat.controller to javafx.fxml;
    exports uni.gaben.iscat.model;
    opens uni.gaben.iscat.model to javafx.fxml;
    exports uni.gaben.iscat.screens.game.view;
    exports uni.gaben.iscat.universe;
    exports uni.gaben.iscat.screens.login.model;
    exports uni.gaben.iscat.database;
    opens uni.gaben.iscat.database to javafx.fxml;
    exports uni.gaben.iscat.utils.theme;
    opens uni.gaben.iscat.utils.theme to javafx.fxml;
    exports uni.gaben.iscat.universe.brain.modifiers.flocking;
}
