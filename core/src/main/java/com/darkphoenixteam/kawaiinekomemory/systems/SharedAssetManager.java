package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;

/**
 * Gestor centralizado de assets compartidos
 * Implementa conteo de referencias para evitar duplicados
 * 
 * Uso:
 * - get(path): Obtiene textura (carga si no existe, incrementa ref)
 * - release(path): Decrementa referencia (libera si llega a 0)
 * - preloadCommon(): Carga assets frecuentes al inicio
 * 
 * @author DarkphoenixTeam
 * @version 1.0
 */
public class SharedAssetManager implements Disposable {
    
    private static final String TAG = "SharedAssetManager";
    
    // Singleton
    private static SharedAssetManager instance;
    
    // Cache de texturas con conteo de referencias
    private final ObjectMap<String, TextureEntry> textureCache;
    
    // Clase interna para tracking
    private static class TextureEntry {
        Texture texture;
        int refCount;
        
        TextureEntry(Texture texture) {
            this.texture = texture;
            this.refCount = 1;
        }
    }
    
    private SharedAssetManager() {
        textureCache = new ObjectMap<>();
        Gdx.app.log(TAG, "Inicializado");
    }
    
    public static SharedAssetManager getInstance() {
        if (instance == null) {
            instance = new SharedAssetManager();
        }
        return instance;
    }
    
    // ==================== TEXTURAS ====================
    
    /**
     * Obtiene una textura. Si ya está cargada, incrementa referencia.
     * Si no existe, la carga.
     * 
     * @param path Ruta del asset
     * @return Texture o null si hay error
     */
    public Texture get(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        // Ya existe en cache
        if (textureCache.containsKey(path)) {
            TextureEntry entry = textureCache.get(path);
            entry.refCount++;
            return entry.texture;
        }
        
        // Cargar nueva
        try {
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error(TAG, "Archivo no existe: " + path);
                return null;
            }
            
            Texture texture = new Texture(Gdx.files.internal(path));
            textureCache.put(path, new TextureEntry(texture));
            Gdx.app.log(TAG, "Cargado: " + path + " (total: " + textureCache.size + ")");
            return texture;
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene textura sin incrementar referencia (para verificación)
     */
    public Texture peek(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path).texture;
        }
        return null;
    }
    
    /**
     * Libera una referencia a la textura.
     * Si llega a 0, la textura se elimina de memoria.
     * 
     * @param path Ruta del asset
     */
    public void release(String path) {
        if (path == null || !textureCache.containsKey(path)) {
            return;
        }
        
        TextureEntry entry = textureCache.get(path);
        entry.refCount--;
        
        if (entry.refCount <= 0) {
            entry.texture.dispose();
            textureCache.remove(path);
            Gdx.app.log(TAG, "Liberado: " + path + " (total: " + textureCache.size + ")");
        }
    }
    
    /**
     * Verifica si una textura está cargada
     */
    public boolean isLoaded(String path) {
        return textureCache.containsKey(path);
    }
    
    /**
     * Obtiene el conteo de referencias de una textura
     */
    public int getRefCount(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path).refCount;
        }
        return 0;
    }
    
    // ==================== PRELOAD ====================
    
    /**
     * Precarga assets de uso frecuente.
     * Llamar desde KawaiiNekoMemory.create()
     */
    public void preloadCommon() {
        Gdx.app.log(TAG, "Precargando assets comunes...");
        
        // Iconos frecuentes
        get(AssetPaths.ICON_NEKOIN);
        get(AssetPaths.ICON_PAUSE);
        get(AssetPaths.ICON_HINT);
        get(AssetPaths.ICON_TIMEFREEZE);
        
        // Card back (usado en muchas pantallas)
        get(AssetPaths.CARD_BACK);
        
        // Patrones de fondo
        get(AssetPaths.PATTERN_HOME);
        
        // Botones comunes
        get(AssetPaths.BTN_BACK);
        get(AssetPaths.BTN_EMPTY);
        
        Gdx.app.log(TAG, "Precarga completada: " + textureCache.size + " texturas");
    }
    
    /**
     * Precarga una carta específica
     */
    public Texture preloadCard(int cardId) {
        int deck = cardId / AssetPaths.CARDS_PER_DECK;
        int index = cardId % AssetPaths.CARDS_PER_DECK;
        String path = AssetPaths.getCardPath(deck, index);
        return get(path);
    }
    
    /**
     * Libera una carta específica
     */
    public void releaseCard(int cardId) {
        int deck = cardId / AssetPaths.CARDS_PER_DECK;
        int index = cardId % AssetPaths.CARDS_PER_DECK;
        String path = AssetPaths.getCardPath(deck, index);
        release(path);
    }
    
    /**
     * Obtiene el path de una carta por su ID
     */
    public String getCardPath(int cardId) {
        int deck = cardId / AssetPaths.CARDS_PER_DECK;
        int index = cardId % AssetPaths.CARDS_PER_DECK;
        return AssetPaths.getCardPath(deck, index);
    }
    
    // ==================== BATCH OPERATIONS ====================
    
    /**
     * Libera múltiples texturas
     */
    public void releaseAll(String... paths) {
        for (String path : paths) {
            release(path);
        }
    }
    
    /**
     * Libera todas las cartas cargadas
     */
    public void releaseAllCards() {
        for (int deck = 0; deck < AssetPaths.TOTAL_DECKS; deck++) {
            for (int card = 0; card < AssetPaths.CARDS_PER_DECK; card++) {
                String path = AssetPaths.getCardPath(deck, card);
                if (isLoaded(path)) {
                    // Forzar liberación completa
                    while (isLoaded(path)) {
                        release(path);
                    }
                }
            }
        }
        Gdx.app.log(TAG, "Todas las cartas liberadas");
    }
    
    // ==================== DEBUG ====================
    
    /**
     * Imprime estado actual del cache
     */
    public void debugPrint() {
        Gdx.app.log(TAG, "=== CACHE STATUS ===");
        Gdx.app.log(TAG, "Total texturas: " + textureCache.size);
        
        int totalRefs = 0;
        for (ObjectMap.Entry<String, TextureEntry> entry : textureCache) {
            totalRefs += entry.value.refCount;
            Gdx.app.log(TAG, "  " + entry.key + " (refs: " + entry.value.refCount + ")");
        }
        Gdx.app.log(TAG, "Total referencias: " + totalRefs);
    }
    
    /**
     * Obtiene memoria estimada usada (en formato legible)
     */
    public String getMemoryUsage() {
        long bytes = 0;
        for (TextureEntry entry : textureCache.values()) {
            if (entry.texture != null) {
                bytes += (long) entry.texture.getWidth() * entry.texture.getHeight() * 4; // RGBA
            }
        }
        float mb = bytes / (1024f * 1024f);
        return String.format("%.2f MB (%d texturas)", mb, textureCache.size);
    }
    
    /**
     * Obtiene el número total de texturas cargadas
     */
    public int getLoadedCount() {
        return textureCache.size;
    }
    
    // ==================== LIFECYCLE ====================
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando todos los recursos...");
        
        for (TextureEntry entry : textureCache.values()) {
            if (entry.texture != null) {
                entry.texture.dispose();
            }
        }
        textureCache.clear();
        
        Gdx.app.log(TAG, "Recursos liberados");
    }
    
    /**
     * Reinicia el singleton (para testing o cambio de contexto)
     */
    public static void reset() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}
