package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.ads.AdController;
import com.darkphoenixteam.kawaiinekomemory.screens.SplashScreen;

public class KawaiiNekoMemory extends Game {
    
    public SpriteBatch batch;
    public AdController adController;
    
    // Constructor sin ads (para testing)
    public KawaiiNekoMemory() {
        this(null);
    }
    
    // Constructor con ads
    public KawaiiNekoMemory(AdController adController) {
        this.adController = adController;
    }
    
    @Override
    public void create() {
        Gdx.app.log("KawaiiNeko", "Game starting...");
        
        batch = new SpriteBatch();
        
        // Iniciar con SplashScreen
        setScreen(new SplashScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
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