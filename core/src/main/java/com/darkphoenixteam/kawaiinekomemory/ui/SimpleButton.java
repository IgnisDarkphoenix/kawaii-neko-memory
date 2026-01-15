package com.darkphoenixteam.kawaiinekomemory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Botón simple con detección de toques
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
    
    // Vector reutilizable para evitar garbage collection
    private final Vector2 touchPoint = new Vector2();
    
    public SimpleButton(Texture texture, String text, float x, float y, float width, float height) {
        this.texture = texture;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
    }
    
    /**
     * Actualiza el estado del botón
     * @param viewport Para convertir coordenadas de pantalla a mundo
     */
    public void update(Viewport viewport) {
        isPressed = false;
        
        if (Gdx.input.isTouched()) {
            // Convertir coordenadas de pantalla a mundo usando viewport
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            
            // Verificar si el toque está dentro del botón
            if (bounds.contains(touchPoint.x, touchPoint.y)) {
                isPressed = true;
                
                // Solo disparar click una vez (cuando se presiona, no mientras se mantiene)
                if (!wasPressed) {
                    triggerClick();
                }
            }
        }
        
        wasPressed = isPressed;
    }
    
    /**
     * Dibuja el botón
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        // Efecto visual de presionado
        float scale = isPressed ? 0.95f : 1.0f;
        float offsetX = isPressed ? bounds.width * 0.025f : 0;
        float offsetY = isPressed ? bounds.height * 0.025f : 0;
        
        // Dibujar textura
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x + offsetX, 
                bounds.y + offsetY, 
                bounds.width * scale, 
                bounds.height * scale
            );
        }
        
        // Dibujar texto centrado
        if (text != null && font != null && !text.isEmpty()) {
            layout.setText(font, text);
            float textX = bounds.x + (bounds.width - layout.width) / 2f;
            float textY = bounds.y + (bounds.height + layout.height) / 2f;
            font.draw(batch, text, textX, textY);
        }
    }
    
    /**
     * Dibuja el botón SIN texto (para botones que ya tienen texto en la imagen)
     */
    public void drawNoText(SpriteBatch batch) {
        float scale = isPressed ? 0.95f : 1.0f;
        float offsetX = isPressed ? bounds.width * 0.025f : 0;
        float offsetY = isPressed ? bounds.height * 0.025f : 0;
        
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x + offsetX, 
                bounds.y + offsetY, 
                bounds.width * scale, 
                bounds.height * scale
            );
        }
    }
    
    /**
     * Establece el callback de click
     */
    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
    
    private void triggerClick() {
        Gdx.app.log("SimpleButton", "Click en: " + text);
        if (onClick != null) {
            onClick.run();
        }
    }
    
    // Getters
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isPressed() {
        return isPressed;
    }
    
    public Texture getTexture() {
        return texture;
    }
    
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}