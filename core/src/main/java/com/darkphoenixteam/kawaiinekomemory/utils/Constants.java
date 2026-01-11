package com.darkphoenixteam.kawaiinekomemory.utils;

public class Constants {
    
    // === GAME INFO ===
    public static final String GAME_TITLE = "Kawaii Neko Memory";
    public static final String VERSION = "1.0.0";
    
    // === SCREEN DIMENSIONS (Base) ===
    public static final float WORLD_WIDTH = 720f;
    public static final float WORLD_HEIGHT = 1280f;
    
    // === TIMING ===
    public static final float SPLASH_DURATION = 2.0f;
    public static final float CARD_FLIP_TIME = 0.3f;
    public static final float MATCH_CHECK_DELAY = 0.3f;
    public static final float STATE_CHECK_DELAY = 0.3f;
    
    // === DIFFICULTY GRID SIZES ===
    public static final int EASY_COLS = 3;
    public static final int EASY_ROWS = 4;       // 12 cards = 6 pairs
    
    public static final int NORMAL_COLS = 4;
    public static final int NORMAL_ROWS = 4;     // 16 cards = 8 pairs
    
    public static final int ADVANCED_COLS = 4;
    public static final int ADVANCED_ROWS = 5;   // 20 cards = 10 pairs
    
    public static final int HARD_COLS = 5;
    public static final int HARD_ROWS = 6;       // 30 cards = 15 pairs
    
    // === LEVELS PER DIFFICULTY ===
    public static final int LEVELS_PER_DIFFICULTY = 50;
    public static final int TOTAL_LEVELS = 200;
    
    // === CARD DIMENSIONS ===
    public static final float CARD_WIDTH = 256f;
    public static final float CARD_HEIGHT = 384f;
    public static final float CARD_MARGIN_PERCENT = 0.07f;  // 7%
    
    // === NEKOINS ===
    public static final int NEKOIN_BASE = 1;
    public static final int NEKOIN_STAR1 = 2;
    public static final int NEKOIN_STAR2 = 3;
    public static final int NEKOIN_STAR3 = 5;
    public static final int NEKOIN_HEART = 7;
    
    // === POWERS ===
    public static final int POWER_FREEZE_MIN_COST = 10;
    public static final int POWER_FREEZE_MAX_COST = 50;
    public static final float POWER_FREEZE_MIN_DURATION = 1.5f;
    public static final float POWER_FREEZE_MAX_DURATION = 5.0f;
    
    public static final int POWER_HINT_MIN_COST = 20;
    public static final int POWER_HINT_MAX_COST = 100;
    public static final int POWER_HINT_MAX_PER_GAME = 5;
    
    public static final int POWER_UPGRADE_BASE_COST = 250;
    public static final float POWER_UPGRADE_MULTIPLIER = 1.25f;  // 25% increase
    
    // === ADS ===
    public static final int INTERSTITIAL_LOSS_INTERVAL = 3;
    public static final int INTERSTITIAL_WIN_INTERVAL = 10;
    public static final int REWARDED_HINTS_MAX = 2;
    public static final int REWARDED_TIME_MAX = 2;
    public static final int REWARDED_TIME_BONUS = 5;
    public static final int REWARDED_RETRY_TIME_BONUS = 15;
    
    // === DECKS ===
    public static final int CARDS_PER_DECK = 7;
    public static final int TOTAL_DECKS = 5;
}