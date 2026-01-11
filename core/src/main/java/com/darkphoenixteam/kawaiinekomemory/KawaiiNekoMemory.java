package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;
import com.darkphoenixteam.kawaiinekomemory.systems.AdController;

/**
 * Clase principal del juego Kawaii Neko Memory
 * Extiende Game para manejar múltiples pantallas
 * 
 * @author DarkphoenixTeam
 */
public class KawaiiNekoMemory extends Game {
    
    public static final String TAG = "KawaiiNekoMemory";
    
    // Dimensiones virtuales del juego (base para escalado)
    public static final float VIRTUAL_WIDTH = 480f;
    public static final float VIRTUAL_HEIGHT = 800f;
    
    // SpriteBatch compartido para optimización
    private SpriteBatch batch;
    
    // Controlador de Ads (inyectado desde Android)
    private AdController adController;
    
    /**
     * Constructor con AdController para monetización
     * @param adController Controlador de anuncios (puede ser null para testing)
     */
    public KawaiiNekoMemory(AdController adController) {
        this.adController = adController;
    }
    
    /**
     * Constructor vacío para testing sin ads
     */
    public KawaiiNekoMemory() {
        this(null);
    }
    
    @Override
    public void create() {
        Gdx.app.log(TAG, "=== Kawaii Neko Memory Iniciando ===");
        Gdx.app.log(TAG, "Versión: 1.0.0");
        Gdx.app.log(TAG, "DarkphoenixTeam");
        Gdx.app.log(TAG, "Pantalla: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        
        // Crear SpriteBatch compartido
        batch = new SpriteBatch();
        
        // Iniciar con SplashScreen
        setScreen(new SplashScreen(this));
        
        Gdx.app.log(TAG, "Juego iniciado correctamente");
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "=== Kawaii Neko Memory Cerrando ===");
        
        if (batch != null) {
            batch.dispose();
        }
        
        // Dispose de la pantalla actual
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
    
    // === GETTERS ===
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public AdController getAdController() {
        return adController;
    }
    
    /**
     * Verifica si los ads están disponibles
     */
    public boolean hasAdController() {
        return adController != null;
    }
}