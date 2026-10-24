package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.screens.BaseScreen;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.FontManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SharedAssetManager;

/**
 * Clase principal del juego Kawaii Neko Memory
 * 
 * @author DarkphoenixTeam
 * @version 1.1 - SharedAssetManager integrado
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
        
        // Inicializar FontManager
        fontManager = new FontManager();
        
        // === INICIALIZAR SHARED ASSET MANAGER ===
        SharedAssetManager sharedAssets = SharedAssetManager.getInstance();
        sharedAssets.preloadCommon();
        Gdx.app.log(TAG, "SharedAssetManager inicializado - " + sharedAssets.getMemoryUsage());
        
        // === INICIALIZAR AUDIO ===
        AudioManager audioManager = AudioManager.getInstance();
        
        // Precargar sonidos frecuentes
        audioManager.preloadSound(AssetPaths.SFX_BUTTON);
        audioManager.preloadSound(AssetPaths.SFX_CARD_FLIP);
        audioManager.preloadSound(AssetPaths.SFX_MATCH);
        
        // Reproducir música del menú
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        Gdx.app.log(TAG, "AudioManager inicializado - Música: " + 
                   (int)(audioManager.getMusicVolume() * 100) + "% | SFX: " + 
                   (int)(audioManager.getSoundVolume() * 100) + "%");
        
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
        
        // IMPORTANTE: sin flush() las SharedPreferences no se escriben a disco.
        // Android puede matar el proceso en background sin avisar.
        SaveManager.getInstance().flushNow();
        
        Gdx.app.log(TAG, "App pausada - música pausada + progreso guardado");
    }
    
    @Override
    public void resume() {
        super.resume();
        
        // Si la pantalla actual está en pausa intencional (menú de pausa),
        // la música no debe volver sola.
        boolean resumeMusic = true;
        Screen current = getScreen();
        if (current instanceof BaseScreen) {
            resumeMusic = ((BaseScreen) current).shouldResumeMusicAfterAppPause();
        }
        
        if (resumeMusic) {
            AudioManager.getInstance().resumeMusic();
        }
        Gdx.app.log(TAG, "App resumida - música: " + (resumeMusic ? "reanudada" : "en pausa"));
    }
    
    /**
     * Cambia de pantalla liberando la anterior.
     * 
     * libGDX NO llama dispose() al cambiar de pantalla: si no lo hacemos aquí,
     * las texturas de la pantalla anterior quedan en VRAM para siempre.
     * 
     * @param next Pantalla nueva
     */
    public void changeScreen(final Screen next) {
        final Screen previous = getScreen();
        
        // Se hace al final del frame: si liberáramos la pantalla dentro de su
        // propio update(), su draw() correría con texturas ya destruidas.
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setScreen(next);
                if (previous != null && previous != next) {
                    try {
                        previous.dispose();
                    } catch (Exception e) {
                        Gdx.app.error(TAG, "Error liberando pantalla anterior", e);
                    }
                }
            }
        });
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "=== Cerrando juego ===");
        
        // Guardar progreso pendiente antes de cerrar
        try {
            SaveManager.getInstance().flushNow();
        } catch (Exception e) {
            Gdx.app.error(TAG, "No se pudo guardar al cerrar", e);
        }
        
        if (batch != null) {
            batch.dispose();
        }
        
        if (fontManager != null) {
            fontManager.dispose();
        }
        
        // Liberar recursos de audio
        AudioManager.getInstance().dispose();
        
        // Liberar SharedAssetManager
        SharedAssetManager.getInstance().dispose();
        
        if (getScreen() != null) {
            getScreen().dispose();
        }
        
        Gdx.app.log(TAG, "Recursos liberados correctamente");
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public FontManager getFontManager() {
        return fontManager;
    }
}
