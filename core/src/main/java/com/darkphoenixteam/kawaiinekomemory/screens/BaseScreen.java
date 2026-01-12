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
 * Pantalla base - todas las pantallas extienden de esta
 * 
 * @author DarkphoenixTeam
 */
public abstract class BaseScreen implements Screen {
    
    protected final KawaiiNekoMemory game;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;
    
    // Color de fondo (rosa pastel por defecto)
    protected float bgR = 1f;
    protected float bgG = 0.92f;
    protected float bgB = 0.95f;
    
    public BaseScreen(KawaiiNekoMemory game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(
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
    
    protected abstract void update(float delta);
    protected abstract void draw();
    
    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(bgR, bgG, bgB, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Actualizar cámara
        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);
        
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
        Gdx.app.log("Screen", "Show: " + getClass().getSimpleName());
    }
    
    @Override
    public void hide() {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void dispose() {}
    
    protected void setBackgroundColor(float r, float g, float b) {
        bgR = r;
        bgG = g;
        bgB = b;
    }
}