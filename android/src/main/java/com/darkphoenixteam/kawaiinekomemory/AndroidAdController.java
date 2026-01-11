package com.darkphoenixteam.kawaiinekomemory;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;

import com.darkphoenixteam.kawaiinekomemory.systems.AdController;

/**
 * Implementación de AdController usando AppLovin MAX
 * Maneja Banner, Interstitial y Rewarded ads con mediación
 * 
 * @author DarkphoenixTeam
 */
public class AndroidAdController implements AdController {
    
    private static final String TAG = "AndroidAdController";
    
    // === AD UNIT IDs (REEMPLAZAR CON TUS IDs REALES) ===
    // Estos son placeholders - obtener de AppLovin Dashboard
    private static final String BANNER_AD_UNIT_ID = "YOUR_BANNER_AD_UNIT_ID";
    private static final String INTERSTITIAL_AD_UNIT_ID = "YOUR_INTERSTITIAL_AD_UNIT_ID";
    private static final String REWARDED_AD_UNIT_ID = "YOUR_REWARDED_AD_UNIT_ID";
    
    private final Activity activity;
    private RelativeLayout mainLayout;
    
    // Ads
    private MaxAdView bannerAd;
    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;
    
    // Estado
    private boolean isInitialized = false;
    private RewardedAdListener currentRewardedListener;
    
    public AndroidAdController(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Inicializa AppLovin SDK y carga los ads
     */
    public void initialize(RelativeLayout layout) {
        this.mainLayout = layout;
        
        Log.d(TAG, "Inicializando AppLovin SDK...");
        
        AppLovinSdk.getInstance(activity).setMediationProvider("max");
        AppLovinSdk.getInstance(activity).initializeSdk(config -> {
            Log.d(TAG, "AppLovin SDK inicializado");
            isInitialized = true;
            
            // Inicializar ads en el hilo principal
            activity.runOnUiThread(() -> {
                initializeBanner();
                initializeInterstitial();
                initializeRewarded();
            });
        });
    }
    
    // === BANNER ===
    
    private void initializeBanner() {
        bannerAd = new MaxAdView(BANNER_AD_UNIT_ID, activity);
        bannerAd.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "Banner cargado");
            }
            
            @Override
            public void onAdDisplayed(MaxAd ad) {}
            
            @Override
            public void onAdHidden(MaxAd ad) {}
            
            @Override
            public void onAdClicked(MaxAd ad) {}
            
            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "Banner falló: " + error.getMessage());
            }
            
            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {}
            
            @Override
            public void onAdExpanded(MaxAd ad) {}
            
            @Override
            public void onAdCollapsed(MaxAd ad) {}
        });
        
        // Configurar posición del banner (parte inferior)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerAd.setLayoutParams(params);
        
        // Oculto por defecto
        bannerAd.setVisibility(View.GONE);
        mainLayout.addView(bannerAd);
        
        // Cargar banner
        bannerAd.loadAd();
    }
    
    @Override
    public void showBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAd != null) {
                bannerAd.setVisibility(View.VISIBLE);
                bannerAd.startAutoRefresh();
            }
        });
    }
    
    @Override
    public void hideBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAd != null) {
                bannerAd.setVisibility(View.GONE);
                bannerAd.stopAutoRefresh();
            }
        });
    }
    
    // === INTERSTITIAL ===
    
    private void initializeInterstitial() {
        interstitialAd = new MaxInterstitialAd(INTERSTITIAL_AD_UNIT_ID, activity);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "Interstitial cargado");
            }
            
            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "Interstitial mostrado");
            }
            
            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "Interstitial cerrado");
                // Precargar siguiente
                interstitialAd.loadAd();
            }
            
            @Override
            public void onAdClicked(MaxAd ad) {}
            
            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "Interstitial falló: " + error.getMessage());
            }
            
            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e(TAG, "Interstitial display falló: " + error.getMessage());
                interstitialAd.loadAd();
            }
        });
        
        interstitialAd.loadAd();
    }
    
    @Override
    public boolean isInterstitialLoaded() {
        return interstitialAd != null && interstitialAd.isReady();
    }
    
    @Override
    public void showInterstitial() {
        activity.runOnUiThread(() -> {
            if (isInterstitialLoaded()) {
                interstitialAd.showAd();
            } else {
                Log.w(TAG, "Interstitial no está listo");
            }
        });
    }
    
    // === REWARDED ===
    
    private void initializeRewarded() {
        rewardedAd = MaxRewardedAd.getInstance(REWARDED_AD_UNIT_ID, activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "Rewarded cargado");
            }
            
            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "Rewarded mostrado");
            }
            
            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "Rewarded cerrado");
                // Precargar siguiente
                rewardedAd.loadAd();
            }
            
            @Override
            public void onAdClicked(MaxAd ad) {}
            
            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "Rewarded falló: " + error.getMessage());
            }
            
            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e(TAG, "Rewarded display falló: " + error.getMessage());
                if (currentRewardedListener != null) {
                    currentRewardedListener.onRewardCancelled();
                    currentRewardedListener = null;
                }
                rewardedAd.loadAd();
            }
            
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {}
            
            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {}
            
            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                Log.d(TAG, "Usuario recompensado: " + reward.getLabel() + " x" + reward.getAmount());
                if (currentRewardedListener != null) {
                    currentRewardedListener.onRewardEarned(reward.getLabel(), reward.getAmount());
                    currentRewardedListener = null;
                }
            }
        });
        
        rewardedAd.loadAd();
    }
    
    @Override
    public boolean isRewardedLoaded() {
        return rewardedAd != null && rewardedAd.isReady();
    }
    
    @Override
    public void showRewarded(RewardedAdListener listener) {
        activity.runOnUiThread(() -> {
            if (isRewardedLoaded()) {
                currentRewardedListener = listener;
                rewardedAd.showAd();
            } else {
                Log.w(TAG, "Rewarded no está listo");
                if (listener != null) {
                    listener.onRewardCancelled();
                }
            }
        });
    }
    
    // === LIFECYCLE ===
    
    public void onResume() {
        if (bannerAd != null) {
            bannerAd.startAutoRefresh();
        }
    }
    
    public void onPause() {
        if (bannerAd != null) {
            bannerAd.stopAutoRefresh();
        }
    }
    
    public void onDestroy() {
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        if (rewardedAd != null) {
            rewardedAd.destroy();
        }
    }
}