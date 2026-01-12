package com.darkphoenixteam.kawaiinekomemory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * Botón simple con detección de toques
 * 
 * @author DarkphoenixTeam
 */
public class SimpleButton {
    
    private Texture texture;
    private String text;
    private Rectangle bounds;
    private BitmapFont font;
    private GlyphLayout layout;
    
    // Callback cuando se presiona
    private Runnable onClick;
    
    // Estado
    private boolean wasPressed = false;
    
    public SimpleButton(Texture texture, String text, float x, float y, float width, float height) {
        this.texture = texture;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
    }
    
    /**
     * Actualiza el estado del botón
     * @param camera Para convertir coordenadas de pantalla a mundo
     */
    public void update(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        if (Gdx.input.isTouched()) {
            // Convertir coordenadas de pantalla a mundo
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            
            // Verificar si el toque está dentro del botón
            if (bounds.contains(touchPos.x, touchPos.y)) {
                if (!wasPressed) {
                    wasPressed = true;
                    triggerClick();
                }
            }
        } else {
            wasPressed = false;
        }
    }
    
    /**
     * Dibuja el botón
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        this.font = font;
        
        // Dibujar textura
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
        
        // Dibujar texto centrado
        if (text != null && font != null) {
            layout.setText(font, text);
            float textX = bounds.x + (bounds.width - layout.width) / 2f;
            float textY = bounds.y + (bounds.height + layout.height) / 2f;
            font.draw(batch, text, textX, textY);
        }
    }
    
    /**
     * Establece el callback de click
     */
    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
    
    private void triggerClick() {
        Gdx.app.log("SimpleButton", "Botón presionado: " + text);
        if (onClick != null) {
            onClick.run();
        }
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}