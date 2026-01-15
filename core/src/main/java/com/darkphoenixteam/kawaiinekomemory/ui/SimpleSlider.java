package com.darkphoenixteam.kawaiinekomemory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Slider personalizado para controles de volumen
 * Usa 3 texturas: fondo, relleno, y knob
 * 
 * @author DarkphoenixTeam
 */
public class SimpleSlider {
    
    private static final String TAG = "SimpleSlider";
    
    // Texturas
    private Texture backgroundTexture;
    private Texture fillTexture;
    private Texture knobTexture;
    private TextureRegion fillRegion;
    
    // Bounds de la barra
    private Rectangle bounds;
    
    // Dimensiones del knob
    private float knobWidth;
    private float knobHeight;
    
    // Estado
    private float value;  // 0.0 a 1.0
    private boolean isDragging;
    
    // Callback
    private ValueChangedListener listener;
    
    // Vector reutilizable (evita GC)
    private final Vector2 touchPoint = new Vector2();
    
    /**
     * Interface para callback de cambio de valor
     */
    public interface ValueChangedListener {
        void onValueChanged(float newValue);
    }
    
    /**
     * Constructor
     * @param background Textura de la barra vacía (fondo)
     * @param fill Textura de la barra llena (se recorta según valor)
     * @param knob Textura del botón deslizante
     * @param x Posición X de la barra
     * @param y Posición Y de la barra
     * @param width Ancho de la barra
     * @param height Alto de la barra
     */
    public SimpleSlider(Texture background, Texture fill, Texture knob,
                        float x, float y, float width, float height) {
        this.backgroundTexture = background;
        this.fillTexture = fill;
        this.knobTexture = knob;
        
        this.bounds = new Rectangle(x, y, width, height);
        
        // Calcular dimensiones del knob
        if (knob != null) {
            float knobAspect = (float) knob.getWidth() / knob.getHeight();
            this.knobHeight = height * 1.6f;  // Knob más alto que la barra
            this.knobWidth = knobHeight * knobAspect;
        } else {
            // Fallback si no hay textura
            this.knobWidth = height * 0.8f;
            this.knobHeight = height * 1.6f;
        }
        
        // TextureRegion para poder recortar el fill
        if (fill != null) {
            this.fillRegion = new TextureRegion(fill);
        }
        
        // Valor inicial: máximo
        this.value = 1.0f;
        this.isDragging = false;
        
        Gdx.app.log(TAG, String.format("Creado: pos=(%.0f,%.0f) size=%.0fx%.0f knob=%.0fx%.0f",
                x, y, width, height, knobWidth, knobHeight));
    }
    
    /**
     * Actualiza el estado del slider (detecta arrastre)
     * @param viewport Viewport para convertir coordenadas de pantalla
     */
    public void update(Viewport viewport) {
        if (Gdx.input.isTouched()) {
            // Convertir coordenadas de pantalla a mundo
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            
            // Área de detección expandida (incluye el knob que sobresale)
            float expandedMinY = bounds.y - (knobHeight - bounds.height) / 2f;
            Rectangle touchArea = new Rectangle(
                bounds.x - knobWidth / 2f,           // Un poco a la izquierda
                expandedMinY,                         // Más abajo por el knob
                bounds.width + knobWidth,             // Un poco más ancha
                knobHeight                            // Altura del knob
            );
            
            // Verificar si está tocando el área o ya estaba arrastrando
            if (touchArea.contains(touchPoint.x, touchPoint.y) || isDragging) {
                isDragging = true;
                
                // Calcular nuevo valor basado en posición X relativa
                float relativeX = touchPoint.x - bounds.x;
                float newValue = MathUtils.clamp(relativeX / bounds.width, 0f, 1f);
                
                // Solo notificar si cambió significativamente
                if (Math.abs(newValue - value) > 0.005f) {
                    value = newValue;
                    
                    if (listener != null) {
                        listener.onValueChanged(value);
                    }
                }
            }
        } else {
            // Dejó de tocar
            isDragging = false;
        }
    }
    
    /**
     * Dibuja el slider (3 capas en orden)
     * @param batch SpriteBatch para dibujar
     */
    public void draw(SpriteBatch batch) {
        // === CAPA 1: FONDO ===
        if (backgroundTexture != null) {
            batch.draw(
                backgroundTexture,
                bounds.x, bounds.y,
                bounds.width, bounds.height
            );
        }
        
        // === CAPA 2: RELLENO (recortado según valor) ===
        if (fillTexture != null && value > 0) {
            // Ancho del relleno visible
            float fillWidth = bounds.width * value;
            
            // Recortar la textura para que no se estire
            int textureWidth = (int)(fillTexture.getWidth() * value);
            fillRegion.setRegion(0, 0, textureWidth, fillTexture.getHeight());
            
            batch.draw(
                fillRegion,
                bounds.x, bounds.y,
                fillWidth, bounds.height
            );
        }
        
        // === CAPA 3: KNOB ===
        if (knobTexture != null) {
            // Posición X del knob (centrado en el punto del valor)
            float knobX = bounds.x + (bounds.width * value) - (knobWidth / 2f);
            // Posición Y del knob (centrado verticalmente)
            float knobY = bounds.y + (bounds.height / 2f) - (knobHeight / 2f);
            
            batch.draw(
                knobTexture,
                knobX, knobY,
                knobWidth, knobHeight
            );
        }
    }
    
    /**
     * Establece el callback para cuando cambia el valor
     */
    public void setOnValueChanged(ValueChangedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Establece el valor programáticamente (sin trigger callback)
     * @param value Valor entre 0.0 y 1.0
     */
    public void setValue(float value) {
        this.value = MathUtils.clamp(value, 0f, 1f);
    }
    
    /**
     * Establece el valor Y dispara el callback
     * @param value Valor entre 0.0 y 1.0
     */
    public void setValueAndNotify(float value) {
        setValue(value);
        if (listener != null) {
            listener.onValueChanged(this.value);
        }
    }
    
    // ==================== GETTERS ====================
    
    public float getValue() {
        return value;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Obtiene el valor como porcentaje entero (0-100)
     */
    public int getPercentage() {
        return Math.round(value * 100);
    }
    
    /**
     * No hace dispose de texturas (pueden ser compartidas)
     * Las texturas se manejan externamente
     */
    public void dispose() {
        // Las texturas se comparten entre sliders, no hacer dispose aquí
    }
}