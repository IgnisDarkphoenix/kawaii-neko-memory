package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Constantes globales del juego
 * 
 * @author DarkphoenixTeam
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
    public static final float TIME_MULT_ENDURANCE = 1.8f;   // Niveles 16-30
    public static final float TIME_MULT_SHUFFLE = 1.25f;    // Niveles 31-40
    public static final float TIME_MULT_MASTERY = 2.3f;     // Niveles 41-50
    
    // === SHUFFLE CONFIG ===
    public static final int SHUFFLE_TRIGGER_PAIRS = 3;      // Shuffle cada 3 pares
    
    // === NIVELES ===
    public static final int LEVELS_PER_DIFFICULTY = 50;
    public static final int TOTAL_LEVELS = 200;
    public static final int LEVELS_PER_ROW = 5;
    public static final int ROWS_PER_DIFFICULTY = 10;
    public static final int TOTAL_DIFFICULTIES = 4;
    
    // === RANGOS DE NIVEL (dentro de cada dificultad, 1-50) ===
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
    public static final int ACTIVE_DECK_SIZE = 15;          // Cartas en el mazo activo
    
    // Distribución de cartas por dificultad (en el mazo de 15)
    public static final int CARDS_FOR_EASY = 6;             // Verde
    public static final int CARDS_FOR_NORMAL = 2;           // Amarillo
    public static final int CARDS_FOR_ADVANCED = 2;         // Naranja
    public static final int CARDS_FOR_HARD = 5;             // Rojo
    
    // === VALORES DE NEKOINS POR RAREZA DE CARTA ===
    public static final int NEKOIN_VALUE_BASE = 1;          // Gris atigrado
    public static final int NEKOIN_VALUE_STAR1 = 2;         // Blanco ojos azules
    public static final int NEKOIN_VALUE_STAR2 = 3;         // Negro ojos miel
    public static final int NEKOIN_VALUE_STAR3 = 5;         // Pelaje esponjoso
    public static final int NEKOIN_VALUE_HEART = 7;         // Naranjoso
    public static final int[] NEKOIN_PER_DECK = {1, 2, 3, 5, 7};
    
    // === RECOMPENSAS POR DIFICULTAD ===
    // [dificultad][0=base1estrella, 1=bonoPorEstrella, 2=firstClear]
    public static final int[][] REWARDS_BY_DIFFICULTY = {
        {10, 10, 50},    // EASY: base=10, +10/estrella, first=50
        {20, 20, 100},   // NORMAL: base=20, +20/estrella, first=100
        {40, 30, 200},   // ADVANCED: base=40, +30/estrella, first=200
        {100, 100, 500}  // HARD: base=100, +100/estrella, first=500
    };
    
    // === SISTEMA DE ESTRELLAS ===
    public static final float STARS_3_THRESHOLD = 0.50f;    // >50% tiempo restante
    public static final float STARS_2_THRESHOLD = 0.25f;    // >25% tiempo restante
    
    // === POWERS ===
    public static final int TIMEFREEZE_MIN_COST = 10;
    public static final int TIMEFREEZE_MAX_COST = 50;
    public static final int HINT_MIN_COST = 20;
    public static final int HINT_MAX_COST = 100;
    public static final int UPGRADE_BASE_COST = 100;
    public static final float UPGRADE_COST_MULTIPLIER = 1.5f;
    
    // Límites por partida
    public static final int MAX_HINTS_PER_GAME = 5;
    public static final int MAX_HINT_ADS_PER_GAME = 2;
    public static final int MAX_TIME_ADS_PER_GAME = 3;
    
    // Efectos de powers
    public static final float TIMEFREEZE_MIN_DURATION = 1.5f;
    public static final float TIMEFREEZE_MAX_DURATION = 5.0f;
    public static final float HINT_SHAKE_DURATION = 1.5f;
    public static final int HINT_CARDS_EASY = 3;            // 2 pares + 1 random
    public static final int HINT_CARDS_HARD = 5;            // 2 pares + 1 random
    
    // === ADS ===
    public static final int GAMES_BEFORE_INTERSTITIAL_LOSS = 3;
    public static final int GAMES_BEFORE_INTERSTITIAL_WIN = 10;
    public static final int REWARDED_TIME_BONUS = 5;
    public static final int RETRY_TIME_BONUS = 15;
    
    // === UI GAMEPLAY ===
    public static final float CARD_MARGIN_PERCENT = 0.05f;  // 5% margen entre cartas
    public static final float HUD_HEIGHT = 80f;             // Altura del HUD superior
    public static final float GRID_PADDING = 10f;           // Padding del área de juego
    
    // === MÚSICA ===
    public static final int GAME_MUSIC_TRACKS = 5;          // game_track_01 a 05
}