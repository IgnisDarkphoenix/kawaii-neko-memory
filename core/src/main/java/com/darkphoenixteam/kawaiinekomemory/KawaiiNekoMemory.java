package com.darkphoenixteam.kawaiinekomemory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.systems.AdController;

/**
 * Versión de diagnóstico - Mínimo posible
 * 
 * @author DarkphoenixTeam
 */
public class KawaiiNekoMemory extends ApplicationAdapter {
    
    public static final String TAG = "KawaiiNekoMemory";
    
    private SpriteBatch batch;
    private BitmapFont font;
    private AdController adController;
    
    private float timer = 0f;
    private String status = "Iniciando...";
    
    public KawaiiNekoMemory(AdController adController) {
        this.adController = adController;
    }
    
    public KawaiiNekoMemory() {
        this(null);
    }
    
    @Override
    public void create() {
        try {
            Gdx.app.log(TAG, "=== CREATE INICIADO ===");
            
            batch = new SpriteBatch();
            Gdx.app.log(TAG, "SpriteBatch creado");
            
            font = new BitmapFont();
            font.getData().setScale(2f);
            Gdx.app.log(TAG, "BitmapFont creado");
            
            status = "OK - Todo funciona!";
            Gdx.app.log(TAG, "=== CREATE COMPLETADO ===");
            
        } catch (Exception e) {
            status = "ERROR: " + e.getMessage();
            Gdx.app.error(TAG, "Error en create()", e);
        }
    }
    
    @Override
    public void render() {
        try {
            // Incrementar timer
            timer += Gdx.graphics.getDeltaTime();
            
            // Limpiar pantalla con color verde (señal de vida)
            Gdx.gl.glClearColor(0.2f, 0.8f, 0.3f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            // Dibujar texto
            batch.begin();
            
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, "Kawaii Neko Memory", 50, 700);
            font.draw(batch, "DarkphoenixTeam", 50, 650);
            font.draw(batch, "-------------------", 50, 600);
            font.draw(batch, "Status: " + status, 50, 550);
            font.draw(batch, "Timer: " + String.format("%.1f", timer) + "s", 50, 500);
            font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 50, 450);
            font.draw(batch, "Screen: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight(), 50, 400);
            
            // Indicador visual animado
            String dots = "";
            int numDots = ((int) timer) % 4;
            for (int i = 0; i < numDots; i++) dots += ".";
            font.draw(batch, "Running" + dots, 50, 300);
            
            batch.end();
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error en render()", e);
        }
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
    
    // Getters para compatibilidad
    public SpriteBatch getBatch() { return batch; }
    public AdController getAdController() { return adController; }
    public boolean hasAdController() { return adController != null; }
}