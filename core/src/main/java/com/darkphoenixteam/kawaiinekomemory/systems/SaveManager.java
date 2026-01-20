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
    private static final String KEY_CARD_UNLOCKED = "card_unlocked_";
    private static final String KEY_ACTIVE_CARDS = "active_cards";
    private static final String KEY_HINT_LEVEL = "hint_level";
    private static final String KEY_TIMEFREEZE_LEVEL = "timefreeze_level";
    
    // Constantes
    private static final int TOTAL_CARDS = 35;
    private static final int ACTIVE_DECK_SIZE = 15;
    private static final int CARDS_PER_DECK = 7;
    
    // Singleton
    private static SaveManager instance;
    private Preferences prefs;
    
    // Cache de cartas activas
    private Array<Integer> activeCards;
    
    private SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        activeCards = new Array<>();
        
        if (!prefs.contains(KEY_NEKOINS)) {
            initializeDefaults();
        } else {
            loadActiveCards();
        }
    }
    
    private void initializeDefaults() {
        Gdx.app.log(TAG, "Inicializando progreso por defecto...");
        
        prefs.putInteger(KEY_NEKOINS, 0);
        
        // Solo desbloquear Deck Base (7 cartas: cardId 0-6)
        for (int i = 0; i < CARDS_PER_DECK; i++) {
            prefs.putBoolean(KEY_CARD_UNLOCKED + i, true);
        }
        
        // Configurar cartas activas (solo las 7 del Deck Base, resto vacío)
        StringBuilder activeCardsStr = new StringBuilder();
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (i > 0) activeCardsStr.append(",");
            if (i < CARDS_PER_DECK) {
                activeCardsStr.append(i);
                activeCards.add(i);
            } else {
                activeCardsStr.append(-1);
                activeCards.add(-1);
            }
        }
        prefs.putString(KEY_ACTIVE_CARDS, activeCardsStr.toString());
        
        prefs.putInteger(KEY_HINT_LEVEL, 0);
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, 0);
        
        prefs.flush();
        Gdx.app.log(TAG, "Progreso inicial creado - Deck Base desbloqueado (7 cartas)");
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
        Gdx.app.log(TAG, "Nekoins: " + current + " -> " + (current + amount));
    }
    
    public boolean spendNekoins(int amount) {
        int current = getNekoins();
        if (current >= amount) {
            prefs.putInteger(KEY_NEKOINS, current - amount);
            prefs.flush();
            Gdx.app.log(TAG, "Gastados " + amount + " nekoins (quedan " + (current - amount) + ")");
            return true;
        }
        Gdx.app.log(TAG, "No hay suficientes nekoins");
        return false;
    }
    
    // ==================== CARTAS INDIVIDUALES ====================
    
    public boolean isCardUnlocked(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return false;
        return prefs.getBoolean(KEY_CARD_UNLOCKED + cardId, false);
    }
    
    public void unlockCard(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return;
        prefs.putBoolean(KEY_CARD_UNLOCKED + cardId, true);
        prefs.flush();
        Gdx.app.log(TAG, "Carta desbloqueada: " + cardId);
    }
    
    public Array<Integer> getUnlockedCards() {
        Array<Integer> unlocked = new Array<>();
        for (int i = 0; i < TOTAL_CARDS; i++) {
            if (isCardUnlocked(i)) {
                unlocked.add(i);
            }
        }
        return unlocked;
    }
    
    public int getUnlockedCardCount() {
        int count = 0;
        for (int i = 0; i < TOTAL_CARDS; i++) {
            if (isCardUnlocked(i)) count++;
        }
        return count;
    }
    
    // ==================== CARTAS ACTIVAS ====================
    
    public Array<Integer> getActiveCards() {
        return new Array<>(activeCards);
    }
    
    public boolean isCardActive(int cardId) {
        return activeCards.contains(cardId, false);
    }
    
    public boolean setActiveCardSlot(int slotIndex, int cardId) {
        if (slotIndex < 0 || slotIndex >= ACTIVE_DECK_SIZE) return false;
        
        if (cardId == -1) {
            if (slotIndex < activeCards.size) {
                activeCards.set(slotIndex, -1);
                saveActiveCards();
                return true;
            }
            return false;
        }
        
        if (!isCardUnlocked(cardId)) {
            Gdx.app.log(TAG, "Carta " + cardId + " no esta desbloqueada");
            return false;
        }
        
        if (isCardActive(cardId)) {
            Gdx.app.log(TAG, "Carta " + cardId + " ya esta activa");
            return false;
        }
        
        while (activeCards.size <= slotIndex) {
            activeCards.add(-1);
        }
        activeCards.set(slotIndex, cardId);
        saveActiveCards();
        
        Gdx.app.log(TAG, "Carta " + cardId + " colocada en slot " + slotIndex);
        return true;
    }
    
    public void swapActiveCards(int slot1, int slot2) {
        if (slot1 < 0 || slot1 >= activeCards.size) return;
        if (slot2 < 0 || slot2 >= activeCards.size) return;
        
        int temp = activeCards.get(slot1);
        activeCards.set(slot1, activeCards.get(slot2));
        activeCards.set(slot2, temp);
        saveActiveCards();
    }
    
    public int removeActiveCard(int cardId) {
        int index = activeCards.indexOf(cardId, false);
        if (index >= 0) {
            activeCards.set(index, -1);
            saveActiveCards();
            Gdx.app.log(TAG, "Carta " + cardId + " removida del slot " + index);
        }
        return index;
    }
    
    public int addActiveCard(int cardId) {
        if (!isCardUnlocked(cardId)) return -1;
        if (isCardActive(cardId)) return -1;
        
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (i >= activeCards.size || activeCards.get(i) == -1) {
                setActiveCardSlot(i, cardId);
                return i;
            }
        }
        
        return -1;
    }
    
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
    
    public static int getDeckFromCardId(int cardId) {
        return cardId / CARDS_PER_DECK;
    }
    
    public static int getCardIndexFromCardId(int cardId) {
        return cardId % CARDS_PER_DECK;
    }
    
    public static int getCardId(int deckIndex, int cardIndex) {
        return deckIndex * CARDS_PER_DECK + cardIndex;
    }
    
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
    
    // ==================== LEGACY ====================
    
    public int getCurrentDeck() {
        if (activeCards.size > 0 && activeCards.get(0) >= 0) {
            return getDeckFromCardId(activeCards.get(0));
        }
        return 0;
    }
    
    public void setCurrentDeck(int deckIndex) {
        // Legacy - no hacer nada
    }
    
    public boolean isDeckUnlocked(int deckIndex) {
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
        Gdx.app.log(TAG, "Progreso borrado");
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
