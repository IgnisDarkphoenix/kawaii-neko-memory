package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;
import com.darkphoenixteam.kawaiinekomemory.systems.FontManager;

/**
 * Clase principal del juego Kawaii Neko Memory
 * 
 * @author DarkphoenixTeam
 */
public class KawaiiNekoMemory extends Game {
    
    public static final String TAG = "KawaiiNekoMemory";
    
    private SpriteBatch batch;
    private FontManager fontManager;
    
    public KawaiiNekoMemory() {
    }
    
    @Override
    public void create() {
        Gdx.app.log(TAG, "=== Kawaii Neko Memory v1.0.0 ===");
        Gdx.app.log(TAG, "DarkphoenixTeam");
        Gdx.app.log(TAG, "Screen: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        
        batch = new SpriteBatch();
        
        // Inicializar FontManager (por ahora sin fuente custom)
        fontManager = new FontManager();
        
        // Iniciar con SplashScreen
        setScreen(new SplashScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "=== Cerrando juego ===");
        
        if (batch != null) {
            batch.dispose();
        }
        
        if (fontManager != null) {
            fontManager.dispose();
        }
        
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public FontManager getFontManager() {
        return fontManager;
    }
}