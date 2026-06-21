module uni.gaben.iscat {

    requires color.thief;
    requires java.desktop;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.unsupported.desktop;
    requires org.dyn4j;
    requires org.json;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;

    opens uni.gaben.iscat to javafx.fxml;
    opens uni.gaben.iscat.controller to javafx.fxml;
    opens uni.gaben.iscat.controller.components to javafx.fxml;
    opens uni.gaben.iscat.controller.components.settings to javafx.fxml;
    opens uni.gaben.iscat.controller.game to javafx.fxml;
    opens uni.gaben.iscat.controller.interfaces to javafx.fxml;
    opens uni.gaben.iscat.controller.menus to javafx.fxml;
    opens uni.gaben.iscat.database to javafx.fxml;
    opens uni.gaben.iscat.database.dao to javafx.fxml;
    opens uni.gaben.iscat.database.sqlite to javafx.fxml;
    opens uni.gaben.iscat.model to javafx.fxml;
    opens uni.gaben.iscat.model.game to javafx.fxml;
    opens uni.gaben.iscat.universe to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.effects to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.entities to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.entities.interfaces to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.entities.parsed to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.entities.worm to javafx.fxml, org.json;
    opens uni.gaben.iscat.universe.spawn to javafx.fxml, org.json;
    opens uni.gaben.iscat.utils to javafx.fxml;
    opens uni.gaben.iscat.utils.sprite to javafx.fxml;
    opens uni.gaben.iscat.utils.theme to javafx.fxml;
    opens uni.gaben.iscat.view to javafx.fxml;
    opens uni.gaben.iscat.view.components to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.controller;
    exports uni.gaben.iscat.controller.components;
    exports uni.gaben.iscat.controller.components.settings;
    exports uni.gaben.iscat.controller.game;
    exports uni.gaben.iscat.controller.interfaces;
    exports uni.gaben.iscat.controller.menus;
    exports uni.gaben.iscat.database;
    exports uni.gaben.iscat.database.dao;
    exports uni.gaben.iscat.database.sqlite;
    exports uni.gaben.iscat.model;
    exports uni.gaben.iscat.model.game;
    exports uni.gaben.iscat.model.login;
    exports uni.gaben.iscat.model.user;

    exports uni.gaben.iscat.universe;
    exports uni.gaben.iscat.universe.camera;
    exports uni.gaben.iscat.universe.effects;
    exports uni.gaben.iscat.universe.rendering;
    exports uni.gaben.iscat.universe.spawn;

    exports uni.gaben.iscat.universe.entities;
    exports uni.gaben.iscat.universe.entities.brain;
    exports uni.gaben.iscat.universe.entities.brain.abilities;
    exports uni.gaben.iscat.universe.entities.brain.rotation;
    exports uni.gaben.iscat.universe.entities.brain.steering;
    exports uni.gaben.iscat.universe.entities.brain.target;
    exports uni.gaben.iscat.universe.entities.boosts.heart;
    exports uni.gaben.iscat.universe.entities.projectiles;
    exports uni.gaben.iscat.universe.entities.interfaces;
    exports uni.gaben.iscat.universe.entities.parsed;
    exports uni.gaben.iscat.universe.entities.player;
    exports uni.gaben.iscat.universe.entities.shooters;
    exports uni.gaben.iscat.universe.entities.worm;

    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.sprite;
    exports uni.gaben.iscat.utils.theme;
    exports uni.gaben.iscat.view;
    exports uni.gaben.iscat.view.components;
    exports uni.gaben.iscat.view.game;
    exports uni.gaben.iscat.view.game.debug;
    exports uni.gaben.iscat.utils.audio;
    opens uni.gaben.iscat.utils.audio to javafx.fxml;
    exports uni.gaben.iscat.universe.entities.asteroids;
    opens uni.gaben.iscat.universe.entities.asteroids to javafx.fxml;
}