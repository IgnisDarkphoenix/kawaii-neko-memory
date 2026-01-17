package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Gestor de guardado de progreso del juego
 * Usa Preferences de libGDX (wrapper de SharedPreferences en Android)
 * 
 * @author DarkphoenixTeam
 */
public class SaveManager {
    
    private static final String TAG = "SaveManager";
    private static final String PREFS_NAME = "KawaiiNekoSave";
    
    // Keys de guardado
    private static final String KEY_NEKOINS = "nekoins";
    private static final String KEY_LEVEL_COMPLETED = "level_completed_"; // + levelId
    private static final String KEY_DECK_UNLOCKED = "deck_unlocked_"; // + deckIndex
    private static final String KEY_HINT_LEVEL = "hint_level";
    private static final String KEY_TIMEFREEZE_LEVEL = "timefreeze_level";
    private static final String KEY_CURRENT_DECK = "current_deck";
    
    // Singleton
    private static SaveManager instance;
    private Preferences prefs;
    
    private SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Inicializar valores por defecto si es primera vez
        if (!prefs.contains(KEY_NEKOINS)) {
            prefs.putInteger(KEY_NEKOINS, 0);
            prefs.putBoolean(KEY_DECK_UNLOCKED + "0", true); // Deck base desbloqueado
            prefs.putInteger(KEY_CURRENT_DECK, 0); // Deck base activo
            prefs.putInteger(KEY_HINT_LEVEL, 0);
            prefs.putInteger(KEY_TIMEFREEZE_LEVEL, 0);
            prefs.flush();
            Gdx.app.log(TAG, "Progreso inicial creado");
        }
    }
    
    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }
    
    // ==================== NEKOINS ====================
    
    public int getNekoins() {
        return prefs.getInteger(KEY_NEKOINS, 0);
    }
    
    public void addNekoins(int amount) {
        int current = getNekoins();
        prefs.putInteger(KEY_NEKOINS, current + amount);
        prefs.flush();
        Gdx.app.log(TAG, "Nekoins: " + current + " → " + (current + amount));
    }
    
    public boolean spendNekoins(int amount) {
        int current = getNekoins();
        if (current >= amount) {
            prefs.putInteger(KEY_NEKOINS, current - amount);
            prefs.flush();
            Gdx.app.log(TAG, "Gastados " + amount + " nekoins (quedan " + (current - amount) + ")");
            return true;
        }
        Gdx.app.log(TAG, "No hay suficientes nekoins (tienes " + current + ", necesitas " + amount + ")");
        return false;
    }
    
    // ==================== NIVELES ====================
    
    /**
     * Marca un nivel como completado
     * @param levelId ID único del nivel (0-199)
     */
    public void setLevelCompleted(int levelId) {
        prefs.putBoolean(KEY_LEVEL_COMPLETED + levelId, true);
        prefs.flush();
        Gdx.app.log(TAG, "Nivel " + levelId + " completado");
    }
    
    /**
     * Verifica si un nivel está completado
     * @param levelId ID único del nivel (0-199)
     */
    public boolean isLevelCompleted(int levelId) {
        return prefs.getBoolean(KEY_LEVEL_COMPLETED + levelId, false);
    }
    
    /**
     * Verifica si un nivel está desbloqueado
     * Regla: Nivel 0 de cada dificultad + niveles previos completados
     */
    public boolean isLevelUnlocked(int levelId) {
        // Niveles 0, 50, 100, 150 siempre desbloqueados (primer nivel de cada dificultad)
        if (levelId % 50 == 0) {
            return true;
        }
        
        // Resto: desbloqueado si el anterior está completado
        return isLevelCompleted(levelId - 1);
    }
    
    // ==================== MAZOS ====================
    
    public boolean isDeckUnlocked(int deckIndex) {
        return prefs.getBoolean(KEY_DECK_UNLOCKED + deckIndex, false);
    }
    
    public void unlockDeck(int deckIndex) {
        prefs.putBoolean(KEY_DECK_UNLOCKED + deckIndex, true);
        prefs.flush();
        Gdx.app.log(TAG, "Deck " + deckIndex + " desbloqueado");
    }
    
    public int getCurrentDeck() {
        return prefs.getInteger(KEY_CURRENT_DECK, 0);
    }
    
    public void setCurrentDeck(int deckIndex) {
        prefs.putInteger(KEY_CURRENT_DECK, deckIndex);
        prefs.flush();
        Gdx.app.log(TAG, "Deck actual: " + deckIndex);
    }
    
    // ==================== POWERS ====================
    
    public int getHintLevel() {
        return prefs.getInteger(KEY_HINT_LEVEL, 0);
    }
    
    public void upgradeHint() {
        int current = getHintLevel();
        prefs.putInteger(KEY_HINT_LEVEL, current + 1);
        prefs.flush();
        Gdx.app.log(TAG, "Hint level: " + current + " → " + (current + 1));
    }
    
    public int getTimeFreezeLevel() {
        return prefs.getInteger(KEY_TIMEFREEZE_LEVEL, 0);
    }
    
    public void upgradeTimeFreeze() {
        int current = getTimeFreezeLevel();
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, current + 1);
        prefs.flush();
        Gdx.app.log(TAG, "TimeFreeze level: " + current + " → " + (current + 1));
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Borra todo el progreso (para testing o botón de reset)
     */
    public void resetAll() {
        prefs.clear();
        prefs.flush();
        Gdx.app.log(TAG, "⚠️ Progreso borrado - reiniciando valores por defecto");
        
        // Re-inicializar valores por defecto
        prefs.putInteger(KEY_NEKOINS, 0);
        prefs.putBoolean(KEY_DECK_UNLOCKED + "0", true);
        prefs.putInteger(KEY_CURRENT_DECK, 0);
        prefs.flush();
    }
    
    /**
     * Obtiene el nivel global del jugador basado en niveles completados
     */
    public int getPlayerLevel() {
        int completed = 0;
        for (int i = 0; i < 200; i++) {
            if (isLevelCompleted(i)) {
                completed++;
            }
        }
        return completed;
    }
    
    /**
     * Obtiene estadísticas del jugador (para debug o pantalla de logros)
     */
    public String getStats() {
        return String.format(
            "Nekoins: %d | Niveles: %d/200 | Decks: %d/5 | Hint Lv: %d | TimeFreeze Lv: %d",
            getNekoins(),
            getPlayerLevel(),
            countUnlockedDecks(),
            getHintLevel(),
            getTimeFreezeLevel()
        );
    }
    
    private int countUnlockedDecks() {
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (isDeckUnlocked(i)) count++;
        }
        return count;
    }
}