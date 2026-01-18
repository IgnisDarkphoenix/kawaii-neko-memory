package com.darkphoenixteam.kawaiinekomemory.models;

import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Modelo de datos para un nivel
 * Incluye configuración de tiempo, grids y shuffle basado en el rango del nivel
 * 
 * @author DarkphoenixTeam
 */
public class LevelData {
    
    /**
     * Enum de dificultades con configuración de grid
     */
    public enum Difficulty {
        EASY(0, "Fácil", 3, 4, Constants.TIME_BASE_EASY, Constants.TIME_DECAY_EASY),
        NORMAL(1, "Normal", 4, 4, Constants.TIME_BASE_NORMAL, Constants.TIME_DECAY_NORMAL),
        ADVANCED(2, "Avanzado", 4, 5, Constants.TIME_BASE_ADVANCED, Constants.TIME_DECAY_ADVANCED),
        HARD(3, "Difícil", 5, 6, Constants.TIME_BASE_HARD, Constants.TIME_DECAY_HARD);
        
        public final int index;
        public final String name;
        public final int cols;
        public final int rows;
        public final float baseTime;
        public final float timeDecay;
        
        Difficulty(int index, String name, int cols, int rows, float baseTime, float timeDecay) {
            this.index = index;
            this.name = name;
            this.cols = cols;
            this.rows = rows;
            this.baseTime = baseTime;
            this.timeDecay = timeDecay;
        }
        
        public int getTotalCards() {
            return cols * rows;
        }
        
        public int getPairs() {
            return getTotalCards() / 2;
        }
        
        public static Difficulty fromGlobalId(int globalLevelId) {
            if (globalLevelId < 50) return EASY;
            if (globalLevelId < 100) return NORMAL;
            if (globalLevelId < 150) return ADVANCED;
            return HARD;
        }
    }
    
    /**
     * Rangos de nivel con diferentes mecánicas
     */
    public enum LevelRange {
        SPEED,      // 1-15: Single grid, no shuffle
        ENDURANCE,  // 16-30: Double grid, no shuffle
        SHUFFLE,    // 31-40: Single grid, shuffle cada 3 pares
        MASTERY     // 41-50: Double grid, shuffle cada 3 pares
    }
    
    private int globalId;        // 0-199 (único en todo el juego)
    private int localId;         // 1-50 (dentro de la dificultad)
    private Difficulty difficulty;
    private LevelRange range;
    private boolean unlocked;
    private boolean completed;
    
    // Configuración calculada
    private float timeLimit;
    private int gridCount;
    private boolean shuffleEnabled;
    
    /**
     * Constructor
     * @param globalId ID único del nivel (0-199)
     */
    public LevelData(int globalId) {
        this.globalId = globalId;
        this.difficulty = Difficulty.fromGlobalId(globalId);
        this.localId = (globalId % 50) + 1; // 1-50
        
        // Calcular rango y configuración
        calculateRange();
        calculateTimeLimit();
        calculateGridConfig();
    }
    
    /**
     * Determina el rango del nivel basado en localId
     */
    private void calculateRange() {
        if (localId <= Constants.RANGE_SPEED_END) {
            range = LevelRange.SPEED;
        } else if (localId <= Constants.RANGE_ENDURANCE_END) {
            range = LevelRange.ENDURANCE;
        } else if (localId <= Constants.RANGE_SHUFFLE_END) {
            range = LevelRange.SHUFFLE;
        } else {
            range = LevelRange.MASTERY;
        }
    }
    
    /**
     * Calcula el tiempo límite basado en dificultad y rango
     */
    private void calculateTimeLimit() {
        float base = difficulty.baseTime;
        float decay = difficulty.timeDecay;
        
        switch (range) {
            case SPEED:
                // Time = Base - ((L-1) * Decay)
                timeLimit = base - ((localId - 1) * decay);
                break;
                
            case ENDURANCE:
                // Time = (Base * 1.8) - ((L-16) * Decay)
                timeLimit = (base * Constants.TIME_MULT_ENDURANCE) - 
                           ((localId - Constants.RANGE_ENDURANCE_START) * decay);
                break;
                
            case SHUFFLE:
                // Time = Base * 1.25
                timeLimit = base * Constants.TIME_MULT_SHUFFLE;
                break;
                
            case MASTERY:
                // Time = Base * 2.3
                timeLimit = base * Constants.TIME_MULT_MASTERY;
                break;
        }
        
        // Mínimo 15 segundos para evitar niveles imposibles
        timeLimit = Math.max(15f, timeLimit);
    }
    
    /**
     * Configura número de grids y shuffle
     */
    private void calculateGridConfig() {
        switch (range) {
            case SPEED:
                gridCount = 1;
                shuffleEnabled = false;
                break;
                
            case ENDURANCE:
                gridCount = 2;
                shuffleEnabled = false;
                break;
                
            case SHUFFLE:
                gridCount = 1;
                shuffleEnabled = true;
                break;
                
            case MASTERY:
                gridCount = 2;
                shuffleEnabled = true;
                break;
        }
    }
    
    // ==================== GETTERS PRINCIPALES ====================
    
    public int getGlobalId() { return globalId; }
    public int getLocalId() { return localId; }
    public Difficulty getDifficulty() { return difficulty; }
    public LevelRange getRange() { return range; }
    
    public float getTimeLimit() { return timeLimit; }
    public int getGridCount() { return gridCount; }
    public boolean isShuffleEnabled() { return shuffleEnabled; }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    // ==================== CÁLCULOS DE RECOMPENSA ====================
    
    /**
     * Calcula las estrellas basado en tiempo restante
     * @param timeRemaining Segundos restantes
     * @return 1, 2 o 3 estrellas
     */
    public int calculateStars(float timeRemaining) {
        float ratio = timeRemaining / timeLimit;
        
        if (ratio > Constants.STARS_3_THRESHOLD) return 3;
        if (ratio > Constants.STARS_2_THRESHOLD) return 2;
        return 1;
    }
    
    /**
     * Calcula la recompensa de nekoins por el nivel
     * @param stars Estrellas obtenidas (1-3)
     * @param isFirstClear true si es la primera vez que se completa
     * @return Nekoins de recompensa del nivel (sin deck bonus)
     */
    public int calculateLevelReward(int stars, boolean isFirstClear) {
        int[] rewards = Constants.REWARDS_BY_DIFFICULTY[difficulty.index];
        int baseReward = rewards[0];
        int starBonus = rewards[1];
        int firstClearBonus = rewards[2];
        
        // Base + bonus por estrellas extra (1 estrella = base, 2 = base+bonus, 3 = base+2*bonus)
        int total = baseReward + ((stars - 1) * starBonus);
        
        // Bonus de primera vez
        if (isFirstClear) {
            total += firstClearBonus;
        }
        
        return total;
    }
    
    /**
     * Obtiene el número de pares necesarios para este nivel
     */
    public int getPairsRequired() {
        return difficulty.getPairs() * gridCount;
    }
    
    /**
     * Obtiene el número de cartas únicas necesarias
     */
    public int getUniqueCardsRequired() {
        return difficulty.getPairs();
    }
    
    // ==================== UI ====================
    
    /**
     * Obtiene el color asociado a la dificultad (para UI)
     */
    public int getColorInt() {
        switch (difficulty) {
            case EASY: return 0x90EE90;      // Verde claro
            case NORMAL: return 0xFFD700;    // Amarillo dorado
            case ADVANCED: return 0xFF8C00;  // Naranja
            case HARD: return 0xFF4500;      // Rojo-naranja
            default: return 0xFFFFFF;
        }
    }
    
    /**
     * Descripción del rango para debug/UI
     */
    public String getRangeDescription() {
        switch (range) {
            case SPEED: return "Velocidad";
            case ENDURANCE: return "Resistencia (" + gridCount + " grids)";
            case SHUFFLE: return "Shuffle";
            case MASTERY: return "Maestría (" + gridCount + " grids + shuffle)";
            default: return "???";
        }
    }
    
    @Override
    public String toString() {
        return String.format("Level %d (%s L%d) - %s | Time: %.0fs | Grids: %d | Shuffle: %s",
            globalId,
            difficulty.name,
            localId,
            range.name(),
            timeLimit,
            gridCount,
            shuffleEnabled ? "ON" : "OFF"
        );
    }
}