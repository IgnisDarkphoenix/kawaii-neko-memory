package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;

/**
 * Pantalla principal del menú
 * Placeholder temporal hasta agregar assets
 * 
 * @author DarkphoenixTeam
 */
public class HomeScreen extends BaseScreen {
    
    private BitmapFont titleFont;
    private BitmapFont infoFont;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;
    
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        // Color de fondo rosa pastel
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Fuentes temporales
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.valueOf("FF69B4"));
        
        infoFont = new BitmapFont();
        infoFont.getData().setScale(2f);
        infoFont.setColor(Color.DARK_GRAY);
        
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        
        // Mostrar banner si hay ads
        if (game.hasAdController()) {
            game.getAdController().showBanner();
        }
        
        Gdx.app.log("HomeScreen", "HomeScreen initialized successfully!");
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        // Detectar touch para feedback
        if (Gdx.input.justTouched()) {
            Gdx.app.log("HomeScreen", "Touch detected at: " + 
                Gdx.input.getX() + ", " + Gdx.input.getY());
        }
    }
    
    @Override
    protected void draw() {
        // Dibujar decoración animada
        drawDecoration();
        
        // Dibujar textos
        game.getBatch().begin();
        
        // Título con efecto de "respiración"
        float scale = 4f + (float) Math.sin(animTimer * 2) * 0.2f;
        titleFont.getData().setScale(scale);
        
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (KawaiiNekoMemory.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = KawaiiNekoMemory.VIRTUAL_HEIGHT - 150f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // Subtítulo
        infoFont.getData().setScale(2f);
        String subtitle = "~ Tap anywhere to test ~";
        layout.setText(infoFont, subtitle);
        float subX = (KawaiiNekoMemory.VIRTUAL_WIDTH - layout.width) / 2f;
        infoFont.draw(game.getBatch(), subtitle, subX, titleY - 80f);
        
        // Info de versión
        infoFont.getData().setScale(1.5f);
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(infoFont, version);
        float verX = (KawaiiNekoMemory.VIRTUAL_WIDTH - layout.width) / 2f;
        infoFont.draw(game.getBatch(), version, verX, 100f);
        
        // Estado del build
        String buildInfo = "Build OK! LibGDX Running";
        layout.setText(infoFont, buildInfo);
        float buildX = (KawaiiNekoMemory.VIRTUAL_WIDTH - layout.width) / 2f;
        infoFont.setColor(Color.valueOf("4CAF50"));
        infoFont.draw(game.getBatch(), buildInfo, buildX, 60f);
        infoFont.setColor(Color.DARK_GRAY);
        
        game.getBatch().end();
    }
    
    private void drawDecoration() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Dibujar círculos decorativos flotantes
        for (int i = 0; i < 5; i++) {
            float x = 80 + i * 90;
            float baseY = KawaiiNekoMemory.VIRTUAL_HEIGHT / 2f;
            float y = baseY + (float) Math.sin(animTimer * 1.5 + i) * 30f;
            float radius = 20 + (float) Math.sin(animTimer * 2 + i * 0.5) * 5f;
            
            // Color pastel alternado
            if (i % 2 == 0) {
                shapeRenderer.setColor(Color.valueOf("FFB6C1AA"));
            } else {
                shapeRenderer.setColor(Color.valueOf("87CEEBAA"));
            }
            
            shapeRenderer.circle(x, y, radius);
        }
        
        shapeRenderer.end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (infoFont != null) infoFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}