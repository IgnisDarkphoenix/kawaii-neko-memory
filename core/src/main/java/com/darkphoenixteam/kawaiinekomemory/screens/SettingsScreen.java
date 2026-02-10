package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleSlider;

/**
 * Pantalla de ajustes con sliders de volumen y selector de idioma
 * 
 * @author DarkphoenixTeam
 * @version 2.0 - Selector de idioma
 */
public class SettingsScreen extends BaseScreen {
    
    private static final String TAG = "SettingsScreen";
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont labelFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === SLIDERS ===
    private SimpleSlider musicSlider;
    private SimpleSlider soundSlider;
    
    private Texture sliderBgTexture;
    private Texture sliderFillTexture;
    private Texture sliderKnobTexture;
    
    // === BOTONES ===
    private SimpleButton backButton;
    private SimpleButton languageButton;
    private Texture backButtonTexture;
    private Texture langButtonTexture;
    
    // === TEXTURAS ===
    private Texture patternTexture;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private LocaleManager localeManager;
    
    // === INPUT ===
    private final Vector2 touchPoint = new Vector2();
    
    // === LAYOUT ===
    private static final float SLIDER_WIDTH_PERCENT = 0.55f;
    private static final float SLIDER_MAX_HEIGHT = 50f;
    private static final float LABEL_SPACING = 15f;
    private static final float SECTION_SPACING = 90f;
    private static final float BUTTON_WIDTH_PERCENT = 0.50f;
    
    public SettingsScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        labelFont = game.getFontManager().getButtonFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        localeManager = LocaleManager.getInstance();
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        createUI();
        
        Gdx.app.log(TAG, "Inicializado - Idioma: " + localeManager.getCurrentLanguage().displayName);
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
        
        try {
            sliderBgTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_BACKGROUND));
            sliderFillTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_FILL));
            sliderKnobTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_KNOB));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando sliders: " + e.getMessage());
        }
        
        try {
            backButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando boton back");
        }
        
        try {
            langButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_EMPTY));
            if (langButtonTexture == null) {
                langButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            }
        } catch (Exception e) {
            try {
                langButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            } catch (Exception e2) {
                Gdx.app.error(TAG, "Error cargando boton idioma");
            }
        }
    }
    
    private void createUI() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float sliderWidth = Constants.VIRTUAL_WIDTH * SLIDER_WIDTH_PERCENT;
        float sliderX = centerX - (sliderWidth / 2f);
        
        // === SLIDER DE MÚSICA ===
        float musicSliderY = Constants.VIRTUAL_HEIGHT * 0.62f;
        
        if (sliderBgTexture != null) {
            musicSlider = new SimpleSlider(
                sliderBgTexture,
                sliderFillTexture,
                sliderKnobTexture,
                sliderX,
                musicSliderY,
                sliderWidth,
                SLIDER_MAX_HEIGHT
            );
            
            musicSlider.setValue(audioManager.getMusicVolume());
            musicSlider.setOnValueChanged(volume -> {
                audioManager.setMusicVolume(volume);
            });
        }
        
        // === SLIDER DE EFECTOS ===
        float sliderHeight = (musicSlider != null) ? musicSlider.getBounds().height : SLIDER_MAX_HEIGHT;
        float soundSliderY = musicSliderY - SECTION_SPACING - sliderHeight;
        
        if (sliderBgTexture != null) {
            soundSlider = new SimpleSlider(
                sliderBgTexture,
                sliderFillTexture,
                sliderKnobTexture,
                sliderX,
                soundSliderY,
                sliderWidth,
                SLIDER_MAX_HEIGHT
            );
            
            soundSlider.setValue(audioManager.getSoundVolume());
            soundSlider.setOnValueChanged(volume -> {
                audioManager.setSoundVolume(volume);
                if (!soundSlider.isDragging()) {
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                }
            });
        }
        
        // === BOTÓN DE IDIOMA ===
        float langButtonY = soundSliderY - SECTION_SPACING - 20f;
        float langButtonWidth = Constants.VIRTUAL_WIDTH * BUTTON_WIDTH_PERCENT;
        float langButtonHeight = 50f;
        
        if (langButtonTexture != null) {
            languageButton = new SimpleButton(
                langButtonTexture,
                localeManager.getCurrentLanguage().displayName,
                centerX - langButtonWidth / 2f,
                langButtonY,
                langButtonWidth,
                langButtonHeight
            );
            
            languageButton.setOnClick(this::cycleLanguage);
        }
        
        // === BOTÓN VOLVER ===
        if (backButtonTexture != null) {
            float buttonWidth = Constants.VIRTUAL_WIDTH * 0.45f;
            float aspectRatio = (float) backButtonTexture.getHeight() / backButtonTexture.getWidth();
            float buttonHeight = buttonWidth * aspectRatio;
            float buttonX = centerX - (buttonWidth / 2f);
            float buttonY = Constants.VIRTUAL_HEIGHT * 0.08f;
            
            backButton = new SimpleButton(
                backButtonTexture,
                localeManager.get("common.back"),
                buttonX,
                buttonY,
                buttonWidth,
                buttonHeight
            );
            
            backButton.setOnClick(() -> {
                Gdx.app.log(TAG, "Volviendo al menu principal");
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        }
    }
    
    private void cycleLanguage() {
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        
        boolean needsFontReload = localeManager.cycleLanguage();
        
        Gdx.app.log(TAG, "Idioma cambiado a: " + localeManager.getCurrentLanguage().displayName);
        
        if (needsFontReload) {
            // Regenerar fuentes para el nuevo tipo de idioma (CJK vs Latin)
            game.getFontManager().regenerateForLanguage(localeManager.isCJK());
            
            // Actualizar referencias de fuentes
            titleFont = game.getFontManager().getTitleFont();
            labelFont = game.getFontManager().getButtonFont();
            buttonFont = game.getFontManager().getButtonFont();
            smallFont = game.getFontManager().getSmallFont();
        }
        
        // Recrear UI con nuevos textos
        recreateUI();
    }
    
    private void recreateUI() {
        // Actualizar textos de los botones existentes
        // Para simplificar, recreamos la pantalla
        game.setScreen(new SettingsScreen(game));
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) {
            return;
        }
        
        boolean isTouched = Gdx.input.isTouched();
        
        if (isTouched) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
        }
        
        if (musicSlider != null) {
            musicSlider.update(touchPoint, isTouched);
        }
        
        if (soundSlider != null) {
            soundSlider.update(touchPoint, isTouched);
        }
        
        if (languageButton != null) {
            languageButton.update(viewport);
        }
        
        if (backButton != null) {
            backButton.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Fondo
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        // Título
        String title = localeManager.get("settings.title");
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = Constants.VIRTUAL_HEIGHT - 60f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // Sección Música
        if (musicSlider != null) {
            drawSliderSection(localeManager.get("settings.music"), musicSlider);
        }
        
        // Sección Efectos
        if (soundSlider != null) {
            drawSliderSection(localeManager.get("settings.sfx"), soundSlider);
        }
        
        // Sección Idioma
        drawLanguageSection();
        
        // Botón volver
        if (backButton != null) {
            backButton.draw(game.getBatch(), buttonFont);
        }
        
        game.getBatch().end();
    }
    
    private void drawSliderSection(String label, SimpleSlider slider) {
        float sliderHeight = slider.getBounds().height;
        float knobSize = slider.getKnobSize();
        
        layout.setText(labelFont, label);
        float labelX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float knobOverhang = (knobSize - sliderHeight) / 2f;
        float labelY = slider.getBounds().y + sliderHeight + knobOverhang + LABEL_SPACING + layout.height;
        labelFont.draw(game.getBatch(), label, labelX, labelY);
        
        slider.draw(game.getBatch());
        
        String percent = slider.getPercentage() + "%";
        layout.setText(labelFont, percent);
        float percentX = slider.getBounds().x + slider.getBounds().width + knobSize / 2f + 10f;
        float percentY = slider.getBounds().y + (sliderHeight + layout.height) / 2f;
        labelFont.draw(game.getBatch(), percent, percentX, percentY);
    }
    
    private void drawLanguageSection() {
        if (languageButton == null) return;
        
        float buttonY = languageButton.getY();
        float buttonHeight = languageButton.getHeight();
        
        // Label
        String label = localeManager.get("settings.language");
        layout.setText(labelFont, label);
        float labelX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float labelY = buttonY + buttonHeight + LABEL_SPACING + layout.height + 10f;
        labelFont.draw(game.getBatch(), label, labelX, labelY);
        
        // Botón con el nombre del idioma actual
        languageButton.draw(game.getBatch(), buttonFont);
        
        // Indicador de idioma actual debajo del botón
        LocaleManager.Language currentLang = localeManager.getCurrentLanguage();
        String langInfo = currentLang.code.toUpperCase() + " - " + currentLang.displayName;
        layout.setText(smallFont, langInfo);
        smallFont.setColor(Color.GRAY);
        smallFont.draw(game.getBatch(), langInfo,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      buttonY - 10f);
        smallFont.setColor(Color.WHITE);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        if (patternTexture != null) patternTexture.dispose();
        if (sliderBgTexture != null) sliderBgTexture.dispose();
        if (sliderFillTexture != null) sliderFillTexture.dispose();
        if (sliderKnobTexture != null) sliderKnobTexture.dispose();
        if (backButtonTexture != null) backButtonTexture.dispose();
        if (langButtonTexture != null) langButtonTexture.dispose();
        
        if (backButton != null) backButton.dispose();
        if (languageButton != null) languageButton.dispose();
    }
            }
