package com.darkphoenixteam.kawaiinekomemory.models;

/**
 * Modelo de datos para un nivel
 * Representa un nivel individual con su dificultad, estado de desbloqueo, etc.
 * 
 * @author DarkphoenixTeam
 */
public class LevelData {
    
    /**
     * Enum de dificultades con configuración de grid
     */
    public enum Difficulty {
        EASY(0, "Fácil", 3, 4),      // 3×4 = 12 cartas (6 pares)
        NORMAL(1, "Normal", 4, 4),   // 4×4 = 16 cartas (8 pares)
        ADVANCED(2, "Avanzado", 4, 5), // 4×5 = 20 cartas (10 pares)
        HARD(3, "Difícil", 5, 6);    // 5×6 = 30 cartas (15 pares)
        
        public final int index;
        public final String name;
        public final int cols;
        public final int rows;
        
        Difficulty(int index, String name, int cols, int rows) {
            this.index = index;
            this.name = name;
            this.cols = cols;
            this.rows = rows;
        }
        
        public int getTotalCards() {
            return cols * rows;
        }
        
        public int getPairs() {
            return getTotalCards() / 2;
        }
        
        /**
         * Obtiene la dificultad basándose en el ID global del nivel
         * @param globalLevelId ID del nivel (0-199)
         * @return Dificultad correspondiente
         */
        public static Difficulty fromGlobalId(int globalLevelId) {
            if (globalLevelId < 50) return EASY;
            if (globalLevelId < 100) return NORMAL;
            if (globalLevelId < 150) return ADVANCED;
            return HARD;
        }
    }
    
    private int globalId;        // 0-199 (único en todo el juego)
    private int localId;         // 1-50 (dentro de la dificultad)
    private Difficulty difficulty;
    private boolean unlocked;
    private boolean completed;
    
    /**
     * Constructor
     * @param globalId ID único del nivel (0-199)
     */
    public LevelData(int globalId) {
        this.globalId = globalId;
        this.difficulty = Difficulty.fromGlobalId(globalId);
        this.localId = (globalId % 50) + 1; // 1-50
    }
    
    // ==================== GETTERS ====================
    
    public int getGlobalId() {
        return globalId;
    }
    
    public int getLocalId() {
        return localId;
    }
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /**
     * Obtiene el color de fondo según la dificultad (para UI)
     */
    public int getColorInt() {
        switch (difficulty) {
            case EASY: return 0x90EE90; // Verde claro
            case NORMAL: return 0xFFD700; // Amarillo dorado
            case ADVANCED: return 0xFF8C00; // Naranja
            case HARD: return 0xFF4500; // Rojo-naranja
            default: return 0xFFFFFF;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Level %d (%s %d) - %s%s",
            globalId,
            difficulty.name,
            localId,
            unlocked ? "🔓" : "🔒",
            completed ? " ✓" : ""
        );
    }
}