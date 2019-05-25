package org.the429ers.gameboy;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class Pallette implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 6087300316832303687L;
    public static final int GRAY_MODE = 0;
    public static final int GREEN_MODE = 1;
    public static final int RAINBOW_MODE = 2;
    
    private int[] colorMap = new int[4];

    private static long ticks = 0;
    private static int time = 0;

    public static final Color[] grayscaleColors = new Color[]{
            Color.WHITE,
            Color.LIGHT_GRAY,
            Color.DARK_GRAY,
            Color.BLACK
    };

    public static Color[][] rainbowColors = null;
    static {
        rainbowColors = new Color[1024][4];
        for(int i = 0; i < 1024; i++){
            for(int j = 0; j < 4; j++){
                double theta = 2 * Math.PI * i / 1024 + Math.PI/2 * j;

                int red = (int)(128 + 127*Math.sin(theta));
                int blue = (int)(128 + 127*Math.sin(theta + 2 * Math.PI / 3));
                int green = (int)(128 + 127*Math.sin(theta + 4 * Math.PI / 3));

                rainbowColors[i][j] = new Color(red, green, blue);
            }
        }
    }
    
    public static final Color[] greenscaleColors = new Color[] {
            new Color(0x8bac0f),
            new Color(0x9bbc0f),
            new Color(0x306230),
            new Color(0x0f380f),
    };
    
    public static final String[] modeNames = new String[] {"Grayscale", "Classic", "Psychedelic"};
    public static int colorMode = GRAY_MODE;
    
    public Pallette(int data) {
        for (int i = 0; i < 4 ; i++) {
            int color = (int) BitOps.extract(data, (2 * i) + 1, 2 * i);
            colorMap[i] = color;
        }
    }
    
    public Color getColor(int colorNum) {
        if(colorMode == RAINBOW_MODE) {
            ticks++;
            if (ticks > 10000) {
                time++;
                if(time >= 1024){
                    time = 0;
                }
                ticks = 0;
            }
            return rainbowColors[time][colorMap[colorNum]];
        }else if(colorMode == GRAY_MODE){
            return grayscaleColors[colorMap[colorNum]];
        }else {
            return greenscaleColors[colorMap[colorNum]];
        }
    }
}
