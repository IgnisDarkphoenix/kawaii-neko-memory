package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.Achievement;

/**
 * Gestor de guardado con sistema de logros, estadísticas y Time Attack
 * 
 * @author DarkphoenixTeam
 * @version 3.0 - Time Attack + Gacha dinámico
 */
public class SaveManager {
    
    private static final String TAG = "SaveManager";
    private static final String PREFS_NAME = "KawaiiNekoSave";
    
    // === KEYS PRINCIPALES ===
    private static final String KEY_NEKOINS = "nekoins";
    private static final String KEY_LEVEL_COMPLETED = "level_completed_";
    private static final String KEY_LEVEL_STARS = "level_stars_";
    private static final String KEY_CARD_UNLOCKED = "card_unlocked_";
    private static final String KEY_ACTIVE_CARDS = "active_cards";
    private static final String KEY_HINT_USES = "hint_uses";
    private static final String KEY_TIMEFREEZE_USES = "timefreeze_uses";
    private static final String KEY_SAVE_VERSION = "save_version_v5";
    
    // === KEYS DE LOGROS ===
    private static final String KEY_ACHIEVEMENT = "achievement_";
    
    // === KEYS DE ESTADÍSTICAS ===
    private static final String KEY_STAT_TOTAL_PAIRS = "stat_total_pairs";
    private static final String KEY_STAT_TOTAL_WINS = "stat_total_wins";
    private static final String KEY_STAT_TOTAL_LOSSES = "stat_total_losses";
    private static final String KEY_STAT_TOTAL_EARNED = "stat_total_earned";
    private static final String KEY_STAT_TOTAL_SPENT = "stat_total_spent";
    private static final String KEY_STAT_POWERS_USED = "stat_powers_used";
    private static final String KEY_STAT_BEST_COMBO = "stat_best_combo";
    private static final String KEY_STAT_PURCHASES = "stat_purchases";
    
    // === KEYS TIME ATTACK ===
    private static final String KEY_TIME_ATTACK_UPGRADES = "time_attack_upgrades";
    private static final String KEY_TIME_ATTACK_BEST_PAIRS = "time_attack_best_pairs";
    private static final String KEY_TIME_ATTACK_TOTAL_PAIRS = "time_attack_total_pairs";
    private static final String KEY_TIME_ATTACK_GAMES_PLAYED = "time_attack_games_played";
    
    // === KEYS GACHA ===
    private static final String KEY_GACHA_PULLS = "gacha_total_pulls";
    
    private static final int CURRENT_VERSION = 5;
    private static final int TOTAL_CARDS = 35;
    private static final int ACTIVE_DECK_SIZE = 15;
    private static final int CARDS_PER_DECK = 7;
    private static final int LEVELS_PER_DIFFICULTY = 50;
    
    private static SaveManager instance;
    private Preferences prefs;
    private Array<Integer> activeCards;
    
    private Array<Achievement> newlyUnlocked;
    
    private SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        activeCards = new Array<>();
        newlyUnlocked = new Array<>();
        
        int version = prefs.getInteger(KEY_SAVE_VERSION, 0);
        Gdx.app.log(TAG, "Save version: " + version + " (current: " + CURRENT_VERSION + ")");
        
        if (version < CURRENT_VERSION) {
            Gdx.app.log(TAG, "Migrando datos a nueva versión...");
            migrateData(version);
        } else {
            loadActiveCards();
        }
        
        debugPrintStats();
    }
    
    private void migrateData(int oldVersion) {
        if (oldVersion >= 4) {
            // Migración desde v4: solo agregar nuevas keys
            if (!prefs.contains(KEY_TIME_ATTACK_UPGRADES)) {
                prefs.putInteger(KEY_TIME_ATTACK_UPGRADES, 0);
            }
            if (!prefs.contains(KEY_TIME_ATTACK_BEST_PAIRS)) {
                prefs.putInteger(KEY_TIME_ATTACK_BEST_PAIRS, 0);
            }
            if (!prefs.contains(KEY_TIME_ATTACK_TOTAL_PAIRS)) {
                prefs.putInteger(KEY_TIME_ATTACK_TOTAL_PAIRS, 0);
            }
            if (!prefs.contains(KEY_TIME_ATTACK_GAMES_PLAYED)) {
                prefs.putInteger(KEY_TIME_ATTACK_GAMES_PLAYED, 0);
            }
            if (!prefs.contains(KEY_GACHA_PULLS)) {
                prefs.putInteger(KEY_GACHA_PULLS, 0);
            }
            prefs.putInteger(KEY_SAVE_VERSION, CURRENT_VERSION);
            prefs.flush();
            loadActiveCards();
        } else if (oldVersion >= 3) {
            prefs.putInteger(KEY_SAVE_VERSION, CURRENT_VERSION);
            prefs.flush();
            loadActiveCards();
        } else {
            resetAndInitialize();
        }
    }
    
    private void resetAndInitialize() {
        prefs.clear();
        prefs.flush();
        activeCards.clear();
        
        prefs.putInteger(KEY_SAVE_VERSION, CURRENT_VERSION);
        prefs.putInteger(KEY_NEKOINS, 100);
        
        for (int i = 0; i < CARDS_PER_DECK; i++) {
            prefs.putBoolean(KEY_CARD_UNLOCKED + i, true);
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
        
        // Estadísticas
        prefs.putInteger(KEY_STAT_TOTAL_PAIRS, 0);
        prefs.putInteger(KEY_STAT_TOTAL_WINS, 0);
        prefs.putInteger(KEY_STAT_TOTAL_LOSSES, 0);
        prefs.putInteger(KEY_STAT_TOTAL_EARNED, 100);
        prefs.putInteger(KEY_STAT_TOTAL_SPENT, 0);
        prefs.putInteger(KEY_STAT_POWERS_USED, 0);
        prefs.putInteger(KEY_STAT_BEST_COMBO, 0);
        prefs.putInteger(KEY_STAT_PURCHASES, 0);
        
        // Time Attack
        prefs.putInteger(KEY_TIME_ATTACK_UPGRADES, 0);
        prefs.putInteger(KEY_TIME_ATTACK_BEST_PAIRS, 0);
        prefs.putInteger(KEY_TIME_ATTACK_TOTAL_PAIRS, 0);
        prefs.putInteger(KEY_TIME_ATTACK_GAMES_PLAYED, 0);
        
        // Gacha
        prefs.putInteger(KEY_GACHA_PULLS, 0);
        
        prefs.flush();
        Gdx.app.log(TAG, "Inicialización completa");
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
        
        int totalEarned = prefs.getInteger(KEY_STAT_TOTAL_EARNED, 0);
        prefs.putInteger(KEY_STAT_TOTAL_EARNED, totalEarned + amount);
        
        prefs.flush();
        
        checkAchievement(Achievement.RICH_NEKO);
    }
    
    public boolean spendNekoins(int amount) {
        int current = getNekoins();
        if (current >= amount) {
            prefs.putInteger(KEY_NEKOINS, current - amount);
            
            int totalSpent = prefs.getInteger(KEY_STAT_TOTAL_SPENT, 0);
            prefs.putInteger(KEY_STAT_TOTAL_SPENT, totalSpent + amount);
            
            int purchases = prefs.getInteger(KEY_STAT_PURCHASES, 0);
            prefs.putInteger(KEY_STAT_PURCHASES, purchases + 1);
            
            prefs.flush();
            
            if (purchases == 0) {
                unlockAchievement(Achievement.FIRST_SHOP);
            }
            checkAchievement(Achievement.BIG_SPENDER);
            
            return true;
        }
        return false;
    }
    
    // ==================== TIME ATTACK ====================
    
    public int getTimeAttackUpgrades() {
        return prefs.getInteger(KEY_TIME_ATTACK_UPGRADES, 0);
    }
    
    public float getTimeAttackTime() {
        int upgrades = getTimeAttackUpgrades();
        return Constants.TIME_ATTACK_BASE_TIME + (upgrades * Constants.TIME_ATTACK_UPGRADE_AMOUNT);
    }
    
    public int getTimeAttackUpgradeCost() {
        int upgrades = getTimeAttackUpgrades();
        if (upgrades >= Constants.TIME_ATTACK_MAX_UPGRADES) {
            return -1; // Ya está al máximo
        }
        
        int cost = Constants.TIME_ATTACK_UPGRADE_BASE_COST;
        for (int i = 0; i < upgrades; i++) {
            if (i % 2 == 0) {
                cost += Constants.TIME_ATTACK_UPGRADE_INCREMENT_ODD;
            } else {
                cost += Constants.TIME_ATTACK_UPGRADE_INCREMENT_EVEN;
            }
        }
        return cost;
    }
    
    public boolean purchaseTimeAttackUpgrade() {
        int cost = getTimeAttackUpgradeCost();
        if (cost < 0) return false; // Ya está al máximo
        
        if (spendNekoins(cost)) {
            int upgrades = getTimeAttackUpgrades();
            prefs.putInteger(KEY_TIME_ATTACK_UPGRADES, upgrades + 1);
            prefs.flush();
            
            float newTime = getTimeAttackTime();
            Gdx.app.log(TAG, "Time Attack upgrade! Nuevo tiempo: " + newTime + "s");
            return true;
        }
        return false;
    }
    
    public int getTimeAttackBestPairs() {
        return prefs.getInteger(KEY_TIME_ATTACK_BEST_PAIRS, 0);
    }
    
    public boolean updateTimeAttackBestPairs(int pairs) {
        int current = getTimeAttackBestPairs();
        if (pairs > current) {
            prefs.putInteger(KEY_TIME_ATTACK_BEST_PAIRS, pairs);
            prefs.flush();
            Gdx.app.log(TAG, "Nuevo récord Time Attack: " + pairs + " pares");
            return true;
        }
        return false;
    }
    
    public void addTimeAttackPairs(int pairs) {
        int total = prefs.getInteger(KEY_TIME_ATTACK_TOTAL_PAIRS, 0);
        prefs.putInteger(KEY_TIME_ATTACK_TOTAL_PAIRS, total + pairs);
        prefs.flush();
    }
    
    public int getTimeAttackTotalPairs() {
        return prefs.getInteger(KEY_TIME_ATTACK_TOTAL_PAIRS, 0);
    }
    
    public void incrementTimeAttackGamesPlayed() {
        int games = prefs.getInteger(KEY_TIME_ATTACK_GAMES_PLAYED, 0);
        prefs.putInteger(KEY_TIME_ATTACK_GAMES_PLAYED, games + 1);
        prefs.flush();
    }
    
    public int getTimeAttackGamesPlayed() {
        return prefs.getInteger(KEY_TIME_ATTACK_GAMES_PLAYED, 0);
    }
    
    // ==================== GACHA ====================
    
    public int getGachaPulls() {
        return prefs.getInteger(KEY_GACHA_PULLS, 0);
    }
    
    public int getGachaCost() {
        int pulls = getGachaPulls();
        int cost = Constants.GACHA_BASE_COST;
        
        for (int i = 0; i < pulls; i++) {
            if (i % 2 == 0) {
                cost += Constants.GACHA_INCREMENT_ODD;
            } else {
                cost += Constants.GACHA_INCREMENT_EVEN;
            }
        }
        return cost;
    }
    
    public void incrementGachaPulls() {
        int pulls = getGachaPulls();
        prefs.putInteger(KEY_GACHA_PULLS, pulls + 1);
        prefs.flush();
    }
    
    // ==================== POWER PRICES ====================
    
    public int getPowerPrice(int currentAmount) {
        if (currentAmount >= Constants.MAX_POWER_STOCK) return -1;
        if (currentAmount < 0) currentAmount = 0;
        if (currentAmount >= Constants.POWER_PRICES.length) {
            return Constants.POWER_PRICES[Constants.POWER_PRICES.length - 1];
        }
        return Constants.POWER_PRICES[currentAmount];
    }
    
    public int getHintPrice() {
        return getPowerPrice(getHintUses());
    }
    
    public int getTimeFreezePrice() {
        return getPowerPrice(getTimeFreezeUses());
    }
    
    // ==================== CARDS ====================
    
    public boolean isCardUnlocked(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return false;
        return prefs.getBoolean(KEY_CARD_UNLOCKED + cardId, false);
    }
    
    public void unlockCard(int cardId) {
        if (cardId < 0 || cardId >= TOTAL_CARDS) return;
        
        prefs.putBoolean(KEY_CARD_UNLOCKED + cardId, true);
        prefs.flush();
        
        checkAchievement(Achievement.GALLERY_UNLOCK);
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
        
        if (!isCardUnlocked(cardId) || isCardActive(cardId)) return false;
        
        activeCards.set(slot, cardId);
        saveActiveCards();
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
        if (!isCardUnlocked(cardId) || isCardActive(cardId)) return -1;
        
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
    
    // ==================== CARD ID UTILS ====================
    
    public static int getDeckFromCardId(int cardId) {
        return cardId / CARDS_PER_DECK;
    }
    
    public static int getCardIndexFromCardId(int cardId) {
        return cardId % CARDS_PER_DECK;
    }
    
    public int getCardNekoinValue(int cardId) {
        int[] values = {1, 2, 3, 5, 7};
        int deck = getDeckFromCardId(cardId);
        return (deck >= 0 && deck < values.length) ? values[deck] : 1;
    }
    
    // ==================== LEVELS ====================
    
    public void setLevelCompleted(int levelId, int stars) {
        boolean wasCompleted = isLevelCompleted(levelId);
        prefs.putBoolean(KEY_LEVEL_COMPLETED + levelId, true);
        
        int currentStars = getLevelStars(levelId);
        if (stars > currentStars) {
            prefs.putInteger(KEY_LEVEL_STARS + levelId, stars);
        }
        
        if (!wasCompleted) {
            int wins = prefs.getInteger(KEY_STAT_TOTAL_WINS, 0);
            prefs.putInteger(KEY_STAT_TOTAL_WINS, wins + 1);
            
            if (wins == 0) {
                unlockAchievement(Achievement.FIRST_WIN);
            }
        }
        
        prefs.flush();
        
        if (stars == 3) {
            checkAchievement(Achievement.FIRST_3_STAR);
        }
        
        checkDifficultyCompletionAchievements();
        checkPerfectionAchievements();
    }
    
    public void setLevelCompleted(int levelId) {
        setLevelCompleted(levelId, 1);
    }
    
    public boolean isLevelCompleted(int levelId) {
        return prefs.getBoolean(KEY_LEVEL_COMPLETED + levelId, false);
    }
    
    public int getLevelStars(int levelId) {
        return prefs.getInteger(KEY_LEVEL_STARS + levelId, 0);
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
    
    // ==================== POWER USES ====================
    
    public int getHintUses() {
        return prefs.getInteger(KEY_HINT_USES, 0);
    }
    
    public void addHintUses(int amount) {
        int current = getHintUses();
        prefs.putInteger(KEY_HINT_USES, current + amount);
        prefs.flush();
    }
    
    public void decrementHintUses() {
        int current = getHintUses();
        if (current > 0) {
            prefs.putInteger(KEY_HINT_USES, current - 1);
            incrementPowersUsed();
            prefs.flush();
        }
    }
    
    public int getTimeFreezeUses() {
        return prefs.getInteger(KEY_TIMEFREEZE_USES, 0);
    }
    
    public void addTimeFreezeUses(int amount) {
        int current = getTimeFreezeUses();
        prefs.putInteger(KEY_TIMEFREEZE_USES, current + amount);
        prefs.flush();
    }
    
    public void decrementTimeFreezeUses() {
        int current = getTimeFreezeUses();
        if (current > 0) {
            prefs.putInteger(KEY_TIMEFREEZE_USES, current - 1);
            incrementPowersUsed();
            prefs.flush();
        }
    }
    
    private void incrementPowersUsed() {
        int total = prefs.getInteger(KEY_STAT_POWERS_USED, 0);
        if (total == 0) {
            unlockAchievement(Achievement.FIRST_POWER);
        }
        prefs.putInteger(KEY_STAT_POWERS_USED, total + 1);
    }
    
    // ==================== ESTADÍSTICAS ====================
    
    public void addPairsFound(int pairs) {
        int total = prefs.getInteger(KEY_STAT_TOTAL_PAIRS, 0);
        prefs.putInteger(KEY_STAT_TOTAL_PAIRS, total + pairs);
        prefs.flush();
        
        checkAchievement(Achievement.PERSISTENT);
    }
    
    public int getTotalPairsFound() {
        return prefs.getInteger(KEY_STAT_TOTAL_PAIRS, 0);
    }
    
    public void updateBestCombo(int combo) {
        int best = prefs.getInteger(KEY_STAT_BEST_COMBO, 0);
        if (combo > best) {
            prefs.putInteger(KEY_STAT_BEST_COMBO, combo);
            prefs.flush();
        }
        
        if (combo >= 5) {
            unlockAchievement(Achievement.COMBO_MASTER);
        }
    }
    
    public int getBestCombo() {
        return prefs.getInteger(KEY_STAT_BEST_COMBO, 0);
    }
    
    public void recordLoss() {
        int losses = prefs.getInteger(KEY_STAT_TOTAL_LOSSES, 0);
        if (losses == 0) {
            unlockAchievement(Achievement.FIRST_FAIL);
        }
        prefs.putInteger(KEY_STAT_TOTAL_LOSSES, losses + 1);
        prefs.flush();
    }
    
    public int getTotalWins() {
        return prefs.getInteger(KEY_STAT_TOTAL_WINS, 0);
    }
    
    public int getTotalLosses() {
        return prefs.getInteger(KEY_STAT_TOTAL_LOSSES, 0);
    }
    
    public int getTotalNekoinsEarned() {
        return prefs.getInteger(KEY_STAT_TOTAL_EARNED, 0);
    }
    
    public int getTotalNekoinsSpent() {
        return prefs.getInteger(KEY_STAT_TOTAL_SPENT, 0);
    }
    
    public int getTotalPurchases() {
        return prefs.getInteger(KEY_STAT_PURCHASES, 0);
    }
    
    public int getTotalPowersUsed() {
        return prefs.getInteger(KEY_STAT_POWERS_USED, 0);
    }
    
    // ==================== LOGROS ====================
    
    public boolean isAchievementUnlocked(Achievement achievement) {
        return prefs.getBoolean(KEY_ACHIEVEMENT + achievement.getIndex(), false);
    }
    
    public void unlockAchievement(Achievement achievement) {
        if (isAchievementUnlocked(achievement)) return;
        
        prefs.putBoolean(KEY_ACHIEVEMENT + achievement.getIndex(), true);
        prefs.flush();
        
        int current = getNekoins();
        prefs.putInteger(KEY_NEKOINS, current + achievement.reward);
        prefs.flush();
        
        newlyUnlocked.add(achievement);
        
        Gdx.app.log(TAG, "🏆 LOGRO DESBLOQUEADO: " + achievement.name + " (+" + achievement.reward + " Nekoins)");
    }
    
    public void checkAchievement(Achievement achievement) {
        if (isAchievementUnlocked(achievement)) return;
        
        boolean shouldUnlock = false;
        
        switch (achievement) {
            case RICH_NEKO:
                shouldUnlock = getTotalNekoinsEarned() >= 1000;
                break;
            case BIG_SPENDER:
                shouldUnlock = getTotalNekoinsSpent() >= 2000;
                break;
            case PERSISTENT:
                shouldUnlock = getTotalPairsFound() >= 1000;
                break;
            case GALLERY_UNLOCK:
                shouldUnlock = getUnlockedCardCount() >= TOTAL_CARDS;
                break;
            default:
                break;
        }
        
        if (shouldUnlock) {
            unlockAchievement(achievement);
        }
    }
    
    private void checkDifficultyCompletionAchievements() {
        // Easy (0-49)
        if (!isAchievementUnlocked(Achievement.EASY_COMPLETION)) {
            boolean allComplete = true;
            for (int i = 0; i < LEVELS_PER_DIFFICULTY; i++) {
                if (!isLevelCompleted(i)) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) unlockAchievement(Achievement.EASY_COMPLETION);
        }
        
        // Normal (50-99)
        if (!isAchievementUnlocked(Achievement.NORMAL_COMPLETION)) {
            boolean allComplete = true;
            for (int i = 50; i < 100; i++) {
                if (!isLevelCompleted(i)) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) unlockAchievement(Achievement.NORMAL_COMPLETION);
        }
        
        // Advanced (100-149)
        if (!isAchievementUnlocked(Achievement.ADVANCED_COMPLETION)) {
            boolean allComplete = true;
            for (int i = 100; i < 150; i++) {
                if (!isLevelCompleted(i)) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) unlockAchievement(Achievement.ADVANCED_COMPLETION);
        }
        
        // Hard (150-199)
        if (!isAchievementUnlocked(Achievement.HARD_COMPLETION)) {
            boolean allComplete = true;
            for (int i = 150; i < 200; i++) {
                if (!isLevelCompleted(i)) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) unlockAchievement(Achievement.HARD_COMPLETION);
        }
    }
    
    private void checkPerfectionAchievements() {
        // Easy 3 estrellas
        if (!isAchievementUnlocked(Achievement.ALL_STARS_EASY)) {
            boolean allPerfect = true;
            for (int i = 0; i < LEVELS_PER_DIFFICULTY; i++) {
                if (getLevelStars(i) < 3) {
                    allPerfect = false;
                    break;
                }
            }
            if (allPerfect) unlockAchievement(Achievement.ALL_STARS_EASY);
        }
        
        // Normal 3 estrellas
        if (!isAchievementUnlocked(Achievement.ALL_STARS_NORMAL)) {
            boolean allPerfect = true;
            for (int i = 50; i < 100; i++) {
                if (getLevelStars(i) < 3) {
                    allPerfect = false;
                    break;
                }
            }
            if (allPerfect) unlockAchievement(Achievement.ALL_STARS_NORMAL);
        }
        
        // Advanced 3 estrellas
        if (!isAchievementUnlocked(Achievement.ALL_STARS_ADVANCED)) {
            boolean allPerfect = true;
            for (int i = 100; i < 150; i++) {
                if (getLevelStars(i) < 3) {
                    allPerfect = false;
                    break;
                }
            }
            if (allPerfect) unlockAchievement(Achievement.ALL_STARS_ADVANCED);
        }
        
        // Hard 3 estrellas
        if (!isAchievementUnlocked(Achievement.ALL_STARS_HARD)) {
            boolean allPerfect = true;
            for (int i = 150; i < 200; i++) {
                if (getLevelStars(i) < 3) {
                    allPerfect = false;
                    break;
                }
            }
            if (allPerfect) unlockAchievement(Achievement.ALL_STARS_HARD);
        }
    }
    
    public int getUnlockedAchievementCount() {
        int count = 0;
        for (Achievement a : Achievement.values()) {
            if (isAchievementUnlocked(a)) count++;
        }
        return count;
    }
    
    public Array<Achievement> popNewlyUnlockedAchievements() {
        Array<Achievement> result = new Array<>(newlyUnlocked);
        newlyUnlocked.clear();
        return result;
    }
    
    public boolean hasNewAchievements() {
        return newlyUnlocked.size > 0;
    }
    
    // ==================== UTILIDADES ====================
    
    public void resetAll() {
        instance = null;
        prefs.clear();
        prefs.flush();
        Gdx.app.log(TAG, "RESET COMPLETO");
    }
    
    private void debugPrintStats() {
        Gdx.app.log(TAG, "=== ESTADÍSTICAS ===");
        Gdx.app.log(TAG, "Nekoins: " + getNekoins());
        Gdx.app.log(TAG, "Victorias: " + getTotalWins());
        Gdx.app.log(TAG, "Derrotas: " + getTotalLosses());
        Gdx.app.log(TAG, "Pares totales: " + getTotalPairsFound());
        Gdx.app.log(TAG, "Mejor combo: " + getBestCombo());
        Gdx.app.log(TAG, "Logros: " + getUnlockedAchievementCount() + "/" + Achievement.count());
        Gdx.app.log(TAG, "Time Attack - Mejor: " + getTimeAttackBestPairs() + " pares");
        Gdx.app.log(TAG, "Time Attack - Tiempo: " + getTimeAttackTime() + "s");
    }
    
    public String getStats() {
        return "Nekoins:" + getNekoins() + 
               " Wins:" + getTotalWins() + 
               " Cartas:" + getUnlockedCardCount() + "/35" +
               " Logros:" + getUnlockedAchievementCount() + "/" + Achievement.count() +
               " TA-Best:" + getTimeAttackBestPairs();
    }
}
