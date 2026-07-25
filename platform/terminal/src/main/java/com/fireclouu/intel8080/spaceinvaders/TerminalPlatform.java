package com.fireclouu.intel8080.spaceinvaders;

import java.io.InputStream;

import com.fireclouu.spaceinvaders.intel8080.Platform;

public class TerminalPlatform extends Platform {

    public TerminalPlatform(boolean isTestSuite) {
        super(isTestSuite);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void draw(short[] memoryVideoRam) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSound(int id) {
        // TODO Auto-generated method stub
    }

    @Override
    public void vibrate(long milli) {
        // TODO Auto-generated method stub
    }

    @Override
    public void writeLog(String message) {
        // TODO Auto-generated method stub
    }

    @Override
    public void log(Exception e, String message) {
        // TODO Auto-generated method stub
    }

    @Override
    public void saveHighScoreOnPlatform(int data) {
        // TODO Auto-generated method stub
    }

    @Override
    public int playMedia(int id, int loop, int priority) {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int fetchHighScoreOnPlatform() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public InputStream openFile(String romName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTestAssetPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendNotification(String message) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getMediaAudioIdAlienKilled() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdAlienMove1() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdAlienMove2() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdAlienMove3() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdAlienMove4() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdFire() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdPlayerExploded() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdShipHit() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public int getMediaAudioIdShipIncoming() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public void initMediaHandler() {
        // TODO Auto-generated method stub
    }

    @Override
    public void releaseResources() {
        // TODO Auto-generated method stub
    }

    @Override
    public void showDebug() {
        // TODO Auto-generated method stub
    }
    
}
