package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.universe.enemies.generic.GenericPhysicalEntitySettings;

import java.util.List;
import java.util.Optional;

public interface EnemyDAO {

    /** Incrementa il contatore delle uccisioni per un utente e nemico */
    void incrementKill(int userId, String entityKey);

    /** Estrae la lista completa del bestiario per un utente */
    List<BestiarioEntry> getBestiarioForUser(int userId);

    /** Cerca un nemico per chiave */
    Optional<GenericPhysicalEntitySettings> findByKey(String entityKey);

    /** Estrae tutti i nemici */
    List<GenericPhysicalEntitySettings> findAll();

    // DTO interno per il bestiario
    record BestiarioEntry(String name, String description, String spritePath, int killCount, boolean isUnlocked) {}
}