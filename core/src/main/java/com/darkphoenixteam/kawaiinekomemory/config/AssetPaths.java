package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Rutas centralizadas de todos los assets
 * Cambiar aquí = cambiar en todo el juego
 * 
 * ACTUALIZADO: Coincide exactamente con la estructura de archivos real
 * 
 * @author DarkphoenixTeam
 */
public final class AssetPaths {
    
    private AssetPaths() {}
    
    // === SPLASH ===
    public static final String LOGO_DARKPHOENIX = "images/splash/logo_darkphoenix.webp";
    public static final String LOGO_GAME = "images/splash/logo_game.webp";
    
    // === BACKGROUNDS ===
    public static final String BG_EASY = "images/backgrounds/bg_easy.webp";
    public static final String BG_NORMAL = "images/backgrounds/bg_normal.webp";
    public static final String BG_ADVANCED = "images/backgrounds/bg_advanced.webp";
    public static final String BG_HARD = "images/backgrounds/bg_hard.webp";
    public static final String PATTERN_HOME = "images/backgrounds/pattern_home.webp";
    public static final String PATTERN_LEVELS = "images/backgrounds/pattern_levels.webp";
    public static final String PATTERN_BAZAAR = "images/backgrounds/pattern_bazaar.webp";
    
    // === BUTTONS ===
    public static final String BTN_PLAY = "images/ui/buttons/btn_play.webp";
    public static final String BTN_DECK = "images/ui/buttons/btn_deck.webp";
    public static final String BTN_BAZAAR = "images/ui/buttons/btn_bazaar.webp";
    public static final String BTN_ACHIEVEMENTS = "images/ui/buttons/btn_achievements.webp";
    public static final String BTN_SETTINGS = "images/ui/buttons/btn_settings.webp";
    public static final String BTN_LEVEL = "images/ui/buttons/btn_level.webp";
    public static final String BTN_BACK = "images/ui/buttons/btn_back.webp";
    
    // === SLIDERS ===
    public static final String SLIDER_BACKGROUND = "images/ui/sliders/slider_background.webp";
    public static final String SLIDER_FILL = "images/ui/sliders/slider_fill.webp";
    public static final String SLIDER_KNOB = "images/ui/sliders/slider_knob.webp";
    
    // === ICONS ===
    public static final String ICON_DELETE = "images/ui/icons/icon_delete.webp";
    public static final String ICON_GACHA = "images/ui/icons/icon_gacha.webp";
    public static final String ICON_HINT = "images/ui/icons/icon_hint.webp";
    public static final String ICON_HINT_HERO = "images/ui/icons/icon_hint_hero.webp";
    public static final String ICON_NEKOIN = "images/ui/icons/icon_nekoin.webp";
    public static final String ICON_PAUSE = "images/ui/icons/icon_pause.webp";
    public static final String ICON_TIMEFREEZE = "images/ui/icons/icon_timefreeze.webp";
    public static final String ICON_TIMEFREEZE_HERO = "images/ui/icons/icon_timefreeze_hero.webp";
    public static final String ICON_UPGRADE = "images/ui/icons/icon_upgrade.webp";
    
    // === PANELS ===
    public static final String PANEL_CONFIRM = "images/ui/panels/panel_confirm.webp";
    public static final String PANEL_DEFEAT = "images/ui/panels/panel_defeat.webp";
    public static final String PANEL_PAUSE = "images/ui/panels/panel_pause.webp";
    public static final String PANEL_SETTINGS = "images/ui/panels/panel_settings.webp";
    public static final String PANEL_VICTORY = "images/ui/panels/panel_victory.webp";
    
    // === CARDS ===
    public static final String CARD_BACK = "images/cards/card_back.webp";
    
    /**
     * Obtiene la ruta de una carta específica
     * @param deckIndex Índice del deck (0-4)
     * @param cardIndex Índice de la carta (0-6)
     * @return Ruta del asset
     */
    public static String getCardPath(int deckIndex, int cardIndex) {
        return "images/cards/deck" + deckIndex + "/character" + deckIndex + "_" + cardIndex + ".webp";
    }
    
    /**
     * Obtiene todas las rutas de cartas de un deck
     * @param deckIndex Índice del deck (0-4)
     * @return Array con las 7 rutas de cartas
     */
    public static String[] getDeckPaths(int deckIndex) {
        String[] paths = new String[7];
        for (int i = 0; i < 7; i++) {
            paths[i] = getCardPath(deckIndex, i);
        }
        return paths;
    }
    
    // === MUSIC (mantiene .ogg) ===
    public static final String MUSIC_MENU = "audio/music/menu_theme.ogg";
    public static final String MUSIC_BAZAAR = "audio/music/bazaar_theme.ogg";
    
    /**
     * Obtiene la ruta de una pista de música de gameplay
     * @param trackIndex Índice de la pista (0-4)
     * @return Ruta del asset
     */
    public static String getGameMusicPath(int trackIndex) {
        return "audio/music/game_track_0" + (trackIndex + 1) + ".ogg";
    }
    
    // === SFX (mantiene .ogg) ===
    public static final String SFX_CARD_FLIP = "audio/sfx/card_flip.ogg";
    public static final String SFX_CARD_SHUFFLE = "audio/sfx/card_shuffle.ogg";
    public static final String SFX_MATCH = "audio/sfx/match.ogg";
    public static final String SFX_NO_MATCH = "audio/sfx/no_match.ogg";
    public static final String SFX_COIN = "audio/sfx/coin.ogg";
    public static final String SFX_BUTTON = "audio/sfx/button_click.ogg";
    public static final String SFX_VICTORY = "audio/sfx/victory.ogg";
    public static final String SFX_DEFEAT = "audio/sfx/defeat.ogg";
    public static final String SFX_TIMEFREEZE = "audio/sfx/time_freeze.ogg";
    
    // === CONSTANTES DE DECKS ===
    public static final int TOTAL_DECKS = 5;
    public static final int CARDS_PER_DECK = 7;
}