package com.darkphoenixteam.kawaiinekomemory.systems;

/**
 * Interface para controlar anuncios
 * Implementada en Android, permite que Core no dependa de librerías Android
 * 
 * @author DarkphoenixTeam
 */
public interface AdController {
    
    // === BANNER ADS ===
    
    /**
     * Muestra el banner en la parte inferior
     */
    void showBanner();
    
    /**
     * Oculta el banner
     */
    void hideBanner();
    
    // === INTERSTITIAL ADS ===
    
    /**
     * Verifica si hay un interstitial listo
     */
    boolean isInterstitialLoaded();
    
    /**
     * Muestra un anuncio interstitial
     */
    void showInterstitial();
    
    // === REWARDED ADS ===
    
    /**
     * Verifica si hay un rewarded listo
     */
    boolean isRewardedLoaded();
    
    /**
     * Muestra un anuncio rewarded con callback
     * @param listener Callback cuando el usuario gana la recompensa
     */
    void showRewarded(RewardedAdListener listener);
    
    // === LISTENER INTERFACE ===
    
    /**
     * Callback para cuando el usuario completa un video rewarded
     */
    interface RewardedAdListener {
        /**
         * Llamado cuando el usuario gana la recompensa
         * @param rewardType Tipo de recompensa (hint, time, coins, etc.)
         * @param rewardAmount Cantidad de recompensa
         */
        void onRewardEarned(String rewardType, int rewardAmount);
        
        /**
         * Llamado cuando el usuario cierra el ad sin completar
         */
        void onRewardCancelled();
    }
}