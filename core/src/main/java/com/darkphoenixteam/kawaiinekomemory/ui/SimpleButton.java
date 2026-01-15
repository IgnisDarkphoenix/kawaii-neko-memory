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
 * Botón simple con detección de toques y aspect ratio automático
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
    
    /**
     * Constructor que calcula automáticamente el height basado en aspect ratio de la textura
     * @param texture Textura del botón
     * @param text Texto a mostrar (puede ser vacío)
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho deseado (height se calcula automáticamente)
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width) {
        this.texture = texture;
        this.text = text;
        this.layout = new GlyphLayout();
        
        // Calcular height manteniendo aspect ratio de la textura
        float height = width;
        if (texture != null) {
            float aspectRatio = (float) texture.getHeight() / (float) texture.getWidth();
            height = width * aspectRatio;
            Gdx.app.log("SimpleButton", String.format(
                "Textura: %dx%d, Ratio: %.2f, Renderizado: %.0fx%.0f",
                texture.getWidth(), texture.getHeight(), aspectRatio, width, height
            ));
        }
        
        this.bounds = new Rectangle(x, y, width, height);
    }
    
    /**
     * Constructor legacy con width y height manual (puede deformar)
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width, float height) {
        this.texture = texture;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
        
        if (texture != null) {
            float textureRatio = (float) texture.getHeight() / (float) texture.getWidth();
            float renderRatio = height / width;
            if (Math.abs(textureRatio - renderRatio) > 0.01f) {
                Gdx.app.log("SimpleButton", "WARNING: Aspect ratio mismatch! " +
                    "Texture=" + textureRatio + " Render=" + renderRatio);
            }
        }
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
     * Dibuja el botón con efecto de presión
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        // Efecto visual de presionado (5% más pequeño)
        float scale = isPressed ? 0.95f : 1.0f;
        float scaledWidth = bounds.width * scale;
        float scaledHeight = bounds.height * scale;
        float offsetX = (bounds.width - scaledWidth) / 2f;
        float offsetY = (bounds.height - scaledHeight) / 2f;
        
        // Dibujar textura
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x + offsetX, 
                bounds.y + offsetY, 
                scaledWidth, 
                scaledHeight
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
     * Dibuja el botón SIN texto (para imágenes con texto integrado)
     */
    public void drawNoText(SpriteBatch batch) {
        float scale = isPressed ? 0.95f : 1.0f;
        float scaledWidth = bounds.width * scale;
        float scaledHeight = bounds.height * scale;
        float offsetX = (bounds.width - scaledWidth) / 2f;
        float offsetY = (bounds.height - scaledHeight) / 2f;
        
        if (texture != null) {
            batch.draw(
                texture, 
                bounds.x + offsetX, 
                bounds.y + offsetY, 
                scaledWidth, 
                scaledHeight
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
        Gdx.app.log("SimpleButton", "Click en: " + (text.isEmpty() ? "botón" : text));
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
    
    public float getWidth() {
        return bounds.width;
    }
    
    public float getHeight() {
        return bounds.height;
    }
    
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}