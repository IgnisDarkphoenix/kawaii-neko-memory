package com.darkphoenixteam.kawaiinekomemory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Botón simple con detección de toques
 * SIN scaling manual - usa tint + offset para feedback visual
 * 
 * @author DarkphoenixTeam
 */
public class SimpleButton {
    
    private Texture texture;
    private String text;
    private Rectangle bounds;
    private GlyphLayout layout;
    
    // Callback
    private Runnable onClick;
    
    // Estado del toque
    private boolean isPressed = false;
    private boolean wasPressed = false;
    
    // Vector reutilizable
    private final Vector2 touchPoint = new Vector2();
    
    // Colores para estados
    private static final Color COLOR_NORMAL = new Color(1f, 1f, 1f, 1f);
    private static final Color COLOR_PRESSED = new Color(0.85f, 0.85f, 0.85f, 1f);
    
    // Offset visual al presionar (simula "hundimiento")
    private static final float PRESS_OFFSET_Y = -4f;
    
    /**
     * Constructor con aspect ratio automático
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width) {
        this.texture = texture;
        this.text = text;
        this.layout = new GlyphLayout();
        
        // Calcular height manteniendo aspect ratio
        float height = width;
        if (texture != null) {
            float aspectRatio = (float) texture.getHeight() / (float) texture.getWidth();
            height = width * aspectRatio;
        }
        
        this.bounds = new Rectangle(x, y, width, height);
    }
    
    /**
     * Constructor legacy con dimensiones manuales
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width, float height) {
        this.texture = texture;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
    }
    
    /**
     * Actualiza el estado del botón
     */
    public void update(Viewport viewport) {
        isPressed = false;
        
        if (Gdx.input.isTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            
            if (bounds.contains(touchPoint.x, touchPoint.y)) {
                isPressed = true;
                
                if (!wasPressed) {
                    triggerClick();
                }
            }
        }
        
        wasPressed = isPressed;
    }
    
    /**
     * Dibuja el botón CON texto
     * Usa tint + offset en lugar de scaling
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        // Calcular offset visual
        float offsetY = isPressed ? PRESS_OFFSET_Y : 0f;
        
        // Aplicar tint según estado
        Color oldColor = batch.getColor().cpy();
        batch.setColor(isPressed ? COLOR_PRESSED : COLOR_NORMAL);
        
        // Dibujar textura (sin scaling, solo offset)
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x, 
                bounds.y + offsetY, 
                bounds.width, 
                bounds.height
            );
        }
        
        // Restaurar color
        batch.setColor(oldColor);
        
        // Dibujar texto centrado (con mismo offset)
        if (text != null && font != null && !text.isEmpty()) {
            layout.setText(font, text);
            float textX = bounds.x + (bounds.width - layout.width) / 2f;
            float textY = bounds.y + offsetY + (bounds.height + layout.height) / 2f;
            font.draw(batch, text, textX, textY);
        }
    }
    
    /**
     * Dibuja el botón SIN texto
     */
    public void drawNoText(SpriteBatch batch) {
        float offsetY = isPressed ? PRESS_OFFSET_Y : 0f;
        
        Color oldColor = batch.getColor().cpy();
        batch.setColor(isPressed ? COLOR_PRESSED : COLOR_NORMAL);
        
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x, 
                bounds.y + offsetY, 
                bounds.width, 
                bounds.height
            );
        }
        
        batch.setColor(oldColor);
    }
    
    /**
     * Establece el callback de click
     */
    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
    
    private void triggerClick() {
        Gdx.app.log("SimpleButton", "Click: " + (text != null && !text.isEmpty() ? text : "botón"));
        if (onClick != null) {
            onClick.run();
        }
    }
    
    // Getters
    public Rectangle getBounds() { return bounds; }
    public boolean isPressed() { return isPressed; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }
    
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}