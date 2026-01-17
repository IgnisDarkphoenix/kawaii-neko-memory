package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Rutas centralizadas de todos los assets
 * Formato: PNG para imágenes, OGG para audio
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
    public static final String BTN_BACK = "images/ui/buttons/btn_back.png";
    
    // Botones de navegación (flechas)
    public static final String BTN_ARROW_UP = "images/ui/buttons/btn_arrow_up.png";
    public static final String BTN_ARROW_DOWN = "images/ui/buttons/btn_arrow_down.png";
    public static final String BTN_ARROW_LEFT = "images/ui/buttons/btn_arrow_left.png";
    public static final String BTN_ARROW_RIGHT = "images/ui/buttons/btn_arrow_right.png";
    
    // Tabs de dificultad
    public static final String TAB_EASY = "images/ui/buttons/tab_easy.png";
    public static final String TAB_NORMAL = "images/ui/buttons/tab_normal.png";
    public static final String TAB_ADVANCED = "images/ui/buttons/tab_advanced.png";
    public static final String TAB_HARD = "images/ui/buttons/tab_hard.png";
    
    // === SLIDERS ===
    public static final String SLIDER_BACKGROUND = "images/ui/sliders/slider_background.png";
    public static final String SLIDER_FILL = "images/ui/sliders/slider_fill.png";
    public static final String SLIDER_KNOB = "images/ui/sliders/slider_knob.png";
    
    // === ICONS ===
    public static final String ICON_DELETE = "images/ui/icons/icon_delete.png";
    public static final String ICON_GACHA = "images/ui/icons/icon_gacha.png";
    public static final String ICON_HINT = "images/ui/icons/icon_hint.png";
    public static final String ICON_HINT_HERO = "images/ui/icons/icon_hint_hero.png";
    public static final String ICON_NEKOIN = "images/ui/icons/icon_nekoin.png";
    public static final String ICON_PAUSE = "images/ui/icons/icon_pause.png";
    public static final String ICON_TIMEFREEZE = "images/ui/icons/icon_timefreeze.png";
    public static final String ICON_TIMEFREEZE_HERO = "images/ui/icons/icon_timefreeze_hero.png";
    public static final String ICON_UPGRADE = "images/ui/icons/icon_upgrade.png";
    
    // === PANELS ===
    public static final String PANEL_CONFIRM = "images/ui/panels/panel_confirm.png";
    public static final String PANEL_DEFEAT = "images/ui/panels/panel_defeat.png";
    public static final String PANEL_PAUSE = "images/ui/panels/panel_pause.png";
    public static final String PANEL_SETTINGS = "images/ui/panels/panel_settings.png";
    public static final String PANEL_VICTORY = "images/ui/panels/panel_victory.png";
    
    // === CARDS ===
    public static final String CARD_BACK = "images/cards/card_back.png";
    
    public static String getCardPath(int deckIndex, int cardIndex) {
        return "images/cards/deck" + deckIndex + "/character" + deckIndex + "_" + cardIndex + ".png";
    }
    
    public static String[] getDeckPaths(int deckIndex) {
        String[] paths = new String[7];
        for (int i = 0; i < 7; i++) {
            paths[i] = getCardPath(deckIndex, i);
        }
        return paths;
    }
    
    // === MUSIC (OGG) ===
    public static final String MUSIC_MENU = "audio/music/menu_theme.ogg";
    public static final String MUSIC_BAZAAR = "audio/music/bazaar_theme.ogg";
    
    public static String getGameMusicPath(int trackIndex) {
        return "audio/music/game_track_0" + (trackIndex + 1) + ".ogg";
    }
    
    // === SFX (OGG) ===
    public static final String SFX_CARD_FLIP = "audio/sfx/card_flip.ogg";
    public static final String SFX_CARD_SHUFFLE = "audio/sfx/card_shuffle.ogg";
    public static final String SFX_MATCH = "audio/sfx/match.ogg";
    public static final String SFX_NO_MATCH = "audio/sfx/no_match.ogg";
    public static final String SFX_COIN = "audio/sfx/coin.ogg";
    public static final String SFX_BUTTON = "audio/sfx/button_click.ogg";
    public static final String SFX_VICTORY = "audio/sfx/victory.ogg";
    public static final String SFX_DEFEAT = "audio/sfx/defeat.ogg";
    public static final String SFX_TIMEFREEZE = "audio/sfx/time_freeze.ogg";
    
    // === CONSTANTES ===
    public static final int TOTAL_DECKS = 5;
    public static final int CARDS_PER_DECK = 7;
}