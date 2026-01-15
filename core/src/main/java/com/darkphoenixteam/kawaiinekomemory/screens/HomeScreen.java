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
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // Background
    private Texture patternTexture;
    
    // Botones
    private Array<SimpleButton> buttons;
    
    // Layout de botones
    private static final float BUTTON_WIDTH = 320f;
    private static final float BUTTON_SPACING = 12f;
    
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Obtener fuentes del FontManager
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        
        layout = new GlyphLayout();
        buttons = new Array<>();
        
        loadAssets();
        createButtons();
        
        Gdx.app.log("HomeScreen", "Inicializado con FontManager");
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
        
        // Crear primer botón para calcular height
        SimpleButton firstBtn = createButton(AssetPaths.BTN_PLAY, "JUGAR", buttonX, 0);
        float buttonHeight = firstBtn != null ? firstBtn.getHeight() : 160f;
        
        // Calcular posición inicial
        float totalHeight = (buttonHeight * 5) + (BUTTON_SPACING * 4);
        float currentY = (Constants.VIRTUAL_HEIGHT / 2f) + (totalHeight / 2f) - buttonHeight;
        
        // JUGAR
        if (firstBtn != null) {
            firstBtn.getBounds().y = currentY;
            firstBtn.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> JUGAR presionado <<<");
            });
            buttons.add(firstBtn);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // MAZO
        SimpleButton btnDeck = createButton(AssetPaths.BTN_DECK, "MAZO", buttonX, currentY);
        if (btnDeck != null) {
            btnDeck.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> MAZO presionado <<<");
            });
            buttons.add(btnDeck);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // BAZAAR
        SimpleButton btnBazaar = createButton(AssetPaths.BTN_BAZAAR, "BAZAAR", buttonX, currentY);
        if (btnBazaar != null) {
            btnBazaar.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> BAZAAR presionado <<<");
            });
            buttons.add(btnBazaar);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // LOGROS
        SimpleButton btnAchievements = createButton(AssetPaths.BTN_ACHIEVEMENTS, "LOGROS", buttonX, currentY);
        if (btnAchievements != null) {
            btnAchievements.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> LOGROS presionado <<<");
            });
            buttons.add(btnAchievements);
        }
        currentY -= (buttonHeight + BUTTON_SPACING);
        
        // AJUSTES
        SimpleButton btnSettings = createButton(AssetPaths.BTN_SETTINGS, "AJUSTES", buttonX, currentY);
        if (btnSettings != null) {
            btnSettings.setOnClick(() -> {
                Gdx.app.log("HomeScreen", ">>> AJUSTES presionado <<<");
            });
            buttons.add(btnSettings);
        }
        
        Gdx.app.log("HomeScreen", "Botones creados: " + buttons.size);
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        try {
            Texture texture = new Texture(Gdx.files.internal(texturePath));
            return new SimpleButton(texture, text, x, y, BUTTON_WIDTH);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Error cargando: " + texturePath);
            return null;
        }
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        for (SimpleButton button : buttons) {
            button.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Pattern de fondo con tile de 512x512
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
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title, titleX, Constants.VIRTUAL_HEIGHT - 80f);
        
        // Botones CON TEXTO
        for (SimpleButton button : buttons) {
            button.draw(game.getBatch(), buttonFont);
        }
        
        // Versión
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(smallFont, version);
        float verX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        smallFont.draw(game.getBatch(), version, verX, 25f);
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        // No disponer fuentes (las maneja FontManager)
        if (patternTexture != null) patternTexture.dispose();
        
        for (SimpleButton button : buttons) {
            button.dispose();
        }
    }
}