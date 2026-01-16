package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.FontManager;

/**
 * Clase principal del juego - RESTAURADA
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
        
        batch = new SpriteBatch();
        
        // Inicializar FontManager
        fontManager = new FontManager();
        
        // Inicializar Audio
        AudioManager.getInstance().preloadSound(AssetPaths.SFX_BUTTON);
        
        // Iniciar con SplashScreen
        setScreen(new SplashScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void pause() {
        super.pause();
        AudioManager.getInstance().pauseMusic();
    }
    
    @Override
    public void resume() {
        super.resume();
        AudioManager.getInstance().resumeMusic();
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (fontManager != null) fontManager.dispose();
        AudioManager.getInstance().dispose();
        if (getScreen() != null) getScreen().dispose();
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public FontManager getFontManager() {
        return fontManager;
    }
}
