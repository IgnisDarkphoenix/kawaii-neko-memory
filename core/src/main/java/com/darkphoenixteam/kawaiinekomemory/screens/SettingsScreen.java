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
    
    // Texturas de sliders (compartidas)
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
    
    // Vector para conversión de coordenadas (reutilizable)
    private final Vector2 touchPoint = new Vector2();
    
    // === LAYOUT CONSTANTS ===
    private static final float SLIDER_WIDTH_PERCENT = 0.55f;  // 55% del ancho
    private static final float SLIDER_MAX_HEIGHT = 50f;       // Altura máxima del slider
    private static final float LABEL_SPACING = 15f;           // Espacio entre label y slider
    private static final float SECTION_SPACING = 100f;        // Espacio entre secciones
    private static final float BUTTON_WIDTH_PERCENT = 0.45f;  // 45% del ancho
    
    public SettingsScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Fonts del FontManager
        titleFont = game.getFontManager().getTitleFont();
        labelFont = game.getFontManager().getButtonFont();
        buttonFont = game.getFontManager().getButtonFont();
        layout = new GlyphLayout();
        
        // Audio Manager (singleton)
        audioManager = AudioManager.getInstance();
        
        loadAssets();
        createUI();
        
        Gdx.app.log(TAG, "Inicializado");
    }
    
    private void loadAssets() {
        // Pattern de fondo
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            Gdx.app.log(TAG, "Pattern cargado");
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado, usando color sólido");
        }
        
        // Texturas de sliders
        try {
            sliderBgTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_BACKGROUND));
            sliderFillTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_FILL));
            sliderKnobTexture = new Texture(Gdx.files.internal(AssetPaths.SLIDER_KNOB));
            
            Gdx.app.log(TAG, String.format(
                "Sliders cargados - BG: %dx%d, Fill: %dx%d, Knob: %dx%d",
                sliderBgTexture.getWidth(), sliderBgTexture.getHeight(),
                sliderFillTexture.getWidth(), sliderFillTexture.getHeight(),
                sliderKnobTexture.getWidth(), sliderKnobTexture.getHeight()
            ));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando texturas de sliders: " + e.getMessage());
        }
        
        // Textura del botón
        try {
            backButtonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_SETTINGS));
            Gdx.app.log(TAG, "Textura de botón cargada");
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando textura de botón: " + e.getMessage());
        }
    }
    
    private void createUI() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float sliderWidth = Constants.VIRTUAL_WIDTH * SLIDER_WIDTH_PERCENT;
        float sliderX = centerX - (sliderWidth / 2f);
        
        // === SLIDER DE MÚSICA ===
        float musicSliderY = Constants.VIRTUAL_HEIGHT * 0.55f;
        
        if (sliderBgTexture != null) {
            musicSlider = new SimpleSlider(
                sliderBgTexture,
                sliderFillTexture,
                sliderKnobTexture,
                sliderX,
                musicSliderY,
                sliderWidth,
                SLIDER_MAX_HEIGHT  // Ahora es maxHeight, no height fija
            );
            
            // Cargar valor actual
            musicSlider.setValue(audioManager.getMusicVolume());
            
            // Callback: actualizar volumen en tiempo real
            musicSlider.setOnValueChanged(volume -> {
                audioManager.setMusicVolume(volume);
            });
            
            Gdx.app.log(TAG, "Slider música creado en Y=" + musicSliderY);
        }
        
        // === SLIDER DE EFECTOS ===
        // Calcular Y basado en el slider anterior + espacio
        float sliderHeight = (musicSlider != null) ? musicSlider.getBounds().height : SLIDER_MAX_HEIGHT;
        float knobSize = (musicSlider != null) ? musicSlider.getKnobSize() : SLIDER_MAX_HEIGHT * 1.8f;
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
            
            // Cargar valor actual
            soundSlider.setValue(audioManager.getSoundVolume());
            
            // Callback: actualizar volumen + reproducir sonido de prueba
            soundSlider.setOnValueChanged(volume -> {
                audioManager.setSoundVolume(volume);
                // Solo reproducir feedback si cambió significativamente
                if (!soundSlider.isDragging()) {
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                }
            });
            
            Gdx.app.log(TAG, "Slider efectos creado en Y=" + soundSliderY);
        }
        
        // === BOTÓN VOLVER ===
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
            
            Gdx.app.log(TAG, "Botón volver creado");
        }
    }
    
    @Override
    protected void update(float delta) {
        // Detectar si hay toque
        boolean isTouched = Gdx.input.isTouched();
        
        // Convertir coordenadas de pantalla a mundo (una sola vez por frame)
        if (isTouched) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
        }
        
        // Actualizar sliders con las coordenadas convertidas
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
        
        // === PATTERN DE FONDO ===
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
        
        // === TÍTULO ===
        String title = "AJUSTES";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = Constants.VIRTUAL_HEIGHT - 80f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // === SECCIÓN MÚSICA ===
        if (musicSlider != null) {
            drawSliderSection("MÚSICA", musicSlider);
        }
        
        // === SECCIÓN EFECTOS ===
        if (soundSlider != null) {
            drawSliderSection("EFECTOS", soundSlider);
        }
        
        // === BOTÓN VOLVER ===
        if (backButton != null) {
            backButton.draw(game.getBatch(), buttonFont);
        }
        
        game.getBatch().end();
    }
    
    /**
     * Dibuja una sección de slider con su etiqueta y porcentaje
     */
    private void drawSliderSection(String label, SimpleSlider slider) {
        float sliderHeight = slider.getBounds().height;
        float knobSize = slider.getKnobSize();
        
        // Etiqueta centrada arriba del slider (considerando el knob)
        layout.setText(labelFont, label);
        float labelX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float knobOverhang = (knobSize - sliderHeight) / 2f;
        float labelY = slider.getBounds().y + sliderHeight + knobOverhang + LABEL_SPACING + layout.height;
        labelFont.draw(game.getBatch(), label, labelX, labelY);
        
        // Slider
        slider.draw(game.getBatch());
        
        // Porcentaje a la derecha del slider (considerando el knob)
        String percent = slider.getPercentage() + "%";
        layout.setText(labelFont, percent);
        float percentX = slider.getBounds().x + slider.getBounds().width + knobSize / 2f + 10f;
        float percentY = slider.getBounds().y + (sliderHeight + layout.height) / 2f;
        labelFont.draw(game.getBatch(), percent, percentX, percentY);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        // Texturas
        if (patternTexture != null) patternTexture.dispose();
        if (sliderBgTexture != null) sliderBgTexture.dispose();
        if (sliderFillTexture != null) sliderFillTexture.dispose();
        if (sliderKnobTexture != null) sliderKnobTexture.dispose();
        
        // Botón
        if (backButton != null) backButton.dispose();
    }
}