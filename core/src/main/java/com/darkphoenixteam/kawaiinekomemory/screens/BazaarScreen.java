package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

public class BazaarScreen extends BaseScreen {
    
    private static final String TAG = "BazaarScreen";
    
    private static final float BUTTON_COOLDOWN = 0.5f;
    private float buttonCooldownTimer = 0f;
    
    private static final int GACHA_COST = 50;
    private static final int HINT_BASE_COST = 20;
    private static final int TIMEFREEZE_BASE_COST = 10;
    private static final int UPGRADE_BASE_COST = 100;
    private static final float UPGRADE_MULTIPLIER = 1.5f;
    private static final int MAX_POWER_LEVEL = 5;
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture hintIconTexture;
    private Texture timefreezeIconTexture;
    private Texture gachaIconTexture;
    private Texture cardBackTexture;
    
    private Array<Texture> allCardTextures;
    
    private SimpleButton backButton;
    private SimpleButton hintBuyButton;
    private SimpleButton hintUpgradeButton;
    private SimpleButton timefreezeBuyButton;
    private SimpleButton timefreezeUpgradeButton;
    private SimpleButton gachaButton;
    
    private boolean showingGachaResult = false;
    private int lastUnlockedCardId = -1;
    private float gachaResultTimer = 0f;
    private static final float GACHA_RESULT_DURATION = 2.5f;
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    public BazaarScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(0.95f, 0.9f, 1f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        
        allCardTextures = new Array<>();
        
        audioManager.playMusic(AssetPaths.MUSIC_BAZAAR, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Nekoins: " + saveManager.getNekoins());
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_BAZAAR));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error pattern");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
            hintIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_HINT_HERO));
            timefreezeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_TIMEFREEZE_HERO));
            gachaIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_GACHA));
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error iconos");
        }
        
        for (int deck = 0; deck < AssetPaths.TOTAL_DECKS; deck++) {
            for (int card = 0; card < AssetPaths.CARDS_PER_DECK; card++) {
                String path = AssetPaths.getCardPath(deck, card);
                try {
                    allCardTextures.add(new Texture(Gdx.files.internal(path)));
                } catch (Exception e) {
                    allCardTextures.add(null);
                }
            }
        }
    }
    
    private void createButtons() {
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        float buttonWidth = Constants.VIRTUAL_WIDTH * 0.4f;
        float buttonHeight = 45f;
        
        float hintY = Constants.VIRTUAL_HEIGHT - 200f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            hintBuyButton = new SimpleButton(btnTex, "COMPRAR", 
                centerX - buttonWidth - 10f, hintY, buttonWidth, buttonHeight);
            hintBuyButton.setOnClick(this::buyHint);
            
            hintUpgradeButton = new SimpleButton(btnTex, "MEJORAR",
                centerX + 10f, hintY, buttonWidth, buttonHeight);
            hintUpgradeButton.setOnClick(this::upgradeHint);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botones Hint");
        }
        
        float freezeY = Constants.VIRTUAL_HEIGHT - 370f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            
            timefreezeBuyButton = new SimpleButton(btnTex, "COMPRAR",
                centerX - buttonWidth - 10f, freezeY, buttonWidth, buttonHeight);
            timefreezeBuyButton.setOnClick(this::buyTimeFreeze);
            
            timefreezeUpgradeButton = new SimpleButton(btnTex, "MEJORAR",
                centerX + 10f, freezeY, buttonWidth, buttonHeight);
            timefreezeUpgradeButton.setOnClick(this::upgradeTimeFreeze);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botones TimeFreeze");
        }
        
        float gachaY = Constants.VIRTUAL_HEIGHT - 540f;
        
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BAZAAR));
            gachaButton = new SimpleButton(btnTex, "ABRIR x" + GACHA_COST,
                centerX - buttonWidth / 2f, gachaY, buttonWidth, buttonHeight);
            gachaButton.setOnClick(this::openGacha);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error boton Gacha");
        }
        
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float backWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float backHeight = backWidth * 0.35f;
            backButton = new SimpleButton(backTex, "VOLVER",
                (Constants.VIRTUAL_WIDTH - backWidth) / 2f, 20f, backWidth, backHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error boton back");
        }
    }
    
    private void buyHint() {
        if (buttonCooldownTimer > 0) return;
        buttonCooldownTimer = BUTTON_COOLDOWN;
        
        int cost = HINT_BASE_COST;
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeHint();
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "Hint comprado");
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void upgradeHint() {
        if (buttonCooldownTimer > 0) return;
        buttonCooldownTimer = BUTTON_COOLDOWN;
        
        int level = saveManager.getHintLevel();
        if (level <= 0 || level >= MAX_POWER_LEVEL) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        int cost = getUpgradeCost(level);
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeHint();
            audioManager.playSound(AssetPaths.SFX_MATCH);
            Gdx.app.log(TAG, "Hint mejorado a nivel " + saveManager.getHintLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void buyTimeFreeze() {
        if (buttonCooldownTimer > 0) return;
        buttonCooldownTimer = BUTTON_COOLDOWN;
        
        int cost = TIMEFREEZE_BASE_COST;
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeTimeFreeze();
            audioManager.playSound(AssetPaths.SFX_COIN);
            Gdx.app.log(TAG, "TimeFreeze comprado");
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void upgradeTimeFreeze() {
        if (buttonCooldownTimer > 0) return;
        buttonCooldownTimer = BUTTON_COOLDOWN;
        
        int level = saveManager.getTimeFreezeLevel();
        if (level <= 0 || level >= MAX_POWER_LEVEL) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        int cost = getUpgradeCost(level);
        if (saveManager.spendNekoins(cost)) {
            saveManager.upgradeTimeFreeze();
            audioManager.playSound(AssetPaths.SFX_MATCH);
            Gdx.app.log(TAG, "TimeFreeze mejorado a nivel " + saveManager.getTimeFreezeLevel());
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private void openGacha() {
        if (buttonCooldownTimer > 0) return;
        buttonCooldownTimer = BUTTON_COOLDOWN;
        
        Array<Integer> locked = getLockedCards();
        if (locked.size == 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Todas las cartas desbloqueadas");
            return;
        }
        
        if (saveManager.spendNekoins(GACHA_COST)) {
            int randomIndex = MathUtils.random(0, locked.size - 1);
            int cardId = locked.get(randomIndex);
            
            saveManager.unlockCard(cardId);
            
            audioManager.playSound(AssetPaths.SFX_VICTORY);
            lastUnlockedCardId = cardId;
            showingGachaResult = true;
            gachaResultTimer = GACHA_RESULT_DURATION;
            
            Gdx.app.log(TAG, "Gacha: desbloqueada carta " + cardId);
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    private int getUpgradeCost(int currentLevel) {
        return (int)(UPGRADE_BASE_COST * Math.pow(UPGRADE_MULTIPLIER, currentLevel - 1));
    }
    
    private Array<Integer> getLockedCards() {
        Array<Integer> locked = new Array<>();
        for (int i = 0; i < 35; i++) {
            if (!saveManager.isCardUnlocked(i)) {
                locked.add(i);
            }
        }
        return locked;
    }
    
    @Override
    protected void update(float delta) {
        if (buttonCooldownTimer > 0) {
            buttonCooldownTimer -= delta;
        }
        
        if (showingGachaResult) {
            gachaResultTimer -= delta;
            if (gachaResultTimer <= 0 || Gdx.input.justTouched()) {
                showingGachaResult = false;
                lastUnlockedCardId = -1;
            }
            return;
        }
        
        if (!isInputEnabled()) return;
        
        if (hintBuyButton != null) hintBuyButton.update(viewport);
        if (hintUpgradeButton != null) hintUpgradeButton.update(viewport);
        if (timefreezeBuyButton != null) timefreezeBuyButton.update(viewport);
        if (timefreezeUpgradeButton != null) timefreezeUpgradeButton.update(viewport);
        if (gachaButton != null) gachaButton.update(viewport);
        if (backButton != null) backButton.update(viewport);
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        drawBackground();
        drawHeader();
        drawHintSection();
        drawTimeFreezeSection();
        drawGachaSection();
        
        int hintLevel = saveManager.getHintLevel();
        if (hintBuyButton != null && hintLevel == 0) {
            hintBuyButton.draw(game.getBatch(), buttonFont);
        }
        if (hintUpgradeButton != null && hintLevel > 0 && hintLevel < MAX_POWER_LEVEL) {
            hintUpgradeButton.draw(game.getBatch(), buttonFont);
        }
        
        int freezeLevel = saveManager.getTimeFreezeLevel();
        if (timefreezeBuyButton != null && freezeLevel == 0) {
            timefreezeBuyButton.draw(game.getBatch(), buttonFont);
        }
        if (timefreezeUpgradeButton != null && freezeLevel > 0 && freezeLevel < MAX_POWER_LEVEL) {
            timefreezeUpgradeButton.draw(game.getBatch(), buttonFont);
        }
        
        if (gachaButton != null && getLockedCards().size > 0) {
            gachaButton.draw(game.getBatch(), buttonFont);
        }
        
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
        
        if (showingGachaResult) {
            drawGachaResult();
        }
    }
    
    private void drawBackground() {
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
    }
    
    private void drawHeader() {
        String title = "BAZAAR";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
            (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
            Constants.VIRTUAL_HEIGHT - 30f);
        
        if (nekoinIconTexture != null) {
            float iconSize = 35f;
            float iconX = Constants.VIRTUAL_WIDTH / 2f - 50f;
            float iconY = Constants.VIRTUAL_HEIGHT - 80f;
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            String nekoins = String.valueOf(saveManager.getNekoins());
            layout.setText(titleFont, nekoins);
            titleFont.draw(game.getBatch(), nekoins, iconX + iconSize + 10f, iconY + iconSize - 5f);
        }
    }
    
    private void drawHintSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 130f;
        
        if (hintIconTexture != null) {
            game.getBatch().draw(hintIconTexture, 20f, sectionY - 60f, 50f, 50f);
        }
        
        buttonFont.draw(game.getBatch(), "PISTA", 80f, sectionY);
        
        int level = saveManager.getHintLevel();
        String status = level == 0 ? "No comprado" : 
                       level >= MAX_POWER_LEVEL ? "MAX" : "Nivel " + level;
        String cost = level == 0 ? "Costo: " + HINT_BASE_COST : 
                     level >= MAX_POWER_LEVEL ? "" : "Mejora: " + getUpgradeCost(level);
        
        smallFont.setColor(level == 0 ? Color.GRAY : level >= MAX_POWER_LEVEL ? Color.GREEN : Color.WHITE);
        smallFont.draw(game.getBatch(), status, 80f, sectionY - 25f);
        smallFont.draw(game.getBatch(), cost, 80f, sectionY - 45f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawTimeFreezeSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 300f;
        
        if (timefreezeIconTexture != null) {
            game.getBatch().draw(timefreezeIconTexture, 20f, sectionY - 60f, 50f, 50f);
        }
        
        buttonFont.draw(game.getBatch(), "CONGELAR", 80f, sectionY);
        
        int level = saveManager.getTimeFreezeLevel();
        String status = level == 0 ? "No comprado" : 
                       level >= MAX_POWER_LEVEL ? "MAX" : "Nivel " + level;
        String cost = level == 0 ? "Costo: " + TIMEFREEZE_BASE_COST : 
                     level >= MAX_POWER_LEVEL ? "" : "Mejora: " + getUpgradeCost(level);
        
        smallFont.setColor(level == 0 ? Color.GRAY : level >= MAX_POWER_LEVEL ? Color.CYAN : Color.WHITE);
        smallFont.draw(game.getBatch(), status, 80f, sectionY - 25f);
        smallFont.draw(game.getBatch(), cost, 80f, sectionY - 45f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaSection() {
        float sectionY = Constants.VIRTUAL_HEIGHT - 470f;
        float centerX = Constants.VIRTUAL_WIDTH / 2f;
        
        if (gachaIconTexture != null) {
            game.getBatch().draw(gachaIconTexture, centerX - 30f, sectionY - 10f, 60f, 60f);
        }
        
        String title = "GACHAPON";
        layout.setText(buttonFont, title);
        buttonFont.draw(game.getBatch(), title, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY + 70f);
        
        int unlocked = saveManager.getUnlockedCardCount();
        String status = unlocked >= 35 ? "Completo!" : "Cartas: " + unlocked + "/35";
        smallFont.setColor(unlocked >= 35 ? Color.GREEN : Color.WHITE);
        layout.setText(smallFont, status);
        smallFont.draw(game.getBatch(), status, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, sectionY - 70f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawGachaResult() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.85f);
        if (cardBackTexture != null) {
            game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        
        titleFont.setColor(Color.GOLD);
        String title = "NUEVA CARTA!";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.78f);
        titleFont.setColor(Color.WHITE);
        
        if (lastUnlockedCardId >= 0 && lastUnlockedCardId < allCardTextures.size) {
            Texture tex = allCardTextures.get(lastUnlockedCardId);
            if (tex != null) {
                float w = 140f;
                float h = w * 1.4f;
                game.getBatch().draw(tex, (Constants.VIRTUAL_WIDTH - w) / 2f, (Constants.VIRTUAL_HEIGHT - h) / 2f, w, h);
            }
        }
        
        int deck = SaveManager.getDeckFromCardId(lastUnlockedCardId);
        String[] names = {"Base", "Estrella", "2 Estrellas", "3 Estrellas", "Corazon"};
        int[] vals = {1, 2, 3, 5, 7};
        String info = names[deck] + " | +" + vals[deck] + " Nekoin/par";
        layout.setText(buttonFont, info);
        buttonFont.draw(game.getBatch(), info, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.25f);
        
        smallFont.setColor(Color.GRAY);
        String tap = "Toca para continuar";
        layout.setText(smallFont, tap);
        smallFont.draw(game.getBatch(), tap, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, Constants.VIRTUAL_HEIGHT * 0.12f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (hintIconTexture != null) hintIconTexture.dispose();
        if (timefreezeIconTexture != null) timefreezeIconTexture.dispose();
        if (gachaIconTexture != null) gachaIconTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        for (Texture tex : allCardTextures) {
            if (tex != null) tex.dispose();
        }
        
        if (backButton != null) backButton.dispose();
        if (hintBuyButton != null) hintBuyButton.dispose();
        if (hintUpgradeButton != null) hintUpgradeButton.dispose();
        if (timefreezeBuyButton != null) timefreezeBuyButton.dispose();
        if (timefreezeUpgradeButton != null) timefreezeUpgradeButton.dispose();
        if (gachaButton != null) gachaButton.dispose();
    }
}