package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;

public class HomeScreen extends BaseScreen {
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        // Fondo rosa
        setBackgroundColor(1f, 0.85f, 0.9f);
        Gdx.app.log("HomeScreen", "Created successfully");
    }
    
    @Override
    public void render(float delta) {
        super.render(delta);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("HomeScreen", "Disposed");
    }
}