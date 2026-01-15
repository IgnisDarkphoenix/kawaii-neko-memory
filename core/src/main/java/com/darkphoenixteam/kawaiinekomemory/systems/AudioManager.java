package com.darkphoenixteam.kawaiinekomemory.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Gestor centralizado de audio (Singleton)
 * Maneja música y efectos de sonido con persistencia de volumen
 * 
 * @author DarkphoenixTeam
 */
public class AudioManager implements Disposable {
    
    private static final String TAG = "AudioManager";
    
    // Singleton
    private static AudioManager instance;
    
    // Preferences
    private static final String PREFS_NAME = "KawaiiNekoAudio";
    private static final String KEY_MUSIC_VOLUME = "musicVolume";
    private static final String KEY_SOUND_VOLUME = "soundVolume";
    
    // Volúmenes (0.0 a 1.0)
    private float musicVolume;
    private float soundVolume;
    
    // Música actual
    private Music currentMusic;
    private String currentMusicPath;
    
    // Cache de sonidos (evita recargar)
    private ObjectMap<String, Sound> soundCache;
    
    // Preferences de libGDX
    private Preferences prefs;
    
    /**
     * Constructor privado (Singleton)
     */
    private AudioManager() {
        soundCache = new ObjectMap<>();
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        loadPreferences();
        
        Gdx.app.log(TAG, "Inicializado - Música: " + (int)(musicVolume * 100) + 
                         "%, Efectos: " + (int)(soundVolume * 100) + "%");
    }
    
    /**
     * Obtiene la instancia única del AudioManager
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Carga las preferencias guardadas (o usa valores por defecto)
     */
    private void loadPreferences() {
        musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, 1.0f);
        soundVolume = prefs.getFloat(KEY_SOUND_VOLUME, 1.0f);
    }
    
    /**
     * Guarda las preferencias actuales
     */
    private void savePreferences() {
        prefs.putFloat(KEY_MUSIC_VOLUME, musicVolume);
        prefs.putFloat(KEY_SOUND_VOLUME, soundVolume);
        prefs.flush();
    }
    
    // ==================== MÚSICA ====================
    
    /**
     * Reproduce música de fondo
     * @param assetPath Ruta del archivo de música
     * @param loop Si debe repetirse en bucle
     */
    public void playMusic(String assetPath, boolean loop) {
        // Si ya está reproduciendo la misma canción, solo asegurar que suene
        if (currentMusic != null && assetPath.equals(currentMusicPath)) {
            if (!currentMusic.isPlaying()) {
                currentMusic.setVolume(musicVolume);
                currentMusic.play();
            }
            return;
        }
        
        // Detener música anterior
        stopMusic();
        
        try {
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(assetPath));
            currentMusic.setLooping(loop);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
            currentMusicPath = assetPath;
            
            Gdx.app.log(TAG, "Reproduciendo: " + assetPath + " (loop=" + loop + ")");
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando música: " + assetPath, e);
        }
    }
    
    /**
     * Detiene la música actual y libera recursos
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }
    
    /**
     * Pausa la música actual
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }
    
    /**
     * Reanuda la música pausada
     */
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }
    
    /**
     * Verifica si hay música reproduciéndose
     */
    public boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }
    
    // ==================== EFECTOS DE SONIDO ====================
    
    /**
     * Reproduce un efecto de sonido
     * @param assetPath Ruta del archivo de sonido
     */
    public void playSound(String assetPath) {
        // No reproducir si el volumen es 0
        if (soundVolume <= 0) return;
        
        try {
            // Buscar en cache primero
            Sound sound = soundCache.get(assetPath);
            
            // Si no está en cache, cargar y guardar
            if (sound == null) {
                sound = Gdx.audio.newSound(Gdx.files.internal(assetPath));
                soundCache.put(assetPath, sound);
                Gdx.app.log(TAG, "Sonido cacheado: " + assetPath);
            }
            
            // Reproducir con el volumen actual
            sound.play(soundVolume);
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error reproduciendo sonido: " + assetPath, e);
        }
    }
    
    /**
     * Precarga un sonido en el cache (útil para loading screens)
     * @param assetPath Ruta del archivo de sonido
     */
    public void preloadSound(String assetPath) {
        if (!soundCache.containsKey(assetPath)) {
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(assetPath));
                soundCache.put(assetPath, sound);
                Gdx.app.log(TAG, "Sonido precargado: " + assetPath);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error precargando sonido: " + assetPath, e);
            }
        }
    }
    
    // ==================== CONTROL DE VOLUMEN ====================
    
    /**
     * Establece el volumen de música (0.0 a 1.0)
     * Actualiza en tiempo real y guarda en preferencias
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        
        // Aplicar inmediatamente a la música actual
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        
        savePreferences();
    }
    
    /**
     * Establece el volumen de efectos (0.0 a 1.0)
     * Guarda en preferencias
     */
    public void setSoundVolume(float volume) {
        this.soundVolume = clamp(volume);
        savePreferences();
    }
    
    /**
     * Obtiene el volumen actual de música
     */
    public float getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Obtiene el volumen actual de efectos
     */
    public float getSoundVolume() {
        return soundVolume;
    }
    
    /**
     * Silencia todo el audio temporalmente
     */
    public void muteAll() {
        if (currentMusic != null) {
            currentMusic.setVolume(0);
        }
    }
    
    /**
     * Restaura el volumen después de silenciar
     */
    public void unmuteAll() {
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Limita un valor entre 0.0 y 1.0
     */
    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Libera todos los recursos de audio
     */
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        // Liberar música
        stopMusic();
        
        // Liberar todos los sonidos en cache
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
        
        Gdx.app.log(TAG, "Recursos liberados");
    }
    
    /**
     * Reinicia el singleton (útil para testing)
     */
    public static void reset() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}