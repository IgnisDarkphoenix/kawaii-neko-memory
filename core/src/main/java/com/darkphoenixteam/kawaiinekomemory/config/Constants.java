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
    
    // === TIMING ===
    public static final float SPLASH_DURATION = 2.5f;
    public static final float FADE_DURATION = 0.5f;
    public static final float CARD_FLIP_TIME = 0.3f;
    public static final float MATCH_CHECK_DELAY = 0.3f;
    
    // === DIFICULTADES (columnas x filas) ===
    public static final int EASY_COLS = 3;
    public static final int EASY_ROWS = 4;
    public static final int NORMAL_COLS = 4;
    public static final int NORMAL_ROWS = 4;
    public static final int ADVANCED_COLS = 4;
    public static final int ADVANCED_ROWS = 5;
    public static final int HARD_COLS = 5;
    public static final int HARD_ROWS = 6;
    
    // === NIVELES ===
    public static final int LEVELS_PER_DIFFICULTY = 50;
    public static final int TOTAL_LEVELS = 200;
    
    // === NEKOINS ===
    public static final int[] NEKOIN_PER_DECK = {1, 2, 3, 5, 7};
    
    // === POWERS ===
    public static final int TIMEFREEZE_MIN_COST = 10;
    public static final int TIMEFREEZE_MAX_COST = 50;
    public static final int HINT_MIN_COST = 20;
    public static final int HINT_MAX_COST = 100;
    public static final int UPGRADE_BASE_COST = 250;
    public static final float UPGRADE_COST_MULTIPLIER = 1.25f;
    
    // === ADS ===
    public static final int GAMES_BEFORE_INTERSTITIAL_LOSS = 3;
    public static final int GAMES_BEFORE_INTERSTITIAL_WIN = 10;
    public static final int MAX_HINT_ADS_PER_GAME = 2;
    public static final int MAX_TIME_ADS_PER_GAME = 2;
    public static final int REWARDED_TIME_BONUS = 5;
    public static final int RETRY_TIME_BONUS = 15;


    // === LEVEL SELECT SCREEN ===
    public static final int LEVELS_PER_ROW = 5;
    public static final int ROWS_PER_DIFFICULTY = 10;
    public static final int TOTAL_DIFFICULTIES = 4;
    public static final int TOTAL_LEVELS = 200;
}