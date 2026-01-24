package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleSlider;

/**
 * Pantalla de ajustes
 * Controles de volumen para música y efectos
 * 
 * @author DarkphoenixTeam
 */
public class SettingsScreen extends BaseScreen {
    
    private static final String TAG = "SettingsScreen";
    
    // Fonts
    private BitmapFont titleFont;
    private BitmapFont labelFont;
    private BitmapFont buttonFont;
    private GlyphLayout layout;
    
    // Sliders
    private SimpleSlider musicSlider;
    private SimpleSlider soundSlider;
    
    // Texturas de sliders
    private Texture sliderBgTexture;
    private Texture sliderFillTexture;
    private Texture sliderKnobTexture;
    
    // Botón volver
    private SimpleButton backButton;
    private Texture backButtonTexture;
    
    // Background
    private Texture patternTexture;
    
    // Audio Manager
    private AudioManager audioManager;
    // Mantener música del menú
AudioManager.getInstance().playMusic(AssetPaths.MUSIC_MENU, true);
    
    // Vector para conversión de coordenadas
    private final Vector2 touchPoint = new Vector2();
    
    // === LAYOUT CONSTANTS ===
    private static final float SLIDER_WIDTH_PERCENT = 0.55f;
    private static final float SLIDER_MAX_HEIGHT = 50f;
    private static final float LABEL_SPACING = 15f;
    private static final float SECTION_SPACING = 100f;
    private static final float BUTTON_WIDTH_PERCENT = 0.45f;
    
    public SettingsScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        labelFont = game.getFontManager().getButtonFont();
        buttonFont = game.getFontManager().getButtonFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        
        loadAssets();
        createUI();
        
        Gdx.app.log(TAG, "Inicializado");
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
            
            Gdx.app.log(TAG, String.format(
                "Sliders cargados - BG: %dx%d, Knob: %dx%d",
                sliderBgTexture.getWidth(), sliderBgTexture.getHeight(),
                sliderKnobTexture.getWidth(), sliderKnobTexture.getHeight()
            ));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando sliders: " + e.getMessage());
        }
        
        try {
            backButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_SETTINGS));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando botón: " + e.getMessage());
        }
    }
    
    private void createUI() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float sliderWidth = Constants.VIRTUAL_WIDTH * SLIDER_WIDTH_PERCENT;
        float sliderX = centerX - (sliderWidth / 2f);
        
        // Slider de música
        float musicSliderY = Constants.VIRTUAL_HEIGHT * 0.55f;
        
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
        
        // Slider de efectos
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
        
        // Botón volver
        if (backButtonTexture != null) {
            float buttonWidth = Constants.VIRTUAL_WIDTH * BUTTON_WIDTH_PERCENT;
            float aspectRatio = (float) backButtonTexture.getHeight() / backButtonTexture.getWidth();
            float buttonHeight = buttonWidth * aspectRatio;
            float buttonX = centerX - (buttonWidth / 2f);
            float buttonY = Constants.VIRTUAL_HEIGHT * 0.08f;
            
            backButton = new SimpleButton(
                backButtonTexture,
                "VOLVER",
                buttonX,
                buttonY,
                buttonWidth,
                buttonHeight
            );
            
            backButton.setOnClick(() -> {
                Gdx.app.log(TAG, "Volviendo al menú principal");
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        }
    }
    
    @Override
    protected void update(float delta) {
        // === VERIFICAR INPUT DELAY ===
        // Solo procesar input si el delay ya pasó
        if (!isInputEnabled()) {
            return;  // Ignorar todo input durante el delay
        }
        
        // Detectar toque
        boolean isTouched = Gdx.input.isTouched();
        
        // Convertir coordenadas
        if (isTouched) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
        }
        
        // Actualizar sliders
        if (musicSlider != null) {
            musicSlider.update(touchPoint, isTouched);
        }
        
        if (soundSlider != null) {
            soundSlider.update(touchPoint, isTouched);
        }
        
        // Actualizar botón
        if (backButton != null) {
            backButton.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Pattern de fondo
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
        String title = "AJUSTES";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = Constants.VIRTUAL_HEIGHT - 80f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // Sección música
        if (musicSlider != null) {
            drawSliderSection("MÚSICA", musicSlider);
        }
        
        // Sección efectos
        if (soundSlider != null) {
            drawSliderSection("EFECTOS", soundSlider);
        }
        
        // Botón volver
        if (backButton != null) {
            backButton.draw(game.getBatch(), buttonFont);
        }
        
        game.getBatch().end();
    }
    
    private void drawSliderSection(String label, SimpleSlider slider) {
        float sliderHeight = slider.getBounds().height;
        float knobSize = slider.getKnobSize();
        
        // Etiqueta
        layout.setText(labelFont, label);
        float labelX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float knobOverhang = (knobSize - sliderHeight) / 2f;
        float labelY = slider.getBounds().y + sliderHeight + knobOverhang + LABEL_SPACING + layout.height;
        labelFont.draw(game.getBatch(), label, labelX, labelY);
        
        // Slider
        slider.draw(game.getBatch());
        
        // Porcentaje
        String percent = slider.getPercentage() + "%";
        layout.setText(labelFont, percent);
        float percentX = slider.getBounds().x + slider.getBounds().width + knobSize / 2f + 10f;
        float percentY = slider.getBounds().y + (sliderHeight + layout.height) / 2f;
        labelFont.draw(game.getBatch(), percent, percentX, percentY);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        if (patternTexture != null) patternTexture.dispose();
        if (sliderBgTexture != null) sliderBgTexture.dispose();
        if (sliderFillTexture != null) sliderFillTexture.dispose();
        if (sliderKnobTexture != null) sliderKnobTexture.dispose();
        
        if (backButton != null) backButton.dispose();
    }
}
