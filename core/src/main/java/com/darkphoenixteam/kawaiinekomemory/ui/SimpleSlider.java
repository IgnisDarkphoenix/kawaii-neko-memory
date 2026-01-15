package com.darkphoenixteam.kawaiinekomemory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Slider personalizado para controles de volumen
 * Usa 3 texturas: fondo, relleno, y knob
 * Respeta aspect ratio de las texturas automáticamente
 * 
 * @author DarkphoenixTeam
 */
public class SimpleSlider {
    
    private static final String TAG = "SimpleSlider";
    
    // === KNOB SIZE CONFIG ===
    // Multiplicador del tamaño del knob respecto a la altura del slider
    private static final float KNOB_SIZE_MULTIPLIER = 0.9f;  // Era 1.8f, ahora la mitad
    
    // Texturas
    private Texture backgroundTexture;
    private Texture fillTexture;
    private Texture knobTexture;
    private TextureRegion fillRegion;
    
    // Bounds de la barra
    private Rectangle bounds;
    
    // Dimensiones del knob
    private float knobSize;
    
    // Estado
    private float value;
    private boolean isDragging;
    
    // Callback
    private ValueChangedListener listener;
    
    /**
     * Interface para callback de cambio de valor
     */
    public interface ValueChangedListener {
        void onValueChanged(float newValue);
    }
    
    /**
     * Constructor con cálculo automático de dimensiones
     * @param background Textura de la barra vacía (fondo)
     * @param fill Textura de la barra llena (se recorta según valor)
     * @param knob Textura del botón deslizante
     * @param x Posición X de la barra
     * @param y Posición Y de la barra
     * @param width Ancho deseado de la barra
     * @param maxHeight Altura máxima permitida
     */
    public SimpleSlider(Texture background, Texture fill, Texture knob,
                        float x, float y, float width, float maxHeight) {
        this.backgroundTexture = background;
        this.fillTexture = fill;
        this.knobTexture = knob;
        
        // Calcular altura respetando aspect ratio de la textura
        float height = maxHeight;
        if (background != null) {
            float textureRatio = (float) background.getHeight() / (float) background.getWidth();
            float idealHeight = width * textureRatio;
            height = Math.min(idealHeight, maxHeight);
            
            Gdx.app.log(TAG, String.format(
                "Slider: textura %dx%d, ratio=%.3f, altura=%.1f",
                background.getWidth(), background.getHeight(), 
                textureRatio, height
            ));
        }
        
        this.bounds = new Rectangle(x, y, width, height);
        
        // Knob cuadrado, proporcional a la altura del slider
        this.knobSize = height * KNOB_SIZE_MULTIPLIER;
        
        // TextureRegion para poder recortar el fill
        if (fill != null) {
            this.fillRegion = new TextureRegion(fill);
        }
        
        // Valor inicial: máximo
        this.value = 1.0f;
        this.isDragging = false;
        
        Gdx.app.log(TAG, String.format(
            "Creado: pos=(%.0f,%.0f) size=%.0fx%.0f knob=%.0f",
            x, y, bounds.width, bounds.height, knobSize
        ));
    }
    
    /**
     * Actualiza el estado del slider
     * @param touchPoint Coordenadas del mundo (ya convertidas con unproject)
     * @param isTouched Si el usuario está tocando la pantalla
     */
    public void update(Vector2 touchPoint, boolean isTouched) {
        if (isTouched) {
            // Área de detección (incluye margen para el knob)
            float touchPadding = knobSize * 0.5f;
            Rectangle touchArea = new Rectangle(
                bounds.x - touchPadding,
                bounds.y - touchPadding,
                bounds.width + (touchPadding * 2f),
                bounds.height + (touchPadding * 2f)
            );
            
            if (touchArea.contains(touchPoint.x, touchPoint.y) || isDragging) {
                if (!isDragging) {
                    Gdx.app.log(TAG, "Iniciando arrastre");
                }
                isDragging = true;
                
                // Calcular nuevo valor
                float relativeX = touchPoint.x - bounds.x;
                float newValue = MathUtils.clamp(relativeX / bounds.width, 0f, 1f);
                
                if (Math.abs(newValue - value) > 0.005f) {
                    value = newValue;
                    
                    if (listener != null) {
                        listener.onValueChanged(value);
                    }
                }
            }
        } else {
            if (isDragging) {
                Gdx.app.log(TAG, "Fin arrastre - valor: " + getPercentage() + "%");
            }
            isDragging = false;
        }
    }
    
    /**
     * Dibuja el slider (3 capas)
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
        
        // === CAPA 2: RELLENO ===
        if (fillTexture != null && value > 0.01f) {
            float fillWidth = bounds.width * value;
            int textureCropWidth = (int)(fillTexture.getWidth() * value);
            fillRegion.setRegion(0, 0, textureCropWidth, fillTexture.getHeight());
            
            batch.draw(
                fillRegion,
                bounds.x, bounds.y,
                fillWidth, bounds.height
            );
        }
        
        // === CAPA 3: KNOB ===
        if (knobTexture != null) {
            float knobX = bounds.x + (bounds.width * value) - (knobSize / 2f);
            float knobY = bounds.y + (bounds.height / 2f) - (knobSize / 2f);
            
            // Efecto visual al arrastrar
            float scale = isDragging ? 1.15f : 1.0f;
            float scaledSize = knobSize * scale;
            float offset = (knobSize - scaledSize) / 2f;
            
            batch.draw(
                knobTexture,
                knobX + offset, knobY + offset,
                scaledSize, scaledSize
            );
        }
    }
    
    /**
     * Establece el callback
     */
    public void setOnValueChanged(ValueChangedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Establece el valor (sin callback)
     */
    public void setValue(float value) {
        this.value = MathUtils.clamp(value, 0f, 1f);
    }
    
    /**
     * Establece el valor Y dispara callback
     */
    public void setValueAndNotify(float value) {
        setValue(value);
        if (listener != null) {
            listener.onValueChanged(this.value);
        }
    }
    
    // === GETTERS ===
    
    public float getValue() {
        return value;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public float getKnobSize() {
        return knobSize;
    }
    
    public int getPercentage() {
        return Math.round(value * 100);
    }
    
    public void dispose() {
        // Texturas manejadas externamente
    }
}