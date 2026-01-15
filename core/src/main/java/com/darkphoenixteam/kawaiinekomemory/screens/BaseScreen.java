package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla base que todas las demás pantallas extienden
 * Incluye sistema de input delay para evitar ghost clicks en transiciones
 * 
 * @author DarkphoenixTeam
 */
public abstract class BaseScreen implements Screen {
    
    private static final String TAG = "BaseScreen";
    
    // === INPUT DELAY CONFIG ===
    /** Tiempo de espera antes de aceptar input al entrar a una pantalla */
    private static final float INPUT_DELAY_DURATION = 0.3f;
    
    /** Timer del delay de input */
    private float inputDelayTimer = 0f;
    
    /** Indica si el input está habilitado */
    private boolean inputEnabled = false;
    
    // === CORE ===
    protected final KawaiiNekoMemory game;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;
    
    // Color de fondo por defecto (rosa pastel kawaii)
    protected float bgRed = 0.98f;
    protected float bgGreen = 0.90f;
    protected float bgBlue = 0.95f;
    
    public BaseScreen(KawaiiNekoMemory game) {
        this.game = game;
        
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(
            Constants.VIRTUAL_WIDTH,
            Constants.VIRTUAL_HEIGHT,
            camera
        );
        
        camera.position.set(
            Constants.VIRTUAL_WIDTH / 2f,
            Constants.VIRTUAL_HEIGHT / 2f,
            0
        );
        camera.update();
        
        // Iniciar con input deshabilitado
        this.inputDelayTimer = INPUT_DELAY_DURATION;
        this.inputEnabled = false;
    }
    
    /**
     * Actualizar lógica del juego
     */
    protected abstract void update(float delta);
    
    /**
     * Dibujar en pantalla
     */
    protected abstract void draw();
    
    @Override
    public void render(float delta) {
        // === ACTUALIZAR INPUT DELAY ===
        if (!inputEnabled) {
            inputDelayTimer -= delta;
            if (inputDelayTimer <= 0) {
                inputEnabled = true;
                Gdx.app.log(TAG, "Input habilitado para: " + this.getClass().getSimpleName());
            }
        }
        
        // Limpiar pantalla
        Gdx.gl.glClearColor(bgRed, bgGreen, bgBlue, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Actualizar cámara
        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);
        
        // Actualizar y dibujar
        update(delta);
        draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(
            Constants.VIRTUAL_WIDTH / 2f,
            Constants.VIRTUAL_HEIGHT / 2f,
            0
        );
    }
    
    @Override
    public void show() {
        Gdx.app.log(TAG, "Show: " + this.getClass().getSimpleName() + 
                         " (input delay: " + (int)(INPUT_DELAY_DURATION * 1000) + "ms)");
        
        // Reiniciar delay cada vez que se muestra la pantalla
        inputDelayTimer = INPUT_DELAY_DURATION;
        inputEnabled = false;
    }
    
    @Override
    public void hide() {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void dispose() {}
    
    // === MÉTODOS PARA SUBCLASES ===
    
    /**
     * Verifica si el input está habilitado (pasó el delay de transición)
     * Las subclases deben verificar esto antes de procesar input
     * 
     * @return true si el input está habilitado
     */
    protected boolean isInputEnabled() {
        return inputEnabled;
    }
    
    /**
     * Obtiene el tiempo restante del delay de input
     * 
     * @return segundos restantes (0 si ya está habilitado)
     */
    protected float getInputDelayRemaining() {
        return Math.max(0, inputDelayTimer);
    }
    
    /**
     * Fuerza la habilitación inmediata del input
     * Usar con precaución
     */
    protected void forceEnableInput() {
        inputDelayTimer = 0;
        inputEnabled = true;
    }
    
    /**
     * Cambiar color de fondo
     */
    protected void setBackgroundColor(float r, float g, float b) {
        this.bgRed = r;
        this.bgGreen = g;
        this.bgBlue = b;
    }
    
    /**
     * Obtener viewport para detección de toques
     */
    public Viewport getViewport() {
        return viewport;
    }
}