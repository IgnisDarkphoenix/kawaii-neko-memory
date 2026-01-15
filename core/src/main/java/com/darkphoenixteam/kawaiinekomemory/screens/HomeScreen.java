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
    
    // Layout de botones - Solo definimos WIDTH, height se calcula automáticamente
    private static final float BUTTON_WIDTH = 320f;
    // BUTTON_HEIGHT se calcula automáticamente según aspect ratio (320 * 0.5 = 160)
    private static final float BUTTON_SPACING = 12f;
    
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
            Gdx.app.log("HomeScreen", "Pattern cargado");
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Pattern no encontrado");
        }
    }
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonX = centerX - (BUTTON_WIDTH / 2f);
        
        // Crear primer botón para calcular height real
        SimpleButton firstBtn = createButton(AssetPaths.BTN_PLAY, buttonX, 0);
        float buttonHeight = firstBtn != null ? firstBtn.getHeight() : 160f;
        
        // Calcular posición inicial (centrado verticalmente)
        float totalHeight = (buttonHeight * 5) + (BUTTON_SPACING * 4);
        float currentY = (Constants.VIRTUAL_HEIGHT / 2f) + (totalHeight / 2f) - buttonHeight;
        
        Gdx.app.log("HomeScreen", "Button dimensions: " + BUTTON_WIDTH + "x" + buttonHeight);
        Gdx.app.log("HomeScreen", "Starting Y: " + currentY);
        
        // Reposicionar primer botón
        if (firstBtn != null) {
            firstBtn.getBounds().y = currentY;
            firstBtn.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> JUGAR presionado <<<");
            });
            buttons.add(firstBtn);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // Botón MAZO
        SimpleButton btnDeck = createButton(AssetPaths.BTN_DECK, buttonX, currentY);
        if (btnDeck != null) {
            btnDeck.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> MAZO presionado <<<");
            });
            buttons.add(btnDeck);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // Botón BAZAAR
        SimpleButton btnBazaar = createButton(AssetPaths.BTN_BAZAAR, buttonX, currentY);
        if (btnBazaar != null) {
            btnBazaar.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> BAZAAR presionado <<<");
            });
            buttons.add(btnBazaar);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // Botón LOGROS
        SimpleButton btnAchievements = createButton(AssetPaths.BTN_ACHIEVEMENTS, buttonX, currentY);
        if (btnAchievements != null) {
            btnAchievements.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> LOGROS presionado <<<");
            });
            buttons.add(btnAchievements);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // Botón AJUSTES
        SimpleButton btnSettings = createButton(AssetPaths.BTN_SETTINGS, buttonX, currentY);
        if (btnSettings != null) {
            btnSettings.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> AJUSTES presionado <<<");
            });
            buttons.add(btnSettings);
        }
        
        Gdx.app.log("HomeScreen", "Botones creados: " + buttons.size);
    }
    
    private SimpleButton createButton(String texturePath, float x, float y) {
        try {
            Texture texture = new Texture(Gdx.files.internal(texturePath));
            // Usar constructor con aspect ratio automático (solo width)
            return new SimpleButton(texture, "", x, y, BUTTON_WIDTH);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Error cargando: " + texturePath);
            return null;
        }
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        // Actualizar botones
        for (SimpleButton button : buttons) {
            button.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Dibujar background pattern semi-transparente
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.2f);
            int patternSize = 256;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += patternSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += patternSize) {
                    game.getBatch().draw(patternTexture, x, y, patternSize, patternSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        // Título con efecto de respiración
        float scale = 3f + (float) Math.sin(animTimer * 2) * 0.12f;
        titleFont.getData().setScale(scale);
        
        String title = "Kawaii Neko";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title, titleX, Constants.VIRTUAL_HEIGHT - 60f);
        
        String title2 = "Memory";
        layout.setText(titleFont, title2);
        float title2X = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title2, title2X, Constants.VIRTUAL_HEIGHT - 110f);
        
        titleFont.getData().setScale(3f);
        
        // Dibujar botones
        for (SimpleButton button : buttons) {
            button.drawNoText(game.getBatch());
        }
        
        // Info de versión
        buttonFont.getData().setScale(1f);
        buttonFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(buttonFont, version);
        float verX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        buttonFont.draw(game.getBatch(), version, verX, 25f);
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