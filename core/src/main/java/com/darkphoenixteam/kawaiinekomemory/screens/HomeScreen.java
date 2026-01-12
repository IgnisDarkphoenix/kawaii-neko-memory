package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla principal del menú
 * 
 * @author DarkphoenixTeam
 */
public class HomeScreen extends BaseScreen {
    
    // Texturas de botones
    private Texture btnPlayTex;
    private Texture btnDeckTex;
    private Texture btnBazaarTex;
    private Texture btnAchievementsTex;
    private Texture btnSettingsTex;
    private Texture patternTex;
    
    // Sprites de botones
    private Sprite btnPlay;
    private Sprite btnDeck;
    private Sprite btnBazaar;
    private Sprite btnAchievements;
    private Sprite btnSettings;
    
    // Hitboxes para detectar touch
    private Rectangle[] buttonBounds;
    
    // Para título
    private BitmapFont titleFont;
    private GlyphLayout layout;
    
    // Touch
    private Vector3 touchPos;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        touchPos = new Vector3();
        layout = new GlyphLayout();
        
        loadAssets();
        setupButtons();
    }
    
    private void loadAssets() {
        try {
            btnPlayTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            btnDeckTex = new Texture(Gdx.files.internal(AssetPaths.BTN_DECK));
            btnBazaarTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BAZAAR));
            btnAchievementsTex = new Texture(Gdx.files.internal(AssetPaths.BTN_ACHIEVEMENTS));
            btnSettingsTex = new Texture(Gdx.files.internal(AssetPaths.BTN_SETTINGS));
            
            Gdx.app.log("HomeScreen", "Botones cargados OK");
        } catch (Exception e) {
            Gdx.app.error("HomeScreen", "Error cargando assets: " + e.getMessage());
        }
        
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
    }
    
    private void setupButtons() {
        float btnWidth = Constants.VIRTUAL_WIDTH * 0.75f;
        float btnHeight = btnWidth * 0.3f; // Proporción aproximada
        float btnX = (Constants.VIRTUAL_WIDTH - btnWidth) / 2f;
        float startY = Constants.VIRTUAL_HEIGHT * 0.55f;
        float spacing = btnHeight + 20f;
        
        buttonBounds = new Rectangle[5];
        
        // Botón Play
        if (btnPlayTex != null) {
            btnPlay = new Sprite(btnPlayTex);
            btnPlay.setSize(btnWidth, btnHeight);
            btnPlay.setPosition(btnX, startY);
            buttonBounds[0] = new Rectangle(btnX, startY, btnWidth, btnHeight);
        }
        
        // Botón Deck
        if (btnDeckTex != null) {
            btnDeck = new Sprite(btnDeckTex);
            btnDeck.setSize(btnWidth, btnHeight);
            btnDeck.setPosition(btnX, startY - spacing);
            buttonBounds[1] = new Rectangle(btnX, startY - spacing, btnWidth, btnHeight);
        }
        
        // Botón Bazaar
        if (btnBazaarTex != null) {
            btnBazaar = new Sprite(btnBazaarTex);
            btnBazaar.setSize(btnWidth, btnHeight);
            btnBazaar.setPosition(btnX, startY - spacing * 2);
            buttonBounds[2] = new Rectangle(btnX, startY - spacing * 2, btnWidth, btnHeight);
        }
        
        // Botón Achievements
        if (btnAchievementsTex != null) {
            btnAchievements = new Sprite(btnAchievementsTex);
            btnAchievements.setSize(btnWidth, btnHeight);
            btnAchievements.setPosition(btnX, startY - spacing * 3);
            buttonBounds[3] = new Rectangle(btnX, startY - spacing * 3, btnWidth, btnHeight);
        }
        
        // Botón Settings
        if (btnSettingsTex != null) {
            btnSettings = new Sprite(btnSettingsTex);
            btnSettings.setSize(btnWidth, btnHeight);
            btnSettings.setPosition(btnX, startY - spacing * 4);
            buttonBounds[4] = new Rectangle(btnX, startY - spacing * 4, btnWidth, btnHeight);
        }
    }
    
    @Override
    protected void update(float delta) {
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);
            
            handleTouch(touchPos.x, touchPos.y);
        }
    }
    
    private void handleTouch(float x, float y) {
        if (buttonBounds[0] != null && buttonBounds[0].contains(x, y)) {
            Gdx.app.log("HomeScreen", "PLAY touched!");
            // TODO: Ir a LevelSelectScreen
        }
        else if (buttonBounds[1] != null && buttonBounds[1].contains(x, y)) {
            Gdx.app.log("HomeScreen", "DECK touched!");
            // TODO: Ir a DeckEditorScreen
        }
        else if (buttonBounds[2] != null && buttonBounds[2].contains(x, y)) {
            Gdx.app.log("HomeScreen", "BAZAAR touched!");
            // TODO: Ir a BazaarScreen
        }
        else if (buttonBounds[3] != null && buttonBounds[3].contains(x, y)) {
            Gdx.app.log("HomeScreen", "ACHIEVEMENTS touched!");
            // TODO: Ir a AchievementsScreen
        }
        else if (buttonBounds[4] != null && buttonBounds[4].contains(x, y)) {
            Gdx.app.log("HomeScreen", "SETTINGS touched!");
            // TODO: Ir a SettingsScreen
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Título
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(game.getBatch(), title, titleX, Constants.VIRTUAL_HEIGHT - 80f);
        
        // Dibujar botones
        if (btnPlay != null) btnPlay.draw(game.getBatch());
        if (btnDeck != null) btnDeck.draw(game.getBatch());
        if (btnBazaar != null) btnBazaar.draw(game.getBatch());
        if (btnAchievements != null) btnAchievements.draw(game.getBatch());
        if (btnSettings != null) btnSettings.draw(game.getBatch());
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (btnPlayTex != null) btnPlayTex.dispose();
        if (btnDeckTex != null) btnDeckTex.dispose();
        if (btnBazaarTex != null) btnBazaarTex.dispose();
        if (btnAchievementsTex != null) btnAchievementsTex.dispose();
        if (btnSettingsTex != null) btnSettingsTex.dispose();
        if (patternTex != null) patternTex.dispose();
        if (titleFont != null) titleFont.dispose();
    }
}