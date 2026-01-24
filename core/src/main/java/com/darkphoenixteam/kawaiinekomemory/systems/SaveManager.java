package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

public class SaveManager {
    
    private static final String TAG = "SaveManager";
    private static final String PREFS_NAME = "KawaiiNekoSave";
    
    private static final String KEY_NEKOINS = "nekoins";
    private static final String KEY_LEVEL_COMPLETED = "level_completed_";
    private static final String KEY_CARD_UNLOCKED = "card_unlocked_";
    private static final String KEY_ACTIVE_CARDS = "active_cards";
    private static final String KEY_HINT_LEVEL = "hint_level";
    private static final String KEY_TIMEFREEZE_LEVEL = "timefreeze_level";
    private static final String KEY_SAVE_VERSION = "save_version_v3";
    
    private static final int CURRENT_VERSION = 3;
    private static final int TOTAL_CARDS = 35;
    private static final int ACTIVE_DECK_SIZE = 15;
    private static final int CARDS_PER_DECK = 7;
    
    private static SaveManager instance;
    private Preferences prefs;
    private Array<Integer> activeCards;
    
    private SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        activeCards = new Array<>();
        
        int version = prefs.getInteger(KEY_SAVE_VERSION, 0);
        Gdx.app.log(TAG, "Save version: " + version + " (current: " + CURRENT_VERSION + ")");
        
        if (version < CURRENT_VERSION) {
            Gdx.app.log(TAG, "Reseteando datos para nueva version...");
            resetAndInitialize();
        } else {
            loadActiveCards();
            debugPrintUnlockedCards();
        }
    }
    
    private void resetAndInitialize() {
        prefs.clear();
        prefs.flush();
        activeCards.clear();
        
        prefs.putInteger(KEY_SAVE_VERSION, CURRENT_VERSION);
        prefs.putInteger(KEY_NEKOINS, 100);
        
        Gdx.app.log(TAG, "Desbloqueando solo Deck Base (cartas 0-6)...");
        for (int i = 0; i < CARDS_PER_DECK; i++) {
            String key = KEY_CARD_UNLOCKED + i;
            prefs.putBoolean(key, true);
            Gdx.app.log(TAG, "  -> " + key + " = true");
        }
        
        for (int i = CARDS_PER_DECK; i < TOTAL_CARDS; i++) {
            String key = KEY_CARD_UNLOCKED + i;
            prefs.putBoolean(key, false);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (i > 0) sb.append(",");
            if (i < CARDS_PER_DECK) {
                sb.append(i);
                activeCards.add(i);
            } else {
                sb.append(-1);
                activeCards.add(-1);
            }
        }
        prefs.putString(KEY_ACTIVE_CARDS, sb.toString());
        
        prefs.putInteger(KEY_HINT_LEVEL, 0);
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, 0);
        
        prefs.flush();
        
        Gdx.app.log(TAG, "Inicializacion completa");
        debugPrintUnlockedCards();
    }
    
    private void debugPrintUnlockedCards() {
        StringBuilder unlocked = new StringBuilder("Cartas desbloqueadas: ");
        int count = 0;
        for (int i = 0; i < TOTAL_CARDS; i++) {
            if (prefs.getBoolean(KEY_CARD_UNLOCKED + i, false)) {
                unlocked.append(i).append(" ");
                count++;
            }
        }
        Gdx.app.log(TAG, unlocked.toString() + "(" + count + " total)");
    }
    
    private void loadActiveCards() {
        activeCards.clear();
        String str = prefs.getString(KEY_ACTIVE_CARDS, "");
        
        if (!str.isEmpty()) {
            String[] parts = str.split(",");
            for (String part : parts) {
                try {
                    activeCards.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    activeCards.add(-1);
                }
            }
        }
        
        while (activeCards.size < ACTIVE_DECK_SIZE) {
            activeCards.add(-1);
        }
        
        Gdx.app.log(TAG, "Cartas activas cargadas: " + getActiveCardCount() + "/15");
    }
    
    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }
    
    public int getNekoins() {
        return prefs.getInteger(KEY_NEKOINS, 0);
    }
    
    public void addNekoins(int amount) {
        int current = getNekoins();
        prefs.putInteger(KEY_NEKOINS, current + amount);
        prefs.flush();
    }
    
    public boolean spendNekoins(int amount) {
        int current = getNekoins();
        if (current >= amount) {
            prefs.putInteger(KEY_NEKOINS, current - amount);
            prefs.flush();
            return true;
        }
        return false;
    }
    
    public boolean isCardUnlocked(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) {
            return false;
        }
        String key = KEY_CARD_UNLOCKED + cardId;
        boolean unlocked = prefs.getBoolean(key, false);
        return unlocked;
    }
    
    public void unlockCard(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) {
            Gdx.app.error(TAG, "unlockCard: cardId invalido: " + cardId);
            return;
        }
        
        String key = KEY_CARD_UNLOCKED + cardId;
        prefs.putBoolean(key, true);
        prefs.flush();
        
        int deck = getDeckFromCardId(cardId);
        int card = getCardIndexFromCardId(cardId);
        Gdx.app.log(TAG, "UNLOCK: cardId=" + cardId + " (deck=" + deck + ", card=" + card + ")");
        
        debugPrintUnlockedCards();
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
    
    public Array<Integer> getActiveCards() {
        return new Array<>(activeCards);
    }
    
    public boolean isCardActive(int cardId) {
        return activeCards.contains(cardId, false);
    }
    
    public boolean setActiveCardSlot(int slot, int cardId) {
        if (slot < 0 || slot >= ACTIVE_DECK_SIZE) return false;
        
        if (cardId == -1) {
            activeCards.set(slot, -1);
            saveActiveCards();
            return true;
        }
        
        if (!isCardUnlocked(cardId)) {
            Gdx.app.log(TAG, "setActiveCardSlot: carta " + cardId + " NO desbloqueada");
            return false;
        }
        
        if (isCardActive(cardId)) {
            Gdx.app.log(TAG, "setActiveCardSlot: carta " + cardId + " ya activa");
            return false;
        }
        
        activeCards.set(slot, cardId);
        saveActiveCards();
        Gdx.app.log(TAG, "setActiveCardSlot: carta " + cardId + " en slot " + slot);
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
        int idx = activeCards.indexOf(cardId, false);
        if (idx >= 0) {
            activeCards.set(idx, -1);
            saveActiveCards();
        }
        return idx;
    }
    
    public int addActiveCard(int cardId) {
        if (!isCardUnlocked(cardId)) return -1;
        if (isCardActive(cardId)) return -1;
        
        for (int i = 0; i < ACTIVE_DECK_SIZE; i++) {
            if (activeCards.get(i) == -1) {
                activeCards.set(i, cardId);
                saveActiveCards();
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
    
    public static int getDeckFromCardId(int cardId) {
        return cardId / CARDS_PER_DECK;
    }
    
    public static int getCardIndexFromCardId(int cardId) {
        return cardId % CARDS_PER_DECK;
    }
    
    public static int getCardId(int deck, int card) {
        return deck * CARDS_PER_DECK + card;
    }
    
    public int getCardNekoinValue(int cardId) {
        int[] values = {1, 2, 3, 5, 7};
        int deck = getDeckFromCardId(cardId);
        return (deck >= 0 && deck < values.length) ? values[deck] : 1;
    }
    
    public void setLevelCompleted(int levelId) {
        prefs.putBoolean(KEY_LEVEL_COMPLETED + levelId, true);
        prefs.flush();
    }
    
    public boolean isLevelCompleted(int levelId) {
        return prefs.getBoolean(KEY_LEVEL_COMPLETED + levelId, false);
    }
    
    public boolean isLevelUnlocked(int levelId) {
        if (levelId % 50 == 0) return true;
        return isLevelCompleted(levelId - 1);
    }
    
    public int getCurrentDeck() {
        if (activeCards.size > 0 && activeCards.get(0) >= 0) {
            return getDeckFromCardId(activeCards.get(0));
        }
        return 0;
    }
    
    public void setCurrentDeck(int deck) {
    }
    
    public boolean isDeckUnlocked(int deckIndex) {
        int start = deckIndex * CARDS_PER_DECK;
        for (int i = 0; i < CARDS_PER_DECK; i++) {
            if (isCardUnlocked(start + i)) return true;
        }
        return false;
    }
    
    public int getHintLevel() {
        return prefs.getInteger(KEY_HINT_LEVEL, 0);
    }
    
    public void upgradeHint() {
        prefs.putInteger(KEY_HINT_LEVEL, getHintLevel() + 1);
        prefs.flush();
    }
    
    public int getTimeFreezeLevel() {
        return prefs.getInteger(KEY_TIMEFREEZE_LEVEL, 0);
    }
    
    public void upgradeTimeFreeze() {
        prefs.putInteger(KEY_TIMEFREEZE_LEVEL, getTimeFreezeLevel() + 1);
        prefs.flush();
    }
    
    public void resetAll() {
        instance = null;
        prefs.clear();
        prefs.flush();
        Gdx.app.log(TAG, "RESET COMPLETO - reinicia la app");
    }
    
    public int getPlayerLevel() {
        int count = 0;
        for (int i = 0; i < 200; i++) {
            if (isLevelCompleted(i)) count++;
        }
        return count;
    }
    
    public String getStats() {
        return "Nekoins:" + getNekoins() + 
               " Niveles:" + getPlayerLevel() + 
               " Cartas:" + getUnlockedCardCount() + "/35";
    }
}