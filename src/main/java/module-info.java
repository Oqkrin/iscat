module uni.gaben.iscat {

    requires color.thief;
    requires java.desktop;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.unsupported.desktop;
    requires org.dyn4j;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires org.xerial.sqlitejdbc;

    opens uni.gaben.iscat to javafx.fxml;
    opens uni.gaben.iscat.controller to javafx.fxml;
    opens uni.gaben.iscat.database to javafx.fxml;
    opens uni.gaben.iscat.database.dao to javafx.fxml;
    opens uni.gaben.iscat.database.sqlite to javafx.fxml;
    opens uni.gaben.iscat.model to javafx.fxml;

    opens uni.gaben.iscat.screens.bestiary to javafx.fxml;
    opens uni.gaben.iscat.screens.confirmation_overlay to javafx.fxml;
    opens uni.gaben.iscat.controller.game to javafx.fxml;
    opens uni.gaben.iscat.screens.leaderboard to javafx.fxml;
    opens uni.gaben.iscat.screens.main_menu to javafx.fxml;
    opens uni.gaben.iscat.controller.components.options to javafx.fxml;
    opens uni.gaben.iscat.screens.scores to javafx.fxml;
    opens uni.gaben.iscat.screens.skin_selection to javafx.fxml;

    opens uni.gaben.iscat.utils to javafx.fxml;
    opens uni.gaben.iscat.utils.sprite to javafx.fxml;
    opens uni.gaben.iscat.utils.theme to javafx.fxml;
    opens uni.gaben.iscat.view to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.controller;
    exports uni.gaben.iscat.database;
    exports uni.gaben.iscat.database.dao;
    exports uni.gaben.iscat.database.sqlite;
    exports uni.gaben.iscat.model;

    exports uni.gaben.iscat.screens.confirmation_overlay;
    exports uni.gaben.iscat.controller.game;
    exports uni.gaben.iscat.model.game;
    exports uni.gaben.iscat.view.game;
    exports uni.gaben.iscat.screens.login;
    exports uni.gaben.iscat.model.login;
    exports uni.gaben.iscat.screens.main_menu;
    exports uni.gaben.iscat.controller.components.options;

    exports uni.gaben.iscat.universe;
    exports uni.gaben.iscat.universe.brain;
    exports uni.gaben.iscat.universe.brain.actions;
    exports uni.gaben.iscat.universe.brain.goals;
    exports uni.gaben.iscat.universe.brain.modifiers;
    exports uni.gaben.iscat.universe.brain.modifiers.flocking;
    exports uni.gaben.iscat.universe.camera;
    exports uni.gaben.iscat.universe.consumables.heart;
    exports uni.gaben.iscat.universe.enemies.worm;
    exports uni.gaben.iscat.universe.enviroment.asteroid;
    exports uni.gaben.iscat.universe.enviroment.starfield;
    exports uni.gaben.iscat.universe.lib.abstracts;
    exports uni.gaben.iscat.universe.lib.implementations.attacks;
    exports uni.gaben.iscat.universe.lib.interfaces.controller;
    exports uni.gaben.iscat.universe.lib.interfaces.model;
    exports uni.gaben.iscat.universe.lib.interfaces.view;
    exports uni.gaben.iscat.universe.player;
    exports uni.gaben.iscat.universe.projectiles;
    exports uni.gaben.iscat.universe.rendering;
    exports uni.gaben.iscat.universe.brain.targets;

    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.sprite;
    exports uni.gaben.iscat.utils.theme;
    exports uni.gaben.iscat.view;
    opens uni.gaben.iscat.model.game to javafx.fxml;
    exports uni.gaben.iscat.controller.components;
    opens uni.gaben.iscat.controller.components to javafx.fxml;
    exports uni.gaben.iscat.model.user;
    exports uni.gaben.iscat.view.components;
    opens uni.gaben.iscat.view.components to javafx.fxml;
}