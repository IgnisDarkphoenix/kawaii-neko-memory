package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;

public class KawaiiNekoMemory extends Game {
    
    public SpriteBatch batch;
    
    @Override
    public void create() {
        try {
            Gdx.app.log("KawaiiNeko", "Game starting...");
            batch = new SpriteBatch();
            Gdx.app.log("KawaiiNeko", "SpriteBatch created");
            setScreen(new SplashScreen(this));
            Gdx.app.log("KawaiiNeko", "SplashScreen set");
        } catch (Exception e) {
            Gdx.app.error("KawaiiNeko", "Error in create(): " + e.getMessage());
        }
    }
    
    @Override
    public void render() {
        try {
            super.render();
        } catch (Exception e) {
            Gdx.app.error("KawaiiNeko", "Error in render(): " + e.getMessage());
        }
    }
    
    @Override
    public void dispose() {
        Gdx.app.log("KawaiiNeko", "Game disposing...");
        if (batch != null) {
            batch.dispose();
        }
        if (screen != null) {
            screen.dispose();
        }
    }
}