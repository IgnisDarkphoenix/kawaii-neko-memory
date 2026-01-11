package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class Main extends ApplicationAdapter {
    
    @Override
    public void create() {
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(1f, 0.8f, 0.9f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}