package com.darkphoenixteam.kawaiinekomemory.config;

/**
 * Rutas centralizadas de todos los assets
 * PATCH: Ajuste de nombres para coincidir con archivos reales en repositorio
 */
public final class AssetPaths {
    
    private AssetPaths() {}
    
    // === SPLASH (Aquí estaba el error principal: nombres invertidos) ===
    public static final String LOGO_DARKPHOENIX = "images/splash/darkphoenix_logo.webp"; // Antes decía logo_darkphoenix
    public static final String LOGO_GAME = "images/splash/game_logo.webp";               // Antes decía logo_game
    
    // === BACKGROUNDS (Estos parecían coincidir, pero verificamos) ===
    public static final String BG_EASY = "images/backgrounds/bg_easy.webp";
    public static final String BG_NORMAL = "images/backgrounds/bg_normal.webp";
    public static final String BG_ADVANCED = "images/backgrounds/bg_advanced.webp";
    public static final String BG_HARD = "images/backgrounds/bg_hard.webp";
    public static final String PATTERN_HOME = "images/backgrounds/pattern_home.webp";
    public static final String PATTERN_LEVELS = "images/backgrounds/pattern_levels.webp";
    public static final String PATTERN_BAZAAR = "images/backgrounds/pattern_bazaar.webp";
    
    // === BUTTONS ===
    // IMPORTANTE: Verifica si tus archivos se llaman 'btn_play.webp' o 'play_btn.webp'. 
    // Dejo la convención estándar, pero si fallan los botones, revisa sus nombres.
    public static final String BTN_PLAY = "images/ui/buttons/btn_play.webp";
    public static final String BTN_DECK = "images/ui/buttons/btn_deck.webp";
    public static final String BTN_BAZAAR = "images/ui/buttons/btn_bazaar.webp";
    public static final String BTN_SETTINGS = "images/ui/buttons/btn_settings.webp";
    public static final String BTN_BACK = "images/ui/buttons/btn_back.webp";
    
    // === ICONS & UI ===
    public static final String CARD_BACK = "images/ui/cards/card_back.webp";
    public static final String CARD_FRONT_BG = "images/ui/cards/card_front_bg.webp";
    public static final String COIN_ICON = "images/ui/icons/coin.webp";
    public static final String ICON_MUSIC = "images/ui/icons/icon_music.webp";
    public static final String ICON_SOUND = "images/ui/icons/icon_sound.webp";
    
    // === PANELS ===
    public static final String PANEL_DIALOG = "images/ui/panels/panel_dialog.webp";
    public static final String PANEL_POPUP = "images/ui/panels/panel_popup.webp";
    
    // === MUSIC (Se mantiene OGG) ===
    public static final String MUSIC_MENU = "audio/music/menu_theme.ogg";
    public static final String MUSIC_BAZAAR = "audio/music/bazaar_theme.ogg";
    
    public static String getGameMusicPath(int trackIndex) {
        return "audio/music/game_track_0" + (trackIndex + 1) + ".ogg";
    }
    
    // === SFX (Se mantiene OGG) ===
    public static final String SFX_CARD_FLIP = "audio/sfx/card_flip.ogg";
    public static final String SFX_CARD_SHUFFLE = "audio/sfx/card_shuffle.ogg";
    public static final String SFX_MATCH = "audio/sfx/match.ogg";
    public static final String SFX_NO_MATCH = "audio/sfx/no_match.ogg";
    public static final String SFX_COIN = "audio/sfx/coin.ogg";
    public static final String SFX_CLICK = "audio/sfx/button_click.ogg";
    public static final String SFX_VICTORY = "audio/sfx/victory.ogg";
    public static final String SFX_DEFEAT = "audio/sfx/defeat.ogg";
    public static final String SFX_TIME_FREEZE = "audio/sfx/time_freeze.ogg";
}
