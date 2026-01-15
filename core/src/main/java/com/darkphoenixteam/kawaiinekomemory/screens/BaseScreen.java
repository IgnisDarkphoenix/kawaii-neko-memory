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
 * 
 * @author DarkphoenixTeam
 */
public abstract class BaseScreen implements Screen {
    
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
        Gdx.app.log("Screen", "Show: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void hide() {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void dispose() {}
    
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