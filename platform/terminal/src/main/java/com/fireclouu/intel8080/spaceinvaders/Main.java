package com.fireclouu.intel8080.spaceinvaders;

public class Main {
    public static void main(String[] args) {
        String filePath = args.length == 0 ? "/assets/tests/8080PRE.COM" : args[0];

        TerminalPlatform terminalPlatform = new TerminalPlatform(true);
        terminalPlatform.setFilePath(filePath);
        terminalPlatform.start();
    }
}
