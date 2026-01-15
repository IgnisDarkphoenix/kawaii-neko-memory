package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Rutas centralizadas de todos los assets
 * Cambiar aquí = cambiar en todo el juego
 * 
 * @author DarkphoenixTeam
 */
public final class AssetPaths {
    
    private AssetPaths() {}
    
    // === SPLASH ===
    public static final String LOGO_DARKPHOENIX = "images/splash/logo_darkphoenix.png";
    public static final String LOGO_GAME = "images/splash/logo_game.png";
    
    // === BACKGROUNDS ===
    public static final String BG_EASY = "images/backgrounds/bg_easy.png";
    public static final String BG_NORMAL = "images/backgrounds/bg_normal.png";
    public static final String BG_ADVANCED = "images/backgrounds/bg_advanced.png";
    public static final String BG_HARD = "images/backgrounds/bg_hard.png";
    public static final String PATTERN_HOME = "images/backgrounds/pattern_home.png";
    public static final String PATTERN_LEVELS = "images/backgrounds/pattern_levels.png";
    public static final String PATTERN_BAZAAR = "images/backgrounds/pattern_bazaar.png";
    
    // === BUTTONS ===
    public static final String BTN_PLAY = "images/ui/buttons/btn_play.png";
    public static final String BTN_DECK = "images/ui/buttons/btn_deck.png";
    public static final String BTN_BAZAAR = "images/ui/buttons/btn_bazaar.png";
    public static final String BTN_ACHIEVEMENTS = "images/ui/buttons/btn_achievements.png";
    public static final String BTN_SETTINGS = "images/ui/buttons/btn_settings.png";
    public static final String BTN_LEVEL = "images/ui/buttons/btn_level.png";
    
    // === SLIDERS ===
    public static final String SLIDER_BACKGROUND = "images/ui/sliders/slider_background.png";
    public static final String SLIDER_FILL = "images/ui/sliders/slider_fill.png";
    public static final String SLIDER_KNOB = "images/ui/sliders/slider_knob.png";
    
    // === CARDS ===
    public static final String CARD_BACK = "images/cards/card_back.png";
    
    /**
     * Obtiene la ruta de una carta específica
     * @param deckIndex Índice del mazo (0-4)
     * @param cardIndex Índice de la carta (0-6)
     * @return Ruta del asset
     */
    public static String getCardPath(int deckIndex, int cardIndex) {
        return "images/cards/deck" + deckIndex + "/character" + deckIndex + "_" + cardIndex + ".png";
    }
    
    // === ICONS ===
    public static final String ICON_HINT = "images/ui/icons/icon_hint.png";
    public static final String ICON_TIMEFREEZE = "images/ui/icons/icon_timefreeze.png";
    public static final String ICON_PAUSE = "images/ui/icons/icon_pause.png";
    public static final String ICON_NEKOIN = "images/ui/icons/icon_nekoin.png";
    
    // === MUSIC ===
    public static final String MUSIC_MENU = "audio/music/menu_theme.ogg";
    public static final String MUSIC_BAZAAR = "audio/music/bazaar_theme.ogg";
    
    /**
     * Obtiene la ruta de una pista de juego
     * @param trackIndex Índice de la pista (0-4)
     * @return Ruta del asset
     */
    public static String getGameMusicPath(int trackIndex) {
        return "audio/music/game_track_0" + (trackIndex + 1) + ".ogg";
    }
    
    // === SFX ===
    public static final String SFX_CARD_FLIP = "audio/sfx/card_flip.ogg";
    public static final String SFX_CARD_SHUFFLE = "audio/sfx/card_shuffle.ogg";
    public static final String SFX_MATCH = "audio/sfx/match.ogg";
    public static final String SFX_NO_MATCH = "audio/sfx/no_match.ogg";
    public static final String SFX_COIN = "audio/sfx/coin.ogg";
    public static final String SFX_BUTTON = "audio/sfx/button_click.ogg";
    public static final String SFX_VICTORY = "audio/sfx/victory.ogg";
    public static final String SFX_DEFEAT = "audio/sfx/defeat.ogg";
    public static final String SFX_TIMEFREEZE = "audio/sfx/time_freeze.ogg";
}