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
    
    // Layout de botones - Ajustado para mejor visualización
    private static final float BUTTON_WIDTH = 280f;
    private static final float BUTTON_HEIGHT = 70f;
    private static final float BUTTON_SPACING = 15f;
    private static final float BUTTONS_START_Y = 480f;
    
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Fuentes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(1f, 0.4f, 0.7f, 1f);
        
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(1f, 1f, 1f, 1f);
        
        layout = new GlyphLayout();
        buttons = new Array<>();
        
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
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Pattern no encontrado");
        }
    }
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonX = centerX - (BUTTON_WIDTH / 2f);
        float currentY = BUTTONS_START_Y;
        
        // Botón JUGAR
        SimpleButton btnPlay = createButton(AssetPaths.BTN_PLAY, "", buttonX, currentY);
        btnPlay.setOnClick(() -> {
            Gdx.app.log("HomeScreen", ">>> JUGAR presionado <<<");
            // TODO: game.setScreen(new LevelSelectScreen(game));
        });
        buttons.add(btnPlay);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón MAZO
        SimpleButton btnDeck = createButton(AssetPaths.BTN_DECK, "", buttonX, currentY);
        btnDeck.setOnClick(() -> {
            Gdx.app.log("HomeScreen", ">>> MAZO presionado <<<");
            // TODO: game.setScreen(new DeckEditorScreen(game));
        });
        buttons.add(btnDeck);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón BAZAAR
        SimpleButton btnBazaar = createButton(AssetPaths.BTN_BAZAAR, "", buttonX, currentY);
        btnBazaar.setOnClick(() -> {
            Gdx.app.log("HomeScreen", ">>> BAZAAR presionado <<<");
            // TODO: game.setScreen(new BazaarScreen(game));
        });
        buttons.add(btnBazaar);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón LOGROS
        SimpleButton btnAchievements = createButton(AssetPaths.BTN_ACHIEVEMENTS, "", buttonX, currentY);
        btnAchievements.setOnClick(() -> {
            Gdx.app.log("HomeScreen", ">>> LOGROS presionado <<<");
            // TODO: game.setScreen(new AchievementsScreen(game));
        });
        buttons.add(btnAchievements);
        currentY -= (BUTTON_HEIGHT + BUTTON_SPACING);
        
        // Botón AJUSTES
        SimpleButton btnSettings = createButton(AssetPaths.BTN_SETTINGS, "", buttonX, currentY);
        btnSettings.setOnClick(() -> {
            Gdx.app.log("HomeScreen", ">>> AJUSTES presionado <<<");
            // TODO: game.setScreen(new SettingsScreen(game));
        });
        buttons.add(btnSettings);
        
        Gdx.app.log("HomeScreen", "Botones creados: " + buttons.size);
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        Texture texture = null;
        try {
            texture = new Texture(Gdx.files.internal(texturePath));
            Gdx.app.log("HomeScreen", "Botón cargado: " + texturePath);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "No se pudo cargar: " + texturePath);
        }
        
        return new SimpleButton(texture, text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        // Actualizar botones con VIEWPORT (no camera)
        for (SimpleButton button : buttons) {
            button.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Dibujar background pattern
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += 128) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += 128) {
                    game.getBatch().draw(patternTexture, x, y, 128, 128);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        // Título con efecto de respiración
        float scale = 3f + (float) Math.sin(animTimer * 2) * 0.1f;
        titleFont.getData().setScale(scale);
        
        String title = "Kawaii Neko";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title, titleX, Constants.VIRTUAL_HEIGHT - 80f);
        
        String title2 = "Memory";
        layout.setText(titleFont, title2);
        float title2X = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title2, title2X, Constants.VIRTUAL_HEIGHT - 130f);
        
        titleFont.getData().setScale(3f);
        
        // Dibujar botones (sin texto porque las imágenes ya lo tienen)
        for (SimpleButton button : buttons) {
            button.drawNoText(game.getBatch());
        }
        
        // Info de versión
        buttonFont.getData().setScale(1f);
        buttonFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(buttonFont, version);
        float verX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        buttonFont.draw(game.getBatch(), version, verX, 30f);
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(1f, 1f, 1f, 1f);
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (patternTexture != null) patternTexture.dispose();
        
        for (SimpleButton button : buttons) {
            button.dispose();
        }
    }
}