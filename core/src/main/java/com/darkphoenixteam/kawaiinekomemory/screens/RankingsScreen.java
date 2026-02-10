package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.Achievement;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de Rankings con localización completa
 * 
 * @author DarkphoenixTeam
 * @version 1.1 - Localización completa
 */
public class RankingsScreen extends BaseScreen {
    
    private static final String TAG = "RankingsScreen";
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === TEXTURAS ===
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    
    // === BOTONES ===
    private SimpleButton backButton;
    
    // === RENDER ===
    private ShapeRenderer shapeRenderer;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    
    // === LAYOUT ===
    private static final float HEADER_HEIGHT = 100f;
    private static final float CARD_MARGIN = 20f;
    private static final float CARD_SPACING = 15f;
    
    public RankingsScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(0.15f, 0.1f, 0.2f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        locale = LocaleManager.getInstance();
        
        shapeRenderer = new ShapeRenderer();
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Rankings Screen inicializado");
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.log(TAG, "Nekoin icon no encontrado");
        }
    }
    
    private void createButtons() {
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float btnHeight = btnWidth * 0.35f;
            backButton = new SimpleButton(backTex, locale.get("common.back"),
                (Constants.VIRTUAL_WIDTH - btnWidth) / 2f, 20f, btnWidth, btnHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón back");
        }
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) return;
        
        if (backButton != null) backButton.update(viewport);
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.1f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        game.getBatch().end();
        
        drawStatCards();
        
        game.getBatch().begin();
        drawHeader();
        drawStatContent();
        
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    private void drawHeader() {
        String title = locale.get("rankings.title");
        titleFont.setColor(Color.GOLD);
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT - 30f);
        titleFont.setColor(Color.WHITE);
        
        String subtitle = locale.get("rankings.subtitle");
        smallFont.setColor(Color.LIGHT_GRAY);
        layout.setText(smallFont, subtitle);
        smallFont.draw(game.getBatch(), subtitle,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT - 65f);
        smallFont.setColor(Color.WHITE);
    }
    
    private void drawStatCards() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        float cardWidth = Constants.VIRTUAL_WIDTH - (CARD_MARGIN * 2);
        float cardHeight = 100f;
        float startY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - 20f;
        
        drawCard(CARD_MARGIN, startY - cardHeight, cardWidth, cardHeight, 
                new Color(0.8f, 0.4f, 0.1f, 0.9f));
        
        drawCard(CARD_MARGIN, startY - cardHeight * 2 - CARD_SPACING, cardWidth, cardHeight,
                new Color(0.2f, 0.5f, 0.8f, 0.9f));
        
        drawCard(CARD_MARGIN, startY - cardHeight * 3 - CARD_SPACING * 2, cardWidth, cardHeight,
                new Color(0.6f, 0.3f, 0.7f, 0.9f));
        
        drawCard(CARD_MARGIN, startY - cardHeight * 4 - CARD_SPACING * 3, cardWidth, cardHeight,
                new Color(0.2f, 0.7f, 0.4f, 0.9f));
    }
    
    private void drawCard(float x, float y, float width, float height, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.3f);
        shapeRenderer.rect(x + 4f, y - 4f, width, height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 0.3f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }
    
    private void drawStatContent() {
        float cardWidth = Constants.VIRTUAL_WIDTH - (CARD_MARGIN * 2);
        float cardHeight = 100f;
        float startY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - 20f;
        float textX = CARD_MARGIN + 15f;
        
        // === CARD 1: TIME ATTACK ===
        float card1Y = startY - cardHeight;
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "⏱ " + locale.get("rankings.timeattack"), textX, card1Y + cardHeight - 15f);
        
        int bestPairs = saveManager.getTimeAttackBestPairs();
        int totalPairs = saveManager.getTimeAttackTotalPairs();
        int gamesPlayed = saveManager.getTimeAttackGamesPlayed();
        float currentTime = saveManager.getTimeAttackTime();
        
        smallFont.setColor(Color.WHITE);
        
        if (bestPairs > 0) {
            buttonFont.setColor(Color.GOLD);
            String recordText = locale.format("rankings.best", bestPairs);
            layout.setText(buttonFont, recordText);
            buttonFont.draw(game.getBatch(), recordText, 
                           Constants.VIRTUAL_WIDTH - CARD_MARGIN - 15f - layout.width,
                           card1Y + cardHeight - 15f);
            buttonFont.setColor(Color.WHITE);
        }
        
        smallFont.draw(game.getBatch(), locale.format("rankings.totalpairs", totalPairs), textX, card1Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.games", gamesPlayed), textX, card1Y + cardHeight - 65f);
        smallFont.draw(game.getBatch(), locale.format("game.time", formatTime(currentTime)), textX + 150f, card1Y + cardHeight - 65f);
        
        // === CARD 2: ESTADÍSTICAS ===
        float card2Y = startY - cardHeight * 2 - CARD_SPACING;
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "📊 " + locale.get("rankings.stats"), textX, card2Y + cardHeight - 15f);
        
        int totalWins = saveManager.getTotalWins();
        int totalLosses = saveManager.getTotalLosses();
        int totalGamePairs = saveManager.getTotalPairsFound();
        int bestCombo = saveManager.getBestCombo();
        
        smallFont.setColor(Color.WHITE);
        smallFont.draw(game.getBatch(), locale.format("rankings.wins", totalWins), textX, card2Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.losses", totalLosses), textX + 140f, card2Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.totalpairs", totalGamePairs), textX, card2Y + cardHeight - 65f);
        smallFont.draw(game.getBatch(), locale.format("rankings.bestcombo", bestCombo), textX + 180f, card2Y + cardHeight - 65f);
        
        if (totalWins + totalLosses > 0) {
            float winRate = (totalWins * 100f) / (totalWins + totalLosses);
            String rateText = String.format("%.1f%%", winRate);
            buttonFont.setColor(winRate >= 50 ? Color.GREEN : Color.RED);
            layout.setText(buttonFont, rateText);
            buttonFont.draw(game.getBatch(), rateText,
                           Constants.VIRTUAL_WIDTH - CARD_MARGIN - 15f - layout.width,
                           card2Y + cardHeight - 15f);
            buttonFont.setColor(Color.WHITE);
        }
        
        // === CARD 3: COLECCIÓN ===
        float card3Y = startY - cardHeight * 3 - CARD_SPACING * 2;
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "🎴 " + locale.get("rankings.collection"), textX, card3Y + cardHeight - 15f);
        
        int unlockedCards = saveManager.getUnlockedCardCount();
        int totalCards = Constants.TOTAL_CARDS;
        int activeCards = saveManager.getActiveCardCount();
        int gachaPulls = saveManager.getGachaPulls();
        
        smallFont.setColor(Color.WHITE);
        smallFont.draw(game.getBatch(), locale.format("rankings.cards", unlockedCards, totalCards), textX, card3Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.activecount", activeCards), textX + 140f, card3Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.gachapulls", gachaPulls), textX, card3Y + cardHeight - 65f);
        
        float collectionPercent = (unlockedCards * 100f) / totalCards;
        String percentText = String.format("%.0f%%", collectionPercent);
        buttonFont.setColor(collectionPercent >= 100 ? Color.GOLD : Color.WHITE);
        layout.setText(buttonFont, percentText);
        buttonFont.draw(game.getBatch(), percentText,
                       Constants.VIRTUAL_WIDTH - CARD_MARGIN - 15f - layout.width,
                       card3Y + cardHeight - 15f);
        buttonFont.setColor(Color.WHITE);
        
        // === CARD 4: LOGROS ===
        float card4Y = startY - cardHeight * 4 - CARD_SPACING * 3;
        
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(game.getBatch(), "🏆 " + locale.get("rankings.achievements"), textX, card4Y + cardHeight - 15f);
        
        int unlockedAchievements = saveManager.getUnlockedAchievementCount();
        int totalAchievements = Achievement.count();
        int totalEarned = saveManager.getTotalNekoinsEarned();
        int totalSpent = saveManager.getTotalNekoinsSpent();
        int powersUsed = saveManager.getTotalPowersUsed();
        
        smallFont.setColor(Color.WHITE);
        smallFont.draw(game.getBatch(), locale.format("rankings.cards", unlockedAchievements, totalAchievements), 
                      textX, card4Y + cardHeight - 45f);
        smallFont.draw(game.getBatch(), locale.format("rankings.powersused", powersUsed), textX + 150f, card4Y + cardHeight - 45f);
        
        if (nekoinIconTexture != null) {
            float iconSize = 18f;
            float iconY = card4Y + cardHeight - 75f;
            
            game.getBatch().draw(nekoinIconTexture, textX, iconY, iconSize, iconSize);
            smallFont.draw(game.getBatch(), locale.format("rankings.earned", totalEarned), textX + iconSize + 5f, iconY + iconSize - 3f);
            
            game.getBatch().draw(nekoinIconTexture, textX + 160f, iconY, iconSize, iconSize);
            smallFont.draw(game.getBatch(), locale.format("rankings.spent", totalSpent), textX + 160f + iconSize + 5f, iconY + iconSize - 3f);
        }
        
        float achievePercent = (unlockedAchievements * 100f) / totalAchievements;
        String achieveText = String.format("%.0f%%", achievePercent);
        buttonFont.setColor(achievePercent >= 100 ? Color.GOLD : Color.WHITE);
        layout.setText(buttonFont, achieveText);
        buttonFont.draw(game.getBatch(), achieveText,
                       Constants.VIRTUAL_WIDTH - CARD_MARGIN - 15f - layout.width,
                       card4Y + cardHeight - 15f);
        buttonFont.setColor(Color.WHITE);
        
        smallFont.setColor(Color.WHITE);
    }
    
    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (backButton != null) backButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
