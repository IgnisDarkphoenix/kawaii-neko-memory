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
 * Pantalla base con sistema de input delay y debounce global
 * 
 * @author DarkphoenixTeam
 */
public abstract class BaseScreen implements Screen {
    
    private static final String TAG = "BaseScreen";
    
    // === INPUT DELAY CONFIG ===
    private static final float INPUT_DELAY_DURATION = 0.3f;
    private float inputDelayTimer = 0f;
    private boolean inputEnabled = false;
    
    // === DEBOUNCE GLOBAL ===
    private static final float GLOBAL_TAP_COOLDOWN = 0.2f;
    private float tapCooldownTimer = 0f;
    private boolean wasTouchedLastFrame = false;
    
    // === CORE ===
    protected final KawaiiNekoMemory game;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;
    
    // Color de fondo
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
        
        this.inputDelayTimer = INPUT_DELAY_DURATION;
        this.inputEnabled = false;
    }
    
    protected abstract void update(float delta);
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
        
        // === ACTUALIZAR DEBOUNCE GLOBAL ===
        if (tapCooldownTimer > 0) {
            tapCooldownTimer -= delta;
        }
        
        // Detectar nuevo toque (anti multi-tap)
        boolean isTouched = Gdx.input.isTouched();
        if (isTouched && !wasTouchedLastFrame) {
            if (tapCooldownTimer <= 0) {
                tapCooldownTimer = GLOBAL_TAP_COOLDOWN;
            }
        }
        wasTouchedLastFrame = isTouched;
        
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
        Gdx.app.log(TAG, "Show: " + this.getClass().getSimpleName());
        inputDelayTimer = INPUT_DELAY_DURATION;
        inputEnabled = false;
        tapCooldownTimer = GLOBAL_TAP_COOLDOWN;
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
    
    protected boolean isInputEnabled() {
        return inputEnabled;
    }
    
    /**
     * Verifica si se puede procesar un nuevo toque
     * Combina input delay + debounce global
     */
    protected boolean canProcessTouch() {
        return inputEnabled && tapCooldownTimer <= 0;
    }
    
    /**
     * Consume el toque actual (resetea cooldown)
     */
    protected void consumeTouch() {
        tapCooldownTimer = GLOBAL_TAP_COOLDOWN;
    }
    
    protected float getInputDelayRemaining() {
        return Math.max(0, inputDelayTimer);
    }
    
    protected void forceEnableInput() {
        inputDelayTimer = 0;
        inputEnabled = true;
    }
    
    protected void setBackgroundColor(float r, float g, float b) {
        this.bgRed = r;
        this.bgGreen = g;
        this.bgBlue = b;
    }
    
    public Viewport getViewport() {
        return viewport;
    }
}
