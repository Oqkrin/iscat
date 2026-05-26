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
    opens uni.gaben.iscat.iscat_screens.skin_selection to javafx.fxml;
    opens uni.gaben.iscat.iscat_screens.scores to javafx.fxml;
    opens uni.gaben.iscat.iscat_screens.bestiary to javafx.fxml;
    opens uni.gaben.iscat.iscat_screens.options to javafx.fxml;

    exports uni.gaben.iscat;
    exports uni.gaben.iscat.utils;
    exports uni.gaben.iscat.iscat_m_view_c;
    exports uni.gaben.iscat.iscat_screens.game.model;
    exports uni.gaben.iscat.iscat_screens.game.controller;
    exports uni.gaben.iscat.iscat_game.rendering;
    exports uni.gaben.iscat.iscat_game.camera;
    exports uni.gaben.iscat.iscat_game.lib.abstracts;
    exports uni.gaben.iscat.iscat_game.lib.interfaces.model;
    exports uni.gaben.iscat.iscat_game.lib.interfaces.view;
    exports uni.gaben.iscat.iscat_game.lib.interfaces.controller;
    exports uni.gaben.iscat.iscat_game.universe;
    exports uni.gaben.iscat.iscat_game.universe.player;
    exports uni.gaben.iscat.iscat_game.universe.heart;
    exports uni.gaben.iscat.iscat_game.universe.asteroid;
    exports uni.gaben.iscat.iscat_game.universe.starfield;
    exports uni.gaben.iscat.iscat_game.universe.projectiles;
    exports uni.gaben.iscat.iscat_game.universe.iscats.mob;
    exports uni.gaben.iscat.iscat_game.universe.iscats.eater;
    exports uni.gaben.iscat.iscat_game.universe.iscats.worm;
    exports uni.gaben.iscat.iscat_screens.options;
    exports uni.gaben.iscat.iscat_screens.main_menu;
    exports uni.gaben.iscat.utils.sprite;
    exports uni.gaben.iscat.iscat_mv_controller;
    exports uni.gaben.iscat.iscat_game.lib.implementations.attacks;


    opens uni.gaben.iscat.utils.sprite to javafx.fxml;
    exports uni.gaben.iscat.iscat_screens.login;
    opens uni.gaben.iscat.iscat_m_view_c to javafx.fxml;
    opens uni.gaben.iscat.iscat_mv_controller to javafx.fxml;
    exports uni.gaben.iscat.iscat_model_vc;
    opens uni.gaben.iscat.iscat_model_vc to javafx.fxml;
    exports uni.gaben.iscat.iscat_game.utils;
    exports uni.gaben.iscat.iscat_screens.game.view;
}
