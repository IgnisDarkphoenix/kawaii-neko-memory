package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.utils.Constants;

public class SplashScreen extends BaseScreen {
    
    private int splashPhase = 1;  // 1 = Team, 2 = Game Logo
    private float timer = 0f;
    private Label mainLabel;
    private Label subLabel;
    private BitmapFont font;
    
    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        
        // Fondo oscuro para splash
        setBackgroundColor(0.1f, 0.1f, 0.15f);
        
        setupUI();
    }
    
    private void setupUI() {
        font = new BitmapFont();
        
        LabelStyle titleStyle = new LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = Color.WHITE;
        
        // Texto principal
        mainLabel = new Label("DarkphoenixTeam", titleStyle);
        mainLabel.setFontScale(3f);
        mainLabel.setPosition(
            (Constants.WORLD_WIDTH - mainLabel.getWidth() * 3f) / 2f,
            Constants.WORLD_HEIGHT / 2f
        );
        
        // Subtexto
        subLabel = new Label("Presents", titleStyle);
        subLabel.setFontScale(1.5f);
        subLabel.setPosition(
            (Constants.WORLD_WIDTH - subLabel.getWidth() * 1.5f) / 2f,
            Constants.WORLD_HEIGHT / 2f - 60f
        );
        
        // Fade in
        mainLabel.getColor().a = 0f;
        subLabel.getColor().a = 0f;
        
        mainLabel.addAction(Actions.fadeIn(0.5f));
        subLabel.addAction(Actions.sequence(
            Actions.delay(0.3f),
            Actions.fadeIn(0.5f)
        ));
        
        stage.addActor(mainLabel);
        stage.addActor(subLabel);
    }
    
    @Override
    public void render(float delta) {
        super.render(delta);
        
        timer += delta;
        
        if (splashPhase == 1 && timer >= Constants.SPLASH_DURATION) {
            // Cambiar a fase 2
            splashPhase = 2;
            timer = 0f;
            showGameLogo();
        } else if (splashPhase == 2 && timer >= Constants.SPLASH_DURATION) {
            // Ir a HomeScreen
            goToHome();
        }
    }
    
    private void showGameLogo() {
        // Fade out fase 1
        mainLabel.addAction(Actions.fadeOut(0.3f));
        subLabel.addAction(Actions.fadeOut(0.3f));
        
        // Cambiar fondo a rosa
        setBackgroundColor(1f, 0.85f, 0.9f);
        
        // Nuevo texto
        mainLabel.addAction(Actions.sequence(
            Actions.delay(0.4f),
            Actions.run(() -> {
                mainLabel.setText(Constants.GAME_TITLE);
                mainLabel.setPosition(
                    (Constants.WORLD_WIDTH - mainLabel.getWidth() * 3f) / 2f,
                    Constants.WORLD_HEIGHT / 2f + 30f
                );
                mainLabel.getColor().a = 0f;
                mainLabel.addAction(Actions.fadeIn(0.5f));
            })
        ));
        
        subLabel.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.run(() -> {
                subLabel.setText("~ Memory Match Game ~");
                subLabel.setPosition(
                    (Constants.WORLD_WIDTH - subLabel.getWidth() * 1.5f) / 2f,
                    Constants.WORLD_HEIGHT / 2f - 40f
                );
                subLabel.getColor().a = 0f;
                subLabel.addAction(Actions.fadeIn(0.5f));
            })
        ));
    }
    
    private void goToHome() {
        Gdx.app.log("SplashScreen", "Going to HomeScreen...");
        game.setScreen(new HomeScreen(game));
        dispose();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (font != null) font.dispose();
    }
}