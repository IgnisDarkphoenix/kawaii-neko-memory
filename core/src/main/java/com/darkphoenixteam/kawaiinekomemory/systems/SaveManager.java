package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

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
    private static final String KEY_LEVEL_COMPLETED = "level_completed_";
    private static final String KEY_CARD_UNLOCKED = "card_unlocked_";      // + cardId (0-34)
    private static final String KEY_ACTIVE_CARDS = "active_cards";          // CSV de cardIds
    private static final String KEY_HINT_LEVEL = "hint_level";
    private static final String KEY_TIMEFREEZE_LEVEL = "timefreeze_level";
    
    // Constantes
    private static final int TOTAL_CARDS = 35;          // 5 decks × 7 cartas
    private static final int ACTIVE_DECK_SIZE = 15;     // Cartas activas
    private static final int CARDS_PER_DECK = 7;
    
    // Singleton
    private static SaveManager instance;
    private Preferences prefs;
    
    // Cache de cartas activas
    private Array<Integer> activeCards;
    
    private SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        activeCards = new Array<>();
        
        // Inicializar valores por defecto si es primera vez
        if (!prefs.contains(KEY_NEKOINS)) {
            initializeDefaults();
        } else {
            loadActiveCards();
        }
    }
    
    private void initializeDefaults() {
        Gdx.app.log(TAG, "Inicializando progreso por defecto...");
        
        // Nekoins iniciales
        prefs.putInteger(KEY_NEKOINS, 0);
        
        // Desbloquear las primeras 15 cartas (deck 0 completo + deck 1 completo + 1 de deck 2)
        // Esto permite jugar todas las dificultades desde el inicio
        for (int i = 0; i < 15; i++) {
            prefs.putBoolean(KEY_CARD_UNLOCKED + i, true);
        }
        
        // Configurar cartas activas por defecto (las primeras 15)
        StringBuilder activeCardsStr = new StringBuilder();
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (i > 0) activeCardsStr.append(",");
            activeCardsStr.append(i);
            activeCards.add(i);
        }
        prefs.putString(KEY_ACTIVE_CARDS, activeCardsStr.toString());
        
        // Powers iniciales
        prefs.putInteger(KEY_HINT_LEVEL, 0);
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, 0);
        
        prefs.flush();
        Gdx.app.log(TAG, "Progreso inicial creado - 15 cartas desbloqueadas y activas");
    }
    
    private void loadActiveCards() {
        activeCards.clear();
        String activeCardsStr = prefs.getString(KEY_ACTIVE_CARDS, "");
        
        if (!activeCardsStr.isEmpty()) {
            String[] parts = activeCardsStr.split(",");
            for (String part : parts) {
                try {
                    int cardId = Integer.parseInt(part.trim());
                    activeCards.add(cardId);
                } catch (NumberFormatException e) {
                    Gdx.app.error(TAG, "Error parseando cardId: " + part);
                }
            }
        }
        
        // Si no hay cartas activas válidas, reinicializar
        if (activeCards.size == 0) {
            Gdx.app.log(TAG, "No hay cartas activas, reinicializando...");
            initializeDefaults();
        }
        
        Gdx.app.log(TAG, "Cartas activas cargadas: " + activeCards.size);
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
    
    // ==================== CARTAS INDIVIDUALES ====================
    
    /**
     * Verifica si una carta está desbloqueada
     * @param cardId ID único de la carta (0-34)
     */
    public boolean isCardUnlocked(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return false;
        return prefs.getBoolean(KEY_CARD_UNLOCKED + cardId, false);
    }
    
    /**
     * Desbloquea una carta individual
     * @param cardId ID único de la carta (0-34)
     */
    public void unlockCard(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return;
        prefs.putBoolean(KEY_CARD_UNLOCKED + cardId, true);
        prefs.flush();
        Gdx.app.log(TAG, "Carta desbloqueada: " + cardId + " (Deck " + getDeckFromCardId(cardId) + 
                         ", Card " + getCardIndexFromCardId(cardId) + ")");
    }
    
    /**
     * Obtiene todas las cartas desbloqueadas
     * @return Array con los IDs de cartas desbloqueadas
     */
    public Array<Integer> getUnlockedCards() {
        Array<Integer> unlocked = new Array<>();
        for (int i = 0; i < TOTAL_CARDS; i++) {
            if (isCardUnlocked(i)) {
                unlocked.add(i);
            }
        }
        return unlocked;
    }
    
    /**
     * Cuenta cuántas cartas están desbloqueadas
     */
    public int getUnlockedCardCount() {
        int count = 0;
        for (int i = 0; i < TOTAL_CARDS; i++) {
            if (isCardUnlocked(i)) count++;
        }
        return count;
    }
    
    // ==================== CARTAS ACTIVAS (15 slots) ====================
    
    /**
     * Obtiene las cartas activas actuales
     * @return Array con los IDs de las 15 cartas activas
     */
    public Array<Integer> getActiveCards() {
        return new Array<>(activeCards);
    }
    
    /**
     * Verifica si una carta está en el mazo activo
     * @param cardId ID de la carta
     */
    public boolean isCardActive(int cardId) {
        return activeCards.contains(cardId, false);
    }
    
    /**
     * Establece una carta en un slot específico
     * @param slotIndex Índice del slot (0-14)
     * @param cardId ID de la carta a colocar (-1 para vaciar)
     * @return true si se pudo colocar
     */
    public boolean setActiveCardSlot(int slotIndex, int cardId) {
        if (slotIndex < 0 || slotIndex >= ACTIVE_DECK_SIZE) return false;
        
        // Vaciar slot
        if (cardId == -1) {
            if (slotIndex < activeCards.size) {
                activeCards.set(slotIndex, -1);
                saveActiveCards();
                return true;
            }
            return false;
        }
        
        // Verificar que la carta esté desbloqueada
        if (!isCardUnlocked(cardId)) {
            Gdx.app.log(TAG, "Carta " + cardId + " no está desbloqueada");
            return false;
        }
        
        // Verificar que no esté duplicada
        if (isCardActive(cardId)) {
            Gdx.app.log(TAG, "Carta " + cardId + " ya está activa (no duplicados)");
            return false;
        }
        
        // Colocar carta
        while (activeCards.size <= slotIndex) {
            activeCards.add(-1);
        }
        activeCards.set(slotIndex, cardId);
        saveActiveCards();
        
        Gdx.app.log(TAG, "Carta " + cardId + " colocada en slot " + slotIndex);
        return true;
    }
    
    /**
     * Intercambia dos slots
     */
    public void swapActiveCards(int slot1, int slot2) {
        if (slot1 < 0 || slot1 >= activeCards.size) return;
        if (slot2 < 0 || slot2 >= activeCards.size) return;
        
        int temp = activeCards.get(slot1);
        activeCards.set(slot1, activeCards.get(slot2));
        activeCards.set(slot2, temp);
        saveActiveCards();
    }
    
    /**
     * Quita una carta del mazo activo
     * @param cardId ID de la carta a quitar
     * @return Índice del slot donde estaba (-1 si no estaba)
     */
    public int removeActiveCard(int cardId) {
        int index = activeCards.indexOf(cardId, false);
        if (index >= 0) {
            activeCards.set(index, -1);
            saveActiveCards();
            Gdx.app.log(TAG, "Carta " + cardId + " removida del slot " + index);
        }
        return index;
    }
    
    /**
     * Agrega una carta al primer slot vacío
     * @param cardId ID de la carta
     * @return Índice del slot donde se colocó (-1 si no hay espacio o error)
     */
    public int addActiveCard(int cardId) {
        // Verificar desbloqueada
        if (!isCardUnlocked(cardId)) return -1;
        
        // Verificar no duplicada
        if (isCardActive(cardId)) return -1;
        
        // Buscar primer slot vacío
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (i >= activeCards.size || activeCards.get(i) == -1) {
                setActiveCardSlot(i, cardId);
                return i;
            }
        }
        
        return -1; // No hay espacio
    }
    
    /**
     * Obtiene cuántos slots activos están ocupados
     */
    public int getActiveCardCount() {
        int count = 0;
        for (int i = 0; i < activeCards.size; i++) {
            if (activeCards.get(i) >= 0) count++;
        }
        return count;
    }
    
    private void saveActiveCards() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activeCards.size; i++) {
            if (i > 0) sb.append(",");
            sb.append(activeCards.get(i));
        }
        prefs.putString(KEY_ACTIVE_CARDS, sb.toString());
        prefs.flush();
    }
    
    // ==================== UTILIDADES DE CARTAS ====================
    
    /**
     * Convierte cardId a índice de deck (0-4)
     */
    public static int getDeckFromCardId(int cardId) {
        return cardId / CARDS_PER_DECK;
    }
    
    /**
     * Convierte cardId a índice dentro del deck (0-6)
     */
    public static int getCardIndexFromCardId(int cardId) {
        return cardId % CARDS_PER_DECK;
    }
    
    /**
     * Convierte deck + cardIndex a cardId global
     */
    public static int getCardId(int deckIndex, int cardIndex) {
        return deckIndex * CARDS_PER_DECK + cardIndex;
    }
    
    /**
     * Obtiene el valor en nekoins de una carta
     */
    public int getCardNekoinValue(int cardId) {
        int deckIndex = getDeckFromCardId(cardId);
        int[] values = {1, 2, 3, 5, 7};
        if (deckIndex >= 0 && deckIndex < values.length) {
            return values[deckIndex];
        }
        return 1;
    }
    
    // ==================== NIVELES ====================
    
    public void setLevelCompleted(int levelId) {
        prefs.putBoolean(KEY_LEVEL_COMPLETED + levelId, true);
        prefs.flush();
        Gdx.app.log(TAG, "Nivel " + levelId + " completado");
    }
    
    public boolean isLevelCompleted(int levelId) {
        return prefs.getBoolean(KEY_LEVEL_COMPLETED + levelId, false);
    }
    
    public boolean isLevelUnlocked(int levelId) {
        if (levelId % 50 == 0) {
            return true;
        }
        return isLevelCompleted(levelId - 1);
    }
    
    // ==================== DECK ACTIVO (LEGACY - para compatibilidad) ====================
    
    /**
     * @deprecated Usar getActiveCards() en su lugar
     */
    public int getCurrentDeck() {
        // Retornar el deck de la primera carta activa
        if (activeCards.size > 0 && activeCards.get(0) >= 0) {
            return getDeckFromCardId(activeCards.get(0));
        }
        return 0;
    }
    
    /**
     * @deprecated Usar sistema de cartas individuales
     */
    public void setCurrentDeck(int deckIndex) {
        // Legacy - no hacer nada
    }
    
    /**
     * @deprecated Usar isCardUnlocked()
     */
    public boolean isDeckUnlocked(int deckIndex) {
        // Un deck está "desbloqueado" si al menos una carta está desbloqueada
        int startId = deckIndex * CARDS_PER_DECK;
        for (int i = 0; i < CARDS_PER_DECK; i++) {
            if (isCardUnlocked(startId + i)) return true;
        }
        return false;
    }
    
    // ==================== POWERS ====================
    
    public int getHintLevel() {
        return prefs.getInteger(KEY_HINT_LEVEL, 0);
    }
    
    public void upgradeHint() {
        int current = getHintLevel();
        prefs.putInteger(KEY_HINT_LEVEL, current + 1);
        prefs.flush();
    }
    
    public int getTimeFreezeLevel() {
        return prefs.getInteger(KEY_TIMEFREEZE_LEVEL, 0);
    }
    
    public void upgradeTimeFreeze() {
        int current = getTimeFreezeLevel();
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, current + 1);
        prefs.flush();
    }
    
    // ==================== UTILIDADES ====================
    
    public void resetAll() {
        prefs.clear();
        prefs.flush();
        activeCards.clear();
        Gdx.app.log(TAG, "⚠️ Progreso borrado");
        initializeDefaults();
    }
    
    public int getPlayerLevel() {
        int completed = 0;
        for (int i = 0; i < 200; i++) {
            if (isLevelCompleted(i)) completed++;
        }
        return completed;
    }
    
    public String getStats() {
        return String.format(
            "Nekoins: %d | Niveles: %d/200 | Cartas: %d/35 | Activas: %d/15",
            getNekoins(),
            getPlayerLevel(),
            getUnlockedCardCount(),
            getActiveCardCount()
        );
    }
}