package com.darkphoenixteam.kawaiinekomemory.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Representa una carta individual en el tablero
 * Maneja estados, animación de volteo y renderizado
 * 
 * @author DarkphoenixTeam
 */
public class Card {
    
    /**
     * Estados posibles de la carta
     */
    public enum State {
        HIDDEN,             // Boca abajo, clickeable
        FLIPPING_TO_FRONT,  // Animación: volteando hacia frente
        REVEALED,           // Boca arriba, visible
        FLIPPING_TO_BACK,   // Animación: volteando hacia atrás
        MATCHED             // Emparejada, no clickeable
    }
    
    // === IDENTIFICACIÓN ===
    private int cardId;         // ID para matching (0-14)
    private int deckIndex;      // Índice del deck (0-4)
    private int cardIndex;      // Índice dentro del deck (0-6)
    private int nekoinValue;    // Valor en nekoins (1,2,3,5,7)
    
    // === ESTADO ===
    private State state;
    private boolean isClickable;
    
    // === POSICIÓN Y DIMENSIONES ===
    private float x, y;
    private float width, height;
    private Rectangle bounds;
    
    // === TEXTURAS ===
    private Texture frontTexture;
    private Texture backTexture;
    private boolean texturesOwned;  // true si esta carta debe disponer las texturas
    
    // === ANIMACIÓN DE FLIP ===
    private float flipProgress;     // 0.0 a 1.0
    private float flipDuration;     // Segundos para voltear
    private static final float DEFAULT_FLIP_DURATION = 0.3f;
    
    // === EFECTOS VISUALES ===
    private float shakeTimer;       // Para efecto de "hint"
    private float shakeIntensity;
    private float matchAlpha;       // Para fade out al hacer match
    private boolean isShaking;
    
    // === COLORES ===
    private static final Color COLOR_NORMAL = new Color(1f, 1f, 1f, 1f);
    private static final Color COLOR_MATCHED = new Color(1f, 1f, 1f, 0.3f);
    private static final Color COLOR_HINT = new Color(1f, 0.9f, 0.5f, 1f);
    
    /**
     * Constructor principal
     * 
     * @param cardId ID único para matching
     * @param frontTexture Textura del frente (imagen del personaje)
     * @param backTexture Textura del reverso (compartida)
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho de la carta
     * @param height Alto de la carta
     */
    public Card(int cardId, Texture frontTexture, Texture backTexture, 
                float x, float y, float width, float height) {
        this.cardId = cardId;
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
        
        // Estado inicial
        this.state = State.HIDDEN;
        this.isClickable = true;
        
        // Animación
        this.flipProgress = 0f;
        this.flipDuration = DEFAULT_FLIP_DURATION;
        
        // Efectos
        this.shakeTimer = 0f;
        this.shakeIntensity = 5f;
        this.matchAlpha = 1f;
        this.isShaking = false;
        
        // Por defecto, valor base
        this.nekoinValue = 1;
        this.texturesOwned = false;
    }
    
    // ==================== ACTUALIZACIÓN ====================
    
    /**
     * Actualiza la animación de la carta
     * @param delta Tiempo desde el último frame
     */
    public void update(float delta) {
        // Actualizar animación de flip
        if (state == State.FLIPPING_TO_FRONT || state == State.FLIPPING_TO_BACK) {
            flipProgress += delta / flipDuration;
            
            if (flipProgress >= 1f) {
                flipProgress = 0f;
                
                if (state == State.FLIPPING_TO_FRONT) {
                    state = State.REVEALED;
                } else {
                    state = State.HIDDEN;
                    isClickable = true;
                }
            }
        }
        
        // Actualizar shake (para hint)
        if (isShaking) {
            shakeTimer -= delta;
            if (shakeTimer <= 0) {
                isShaking = false;
                shakeTimer = 0f;
            }
        }
        
        // Actualizar fade out de cartas matched
        if (state == State.MATCHED && matchAlpha > 0.3f) {
            matchAlpha -= delta * 2f;  // Fade en 0.35 segundos
            if (matchAlpha < 0.3f) matchAlpha = 0.3f;
        }
    }
    
    // ==================== RENDERIZADO ====================
    
    /**
     * Dibuja la carta en su estado actual
     */
    public void draw(SpriteBatch batch) {
        if (state == State.MATCHED && matchAlpha <= 0.05f) {
            return;  // No dibujar si está casi invisible
        }
        
        // Calcular offset de shake
        float offsetX = 0f;
        if (isShaking) {
            offsetX = MathUtils.sin(shakeTimer * 50f) * shakeIntensity;
        }
        
        // Calcular escala horizontal para efecto de flip
        float scaleX = 1f;
        Texture textureToDraw = backTexture;
        
        if (state == State.FLIPPING_TO_FRONT) {
            // Primera mitad: se ve el reverso encogiendo
            if (flipProgress < 0.5f) {
                scaleX = 1f - (flipProgress * 2f);
                textureToDraw = backTexture;
            } 
            // Segunda mitad: se ve el frente expandiendo
            else {
                scaleX = (flipProgress - 0.5f) * 2f;
                textureToDraw = frontTexture;
            }
        } 
        else if (state == State.FLIPPING_TO_BACK) {
            // Primera mitad: se ve el frente encogiendo
            if (flipProgress < 0.5f) {
                scaleX = 1f - (flipProgress * 2f);
                textureToDraw = frontTexture;
            } 
            // Segunda mitad: se ve el reverso expandiendo
            else {
                scaleX = (flipProgress - 0.5f) * 2f;
                textureToDraw = backTexture;
            }
        }
        else if (state == State.REVEALED || state == State.MATCHED) {
            textureToDraw = frontTexture;
        }
        else {
            textureToDraw = backTexture;
        }
        
        // Aplicar alpha si está matched
        Color oldColor = batch.getColor().cpy();
        if (state == State.MATCHED) {
            batch.setColor(1f, 1f, 1f, matchAlpha);
        } else if (isShaking) {
            batch.setColor(COLOR_HINT);
        }
        
        // Calcular posición centrada con escala
        float drawWidth = width * Math.abs(scaleX);
        float drawX = x + offsetX + (width - drawWidth) / 2f;
        
        // Dibujar
        if (textureToDraw != null) {
            batch.draw(textureToDraw, drawX, y, drawWidth, height);
        }
        
        // Restaurar color
        batch.setColor(oldColor);
    }
    
    // ==================== ACCIONES ====================
    
    /**
     * Intenta voltear la carta hacia el frente
     * @return true si se pudo voltear
     */
    public boolean flip() {
        if (!isClickable || state != State.HIDDEN) {
            return false;
        }
        
        state = State.FLIPPING_TO_FRONT;
        flipProgress = 0f;
        isClickable = false;
        return true;
    }
    
    /**
     * Voltea la carta hacia atrás (no match)
     */
    public void flipBack() {
        if (state == State.REVEALED) {
            state = State.FLIPPING_TO_BACK;
            flipProgress = 0f;
        }
    }
    
    /**
     * Marca la carta como emparejada
     */
    public void setMatched() {
        state = State.MATCHED;
        isClickable = false;
    }
    
    /**
     * Activa el efecto de shake (para hint)
     * @param duration Duración del shake en segundos
     */
    public void startShake(float duration) {
        isShaking = true;
        shakeTimer = duration;
    }
    
    /**
     * Reinicia la carta a su estado inicial
     */
    public void reset() {
        state = State.HIDDEN;
        isClickable = true;
        flipProgress = 0f;
        matchAlpha = 1f;
        isShaking = false;
        shakeTimer = 0f;
    }
    
    // ==================== DETECCIÓN DE TOQUE ====================
    
    /**
     * Verifica si un punto está dentro de la carta
     */
    public boolean contains(float touchX, float touchY) {
        return bounds.contains(touchX, touchY);
    }
    
    /**
     * Verifica si la carta puede ser tocada
     */
    public boolean canBeClicked() {
        return isClickable && state == State.HIDDEN;
    }
    
    // ==================== POSICIÓN ====================
    
    /**
     * Actualiza la posición de la carta (para shuffle)
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.setPosition(x, y);
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public Rectangle getBounds() { return bounds; }
    
    // ==================== IDENTIFICACIÓN ====================
    
    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }
    
    public int getDeckIndex() { return deckIndex; }
    public void setDeckIndex(int deckIndex) { this.deckIndex = deckIndex; }
    
    public int getCardIndex() { return cardIndex; }
    public void setCardIndex(int cardIndex) { this.cardIndex = cardIndex; }
    
    public int getNekoinValue() { return nekoinValue; }
    public void setNekoinValue(int nekoinValue) { this.nekoinValue = nekoinValue; }
    
    // ==================== ESTADO ====================
    
    public State getState() { return state; }
    
    public boolean isRevealed() { 
        return state == State.REVEALED; 
    }
    
    public boolean isAnimating() {
        return state == State.FLIPPING_TO_FRONT || state == State.FLIPPING_TO_BACK;
    }
    
    public boolean isMatched() {
        return state == State.MATCHED;
    }
    
    public boolean isHidden() {
        return state == State.HIDDEN;
    }
    
    // ==================== TEXTURAS ====================
    
    public Texture getFrontTexture() { return frontTexture; }
    public Texture getBackTexture() { return backTexture; }
    
    public void setFrontTexture(Texture texture) { 
        this.frontTexture = texture; 
    }
    
    public void setTexturesOwned(boolean owned) {
        this.texturesOwned = owned;
    }
    
    /**
     * Libera recursos si esta carta es dueña de las texturas
     */
    public void dispose() {
        if (texturesOwned) {
            if (frontTexture != null) frontTexture.dispose();
            // backTexture es compartida, no la disponemos aquí
        }
    }
    
    @Override
    public String toString() {
        return "Card[id=" + cardId + ", state=" + state + ", pos=(" + x + "," + y + ")]";
    }
}