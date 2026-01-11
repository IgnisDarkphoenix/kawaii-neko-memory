package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;

/**
 * Pantalla base que todas las demás pantallas extienden
 * Maneja la cámara, viewport y ciclo de vida básico
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
        
        // Configurar cámara
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(
            KawaiiNekoMemory.VIRTUAL_WIDTH,
            KawaiiNekoMemory.VIRTUAL_HEIGHT,
            camera
        );
        
        // Centrar cámara
        camera.position.set(
            KawaiiNekoMemory.VIRTUAL_WIDTH / 2f,
            KawaiiNekoMemory.VIRTUAL_HEIGHT / 2f,
            0
        );
        camera.update();
    }
    
    /**
     * Llamado cada frame para actualizar lógica
     * @param delta Tiempo desde el último frame
     */
    protected abstract void update(float delta);
    
    /**
     * Llamado cada frame para dibujar
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
        
        // Actualizar lógica
        update(delta);
        
        // Dibujar
        draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(
            KawaiiNekoMemory.VIRTUAL_WIDTH / 2f,
            KawaiiNekoMemory.VIRTUAL_HEIGHT / 2f,
            0
        );
    }
    
    @Override
    public void show() {
        Gdx.app.log("Screen", "Showing: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void hide() {
        Gdx.app.log("Screen", "Hiding: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void dispose() {}
    
    /**
     * Cambia el color de fondo
     */
    protected void setBackgroundColor(float r, float g, float b) {
        this.bgRed = r;
        this.bgGreen = g;
        this.bgBlue = b;
    }
}