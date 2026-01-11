package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class Main extends ApplicationAdapter {
    
    private float timer = 0f;
    private boolean isPink = false;
    
    @Override
    public void create() {
        Gdx.app.log("Main", "Game created successfully!");
    }
    
    @Override
    public void render() {
        timer += Gdx.graphics.getDeltaTime();
        
        // Cambiar color después de 3 segundos
        if (timer >= 3f && !isPink) {
            isPink = true;
            Gdx.app.log("Main", "Switching to pink!");
        }
        
        // Color de fondo
        if (isPink) {
            // Rosa
            Gdx.gl.glClearColor(1f, 0.85f, 0.9f, 1f);
        } else {
            // Oscuro
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        }
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log("Main", "Game disposed!");
    }
}