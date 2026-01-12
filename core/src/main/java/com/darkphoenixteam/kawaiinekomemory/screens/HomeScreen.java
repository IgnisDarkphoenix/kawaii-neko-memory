package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal del menú
 * 
 * @author DarkphoenixTeam
 */
public class HomeScreen extends BaseScreen {
    
    // UI
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private GlyphLayout layout;
    
    // Background
    private Texture patternTexture;
    
    // Botones
    private Array<SimpleButton> buttons;
    
    // Layout de botones (proporción 2:1 de las texturas 1024x512)
    private static final float BUTTON_WIDTH = 320f;
    private static final float BUTTON_HEIGHT = 160f;
    private static final float BUTTON_SPACING = 15f;
    private static final float BUTTONS_START_Y = 520f;
    
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Fuentes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(1f, 0.4f, 0.7f, 1f);
        
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2f);
        buttonFont.setColor(0.3f, 0.3f, 0.3f, 1f);
        
        layout = new GlyphLayout();
        
        // Cargar assets
        loadAssets();
        
        // Crear botones
        createButtons();
        
        Gdx.app.log("HomeScreen", "Inicializado correctamente");
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            Gdx.app.log("HomeScreen", "Pattern cargado");
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Pattern no encontrado");
        }
    }
    
    private void createButtons() {
        buttons = new Array<>();
        
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonX = centerX - (BUTTON_WIDTH / 2f);
        float currentY = BUTTONS_START_Y;
        
        // Botón JUGAR
        SimpleButton btnPlay = createButton(AssetPaths.BTN_PLAY, "JUGAR", buttonX, currentY);
        btnPlay.setOnClick(() -> {
            Gdx.app.log("HomeScreen", "JUGAR presionado");
            // TODO: Ir a LevelSelectScreen
        });
        buttons.add(btnPlay);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón MAZO
        SimpleButton btnDeck = createButton(AssetPaths.BTN_DECK, "MAZO", buttonX, currentY);
        btnDeck.setOnClick(() -> {
            Gdx.app.log("HomeScreen", "MAZO presionado");
            // TODO: Ir a DeckEditorScreen
        });
        buttons.add(btnDeck);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón BAZAAR
        SimpleButton btnBazaar = createButton(AssetPaths.BTN_BAZAAR, "BAZAAR", buttonX, currentY);
        btnBazaar.setOnClick(() -> {
            Gdx.app.log("HomeScreen", "BAZAAR presionado");
            // TODO: Ir a BazaarScreen
        });
        buttons.add(btnBazaar);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón LOGROS
        SimpleButton btnAchievements = createButton(AssetPaths.BTN_ACHIEVEMENTS, "LOGROS", buttonX, currentY);
        btnAchievements.setOnClick(() -> {
            Gdx.app.log("HomeScreen", "LOGROS presionado");
            // TODO: Ir a AchievementsScreen
        });
        buttons.add(btnAchievements);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón AJUSTES
        SimpleButton btnSettings = createButton(AssetPaths.BTN_SETTINGS, "AJUSTES", buttonX, currentY);
        btnSettings.setOnClick(() -> {
            Gdx.app.log("HomeScreen", "AJUSTES presionado");
            // TODO: Ir a SettingsScreen
        });
        buttons.add(btnSettings);
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        Texture texture = null;
        try {
            texture = new Texture(Gdx.files.internal(texturePath));
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "No se pudo cargar: " + texturePath);
        }
        
        return new SimpleButton(texture, text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        // Actualizar botones
        for (SimpleButton button : buttons) {
            button.update(camera);
        }
    }
    
    @Override
    protected void draw() {
        // Dibujar background pattern si existe
        if (patternTexture != null) {
            game.getBatch().begin();
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += 512) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += 512) {
                    game.getBatch().draw(patternTexture, x, y, 512, 512);
                }
            }
            game.getBatch().end();
        }
        
        game.getBatch().begin();
        
        // Título con efecto de respiración
        float scale = 3.5f + (float) Math.sin(animTimer * 2) * 0.15f;
        titleFont.getData().setScale(scale);
        
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = Constants.VIRTUAL_HEIGHT - 80f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // Restaurar escala
        titleFont.getData().setScale(3.5f);
        
        // Dibujar botones
        for (SimpleButton button : buttons) {
            button.draw(game.getBatch(), buttonFont);
        }
        
        // Info de versión
        buttonFont.getData().setScale(1.2f);
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(buttonFont, version);
        float verX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        buttonFont.draw(game.getBatch(), version, verX, 30f);
        buttonFont.getData().setScale(2f);
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (patternTexture != null) patternTexture.dispose();
        
        // Disponer texturas de botones
        for (SimpleButton button : buttons) {
            // Las texturas se disponen aquí si guardamos referencia
            // Por ahora, SimpleButton no tiene dispose, lo agregaremos si es necesario
        }
    }
}