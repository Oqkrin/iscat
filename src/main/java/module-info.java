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
    exports uni.gaben.iscat.menus.login_menu.controller;
    exports uni.gaben.iscat.menus.login_menu.model;
    exports uni.gaben.iscat.menus.login_menu.view;
    exports uni.gaben.iscat.game;
    exports uni.gaben.iscat.game.components.entities;
    exports uni.gaben.iscat.game.components.entities.npcs;
    exports uni.gaben.iscat.game.components.entities.npcs.iscat_bomber;
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
    exports uni.gaben.iscat.gamenex.universe;
    exports uni.gaben.iscat.gamenex.controller;
    exports uni.gaben.iscat.gamenex.view;
    exports uni.gaben.iscat.gamenex.model;
    exports uni.gaben.iscat.gamenex.universe.player;
    exports uni.gaben.iscat.gamenex.universe.starfield;
    exports uni.gaben.iscat.gamenex.lib.interfaces.model;
    exports uni.gaben.iscat.gamenex.lib.interfaces.view;
    exports uni.gaben.iscat.gamenex.lib.abstracts;
    exports uni.gaben.iscat.gamenex.universe.enemies.iscat_mob;
    exports uni.gaben.iscat.gamenex.universe.hearth;
    exports uni.gaben.iscat.gamenex.universe.asteroid;
    exports uni.gaben.iscat.gamenex.universe.enemies.iscat_eater;
    exports uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head;
    exports uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part;
    exports uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_tail;
    exports uni.gaben.iscat.gamenex.universe.projectiles;
    exports uni.gaben.iscat.menus.options_menu;
    exports uni.gaben.iscat.menus.main_menu;
    exports uni.gaben.iscat.utils.sprite;
    opens uni.gaben.iscat.utils.sprite to javafx.fxml;


}
