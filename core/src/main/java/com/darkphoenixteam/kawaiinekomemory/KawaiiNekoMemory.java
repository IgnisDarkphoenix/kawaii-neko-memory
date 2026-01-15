package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
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
        
        // Inicializar AudioManager y reproducir música del menú
        Gdx.app.log(TAG, "Inicializando sistema de audio...");
        AudioManager.getInstance().playMusic(AssetPaths.MUSIC_MENU, true);
        
        // Precargar sonidos frecuentes
        AudioManager.getInstance().preloadSound(AssetPaths.SFX_BUTTON);
        AudioManager.getInstance().preloadSound(AssetPaths.SFX_CARD_FLIP);
        AudioManager.getInstance().preloadSound(AssetPaths.SFX_MATCH);
        
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
        // Pausar música cuando la app pasa a segundo plano
        AudioManager.getInstance().pauseMusic();
        Gdx.app.log(TAG, "App pausada - música pausada");
    }
    
    @Override
    public void resume() {
        super.resume();
        // Reanudar música cuando la app vuelve a primer plano
        AudioManager.getInstance().resumeMusic();
        Gdx.app.log(TAG, "App resumida - música reanudada");
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
        
        // Liberar recursos de audio
        AudioManager.getInstance().dispose();
        
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