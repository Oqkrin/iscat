module uni.gaben.iscat {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.desktop;
    requires javafx.media;

    opens uni.gaben.iscat to javafx.fxml;
    opens uni.gaben.iscat.utils to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.utils.components;
    exports uni.gaben.iscat.login.controller;
    exports uni.gaben.iscat.login.model;
    exports uni.gaben.iscat.login.view;
    exports uni.gaben.iscat.menu.controller;
    exports uni.gaben.iscat.menu.view;
    exports uni.gaben.iscat.game;
    exports uni.gaben.iscat.game.entities;
    exports uni.gaben.iscat.game.enemies;
    exports uni.gaben.iscat.game.enemies.iscat_bomber;
    exports uni.gaben.iscat.game.enemies.fake_iscat;
    exports uni.gaben.iscat.game.enemies.fallen_star_golem;
    exports uni.gaben.iscat.game.hud;
    exports uni.gaben.iscat.game.input;
    exports uni.gaben.iscat.game.interfaces;
    exports uni.gaben.iscat.game.physics;
    exports uni.gaben.iscat.game.player;
    exports uni.gaben.iscat.game.player.controller;
    exports uni.gaben.iscat.game.projectile;
    exports uni.gaben.iscat.game.settings;
    exports uni.gaben.iscat.game.space;
}
