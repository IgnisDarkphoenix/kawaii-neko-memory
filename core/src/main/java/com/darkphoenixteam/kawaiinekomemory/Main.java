package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {
    
    private SpriteBatch batch;
    private BitmapFont font;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        
        Gdx.app.log("KawaiiNeko", "Game initialized successfully!");
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(1f, 0.8f, 0.9f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        font.draw(batch, "Kawaii Neko Memory", 100, Gdx.graphics.getHeight() / 2f + 50);
        font.draw(batch, "Build Success!", 100, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, "Touch to continue...", 100, Gdx.graphics.getHeight() / 2f - 50);
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}