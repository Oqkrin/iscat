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
    exports uni.gaben.iscat.game.components.entities;
    exports uni.gaben.iscat.game.components.entities.npcs;
    exports uni.gaben.iscat.game.components.entities.npcs.iscat_bomber;
    exports uni.gaben.iscat.game.components.entities.npcs.fake_iscat;
    exports uni.gaben.iscat.game.components.entities.npcs.fallen_star_golem;
    exports uni.gaben.iscat.game.controller;
    exports uni.gaben.iscat.game.utils.interfaces;
    exports uni.gaben.iscat.game.utils.physics;
    exports uni.gaben.iscat.game.components.entities.player;
    exports uni.gaben.iscat.game.components.entities.player.controller;
    exports uni.gaben.iscat.game.components.entities.player.projectile;
    exports uni.gaben.iscat.game.utils.settings;
    exports uni.gaben.iscat.game.components.space;
    exports uni.gaben.iscat.game.view;
    exports uni.gaben.iscat.game.components.entities.objects;
}
