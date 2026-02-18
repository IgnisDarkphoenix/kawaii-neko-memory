package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Constantes globales del juego
 * 
 * @author DarkphoenixTeam
 * @version 2.1 - Time Attack Dual Mode + Card requirements
 */
public final class Constants {
    
    private Constants() {} // No instanciable
    
    // === DIMENSIONES VIRTUALES ===
    public static final float VIRTUAL_WIDTH = 480f;
    public static final float VIRTUAL_HEIGHT = 800f;
    
    // === TIMING GENERAL ===
    public static final float SPLASH_DURATION = 2.5f;
    public static final float FADE_DURATION = 0.5f;
    public static final float CARD_FLIP_TIME = 0.3f;
    public static final float MATCH_CHECK_DELAY = 0.3f;
    public static final float SHUFFLE_DURATION = 0.5f;
    
    // === DIFICULTADES (columnas x filas) ===
    public static final int EASY_COLS = 3;
    public static final int EASY_ROWS = 4;
    public static final int NORMAL_COLS = 4;
    public static final int NORMAL_ROWS = 4;
    public static final int ADVANCED_COLS = 4;
    public static final int ADVANCED_ROWS = 5;
    public static final int HARD_COLS = 5;
    public static final int HARD_ROWS = 6;
    
    // === CARTAS MÍNIMAS REQUERIDAS POR DIFICULTAD ===
    public static final int MIN_CARDS_EASY = 6;      // 3x4 = 12 cartas = 6 pares
    public static final int MIN_CARDS_NORMAL = 8;    // 4x4 = 16 cartas = 8 pares
    public static final int MIN_CARDS_ADVANCED = 10; // 4x5 = 20 cartas = 10 pares
    public static final int MIN_CARDS_HARD = 15;     // 5x6 = 30 cartas = 15 pares
    
    // === TIME ATTACK MODE ===
    // Modo 12 (3x4 grid)
    public static final int TIME_ATTACK_12_COLS = 3;
    public static final int TIME_ATTACK_12_ROWS = 4;
    public static final int TIME_ATTACK_12_PAIRS = 6;
    public static final int MIN_CARDS_TIME_ATTACK_12 = 6;
    
    // Modo 30 (5x6 grid)
    public static final int TIME_ATTACK_30_COLS = 5;
    public static final int TIME_ATTACK_30_ROWS = 6;
    public static final int TIME_ATTACK_30_PAIRS = 15;
    public static final int MIN_CARDS_TIME_ATTACK_30 = 15;
    
    // Configuración común Time Attack
    public static final float TIME_ATTACK_BASE_TIME = 60f;          // 1:00 inicial
    public static final float TIME_ATTACK_MAX_TIME = 300f;          // 5:00 máximo
    public static final float TIME_ATTACK_UPGRADE_AMOUNT = 5f;      // +5 seg por mejora
    public static final int TIME_ATTACK_MAX_UPGRADES = 48;          // (300-60)/5 = 48 mejoras
    
    // Costo de mejora Time Attack: 500 +250/+500/+250/+500...
    public static final int TIME_ATTACK_UPGRADE_BASE_COST = 500;
    public static final int TIME_ATTACK_UPGRADE_INCREMENT_ODD = 250;   // Mejoras impares
    public static final int TIME_ATTACK_UPGRADE_INCREMENT_EVEN = 500;  // Mejoras pares
    
    // Nekoins por par en Time Attack
    public static final int TIME_ATTACK_NEKOIN_PER_PAIR = 5;
    public static final float TIME_ATTACK_AD_MULTIPLIER = 2.5f;
    
    // === TIEMPO BASE POR DIFICULTAD (segundos) ===
    public static final float TIME_BASE_EASY = 45f;
    public static final float TIME_BASE_NORMAL = 60f;
    public static final float TIME_BASE_ADVANCED = 80f;
    public static final float TIME_BASE_HARD = 120f;
    
    // === DECAY DE TIEMPO POR NIVEL ===
    public static final float TIME_DECAY_EASY = 1.0f;
    public static final float TIME_DECAY_NORMAL = 1.5f;
    public static final float TIME_DECAY_ADVANCED = 2.0f;
    public static final float TIME_DECAY_HARD = 2.5f;
    
    // === MULTIPLICADORES DE TIEMPO POR RANGO ===
    public static final float TIME_MULT_ENDURANCE = 1.8f;
    public static final float TIME_MULT_SHUFFLE = 1.25f;
    public static final float TIME_MULT_MASTERY = 2.3f;
    
    // === SHUFFLE CONFIG ===
    public static final int SHUFFLE_TRIGGER_PAIRS = 3;
    
    // === NIVELES ===
    public static final int LEVELS_PER_DIFFICULTY = 50;
    public static final int TOTAL_LEVELS = 200;
    public static final int LEVELS_PER_ROW = 5;
    public static final int ROWS_PER_DIFFICULTY = 10;
    public static final int TOTAL_DIFFICULTIES = 4;
    
    // === RANGOS DE NIVEL ===
    public static final int RANGE_SPEED_START = 1;
    public static final int RANGE_SPEED_END = 15;
    public static final int RANGE_ENDURANCE_START = 16;
    public static final int RANGE_ENDURANCE_END = 30;
    public static final int RANGE_SHUFFLE_START = 31;
    public static final int RANGE_SHUFFLE_END = 40;
    public static final int RANGE_MASTERY_START = 41;
    public static final int RANGE_MASTERY_END = 50;
    
    // === MAZO Y CARTAS ===
    public static final int TOTAL_DECKS = 5;
    public static final int CARDS_PER_DECK = 7;
    public static final int TOTAL_CARDS = 35;
    public static final int ACTIVE_DECK_SIZE = 15;
    
    // Distribución de cartas por dificultad
    public static final int CARDS_FOR_EASY = 6;
    public static final int CARDS_FOR_NORMAL = 2;
    public static final int CARDS_FOR_ADVANCED = 2;
    public static final int CARDS_FOR_HARD = 5;
    
    // === VALORES DE NEKOINS POR RAREZA DE CARTA ===
    public static final int NEKOIN_VALUE_BASE = 1;
    public static final int NEKOIN_VALUE_STAR1 = 2;
    public static final int NEKOIN_VALUE_STAR2 = 3;
    public static final int NEKOIN_VALUE_STAR3 = 5;
    public static final int NEKOIN_VALUE_HEART = 7;
    public static final int[] NEKOIN_PER_DECK = {1, 2, 3, 5, 7};
    
    // === RECOMPENSAS POR DIFICULTAD ===
    public static final int[][] REWARDS_BY_DIFFICULTY = {
        {10, 10, 50},
        {20, 20, 100},
        {40, 30, 200},
        {100, 100, 500}
    };
    
    // === SISTEMA DE ESTRELLAS ===
    public static final float STARS_3_THRESHOLD = 0.50f;
    public static final float STARS_2_THRESHOLD = 0.25f;
    
    // === POWERS - NUEVOS PRECIOS ===
    // Índice = cantidad actual, valor = precio
    public static final int[] POWER_PRICES = {500, 1000, 2000, 2500, 3500};
    public static final int MAX_POWER_STOCK = 5;
    
    // Límites por partida
    public static final int MAX_HINTS_PER_GAME = 5;
    public static final int MAX_TIMEFREEZE_PER_GAME = 5;
    public static final int MAX_HINT_ADS_PER_GAME = 2;
    public static final int MAX_TIME_ADS_PER_GAME = 3;
    
    // Efectos de powers
    public static final float TIMEFREEZE_DURATION = 5.0f;
    public static final float HINT_SHAKE_DURATION = 1.5f;
    public static final int HINT_CARDS_EASY = 3;
    public static final int HINT_CARDS_HARD = 5;
    
    // === GACHA - NUEVOS PRECIOS ===
    // Precio: 1000 + 500/1000/500/1000... alternando
    public static final int GACHA_BASE_COST = 1000;
    public static final int GACHA_INCREMENT_ODD = 500;
    public static final int GACHA_INCREMENT_EVEN = 1000;
    
    // === ADS ===
    public static final int GAMES_BEFORE_INTERSTITIAL_LOSS = 3;
    public static final int GAMES_BEFORE_INTERSTITIAL_WIN = 10;
    public static final int REWARDED_TIME_BONUS = 5;
    public static final int RETRY_TIME_BONUS = 15;
    
    // === UI GAMEPLAY ===
    public static final float CARD_MARGIN_PERCENT = 0.05f;
    public static final float HUD_HEIGHT = 80f;
    public static final float GRID_PADDING = 10f;
    
    // === MÚSICA ===
    public static final int GAME_MUSIC_TRACKS = 5;
    
    // === RANKINGS ===
    public static final int RANKINGS_MAX_ENTRIES = 100;
    
    // === MÉTODOS HELPER ===
    
    /**
     * Obtiene el mínimo de cartas activas requeridas para una dificultad
     */
    public static int getMinCardsForDifficulty(int difficultyIndex) {
        switch (difficultyIndex) {
            case 0: return MIN_CARDS_EASY;
            case 1: return MIN_CARDS_NORMAL;
            case 2: return MIN_CARDS_ADVANCED;
            case 3: return MIN_CARDS_HARD;
            default: return MIN_CARDS_EASY;
        }
    }
}
