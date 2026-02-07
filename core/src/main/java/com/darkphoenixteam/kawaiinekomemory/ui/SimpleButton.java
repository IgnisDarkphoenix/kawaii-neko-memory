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
 * Botón simple con detección de toques y sistema de debounce
 * Previene múltiples clicks accidentales
 * 
 * @author DarkphoenixTeam
 */
public class SimpleButton {
    
    private static final String TAG = "SimpleButton";
    
    // === DEBOUNCE CONFIG ===
    private static final float DEFAULT_COOLDOWN = 0.5f;
    private float cooldownTimer = 0f;
    
    // Texturas y bounds
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
    private static final Color COLOR_COOLDOWN = new Color(0.7f, 0.7f, 0.7f, 0.8f);
    
    // Offset visual al presionar
    private static final float PRESS_OFFSET_Y = -4f;
    
    /**
     * Constructor con aspect ratio automático
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width) {
        this.texture = texture;
        this.text = text;
        this.layout = new GlyphLayout();
        
        float height = width;
        if (texture != null) {
            float aspectRatio = (float) texture.getHeight() / (float) texture.getWidth();
            height = width * aspectRatio;
        }
        
        this.bounds = new Rectangle(x, y, width, height);
    }
    
    /**
     * Constructor con dimensiones manuales
     */
    public SimpleButton(Texture texture, String text, float x, float y, float width, float height) {
        this.texture = texture;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
    }
    
    /**
     * Actualiza el estado del botón con sistema de debounce
     */
    public void update(Viewport viewport) {
        float delta = Gdx.graphics.getDeltaTime();
        
        if (cooldownTimer > 0) {
            cooldownTimer -= delta;
            isPressed = false;
            wasPressed = false;
            return;
        }
        
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
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        float offsetY = isPressed ? PRESS_OFFSET_Y : 0f;
        
        Color oldColor = batch.getColor().cpy();
        if (cooldownTimer > 0) {
            batch.setColor(COLOR_COOLDOWN);
        } else if (isPressed) {
            batch.setColor(COLOR_PRESSED);
        } else {
            batch.setColor(COLOR_NORMAL);
        }
        
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
        if (cooldownTimer > 0) {
            batch.setColor(COLOR_COOLDOWN);
        } else if (isPressed) {
            batch.setColor(COLOR_PRESSED);
        } else {
            batch.setColor(COLOR_NORMAL);
        }
        
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
    
    /**
     * Dispara el evento de click y activa el cooldown
     */
    private void triggerClick() {
        cooldownTimer = DEFAULT_COOLDOWN;
        
        Gdx.app.log(TAG, "Click: " + (text != null && !text.isEmpty() ? text : "botón") + 
                         " (cooldown " + (int)(DEFAULT_COOLDOWN * 1000) + "ms)");
        
        if (onClick != null) {
            onClick.run();
        }
    }
    
    /**
     * Fuerza el reset del cooldown
     */
    public void resetCooldown() {
        cooldownTimer = 0f;
    }
    
    /**
     * Verifica si el botón está en cooldown
     */
    public boolean isOnCooldown() {
        return cooldownTimer > 0;
    }
    
    /**
     * Establece un cooldown personalizado
     */
    public void setCooldown(float seconds) {
        cooldownTimer = seconds;
    }
    
    // === GETTERS ===
    
    public Rectangle getBounds() { 
        return bounds; 
    }
    
    public boolean isPressed() { 
        return isPressed; 
    }
    
    public float getX() {
        return bounds.x;
    }
    
    public float getY() {
        return bounds.y;
    }
    
    public float getWidth() { 
        return bounds.width; 
    }
    
    public float getHeight() { 
        return bounds.height; 
    }
    
    public float getCooldownRemaining() {
        return cooldownTimer;
    }
    
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
