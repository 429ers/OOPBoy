package org.gheith.gameboy;

import javax.sound.sampled.*;

public interface SoundChannel {
    void handleByte(int location, int toWrite);
}

class SquareWave implements SoundChannel {
    private int duty = 0;
    private int lengthLoad = 0;
    private int startingVolume = 0;
    private boolean envelopeAdd = false;
    private int envelopePeriod = 0;
    private int frequency = 0;
    private boolean playing = false;
    private boolean lengthEnabled = false;
    private int lengthCounter = 0;
    
    private Clip clip;
    
    public static final int START_ADDR = 0xff15;
    
    Thread lengthDecreaser = new Thread(() -> {
        while(true){
            if(lengthEnabled){
                lengthCounter--;
                if(lengthCounter <= 0){
                    clip.stop();
                    this.playing = false;
                }
            }

            try {
                Thread.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    
    private static final int SAMPLE_RATE = 131072;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(8 * 131072, 8, 1, true, false);

    public static void main(String[] args) throws InterruptedException {
        SquareWave test = new SquareWave();
        test.duty = 2;
        test.frequency = 900;
        test.playing = true;
        test.lengthLoad = 256;
        test.lengthCounter = 256;
        test.lengthEnabled = true;
        test.updateSound();
        Thread.sleep(1000000);
    }
    
    public SquareWave() {
        this.lengthDecreaser.start();
    }
    
    public static int getWaveform(int duty) {
        switch(duty){
            case 0:
                return 0x01;
            case 1:
                return 0x81;
            case 2:
                return 0x87;
            case 3:
                return 0x7e;
            default:
                throw new IllegalArgumentException("duty must be in [0,4)");
        }
    }

    public void updateSound() {
        if(clip != null) clip.stop();
        
        if(!this.playing){
            return;
        }
        
        int chunkSize = (2048 - frequency); //number of samples before switching things up
        byte[] soundBuffer = new byte[8 * chunkSize];
        int waveForm = getWaveform(duty);
        
        for(int i = 0; i < 8; i++){
            byte toWrite = (((waveForm >> i) & 1) == 1)? (byte)127: (byte)-128;
            for(int j = 0; j < chunkSize; j++){
                soundBuffer[i * chunkSize + j] = toWrite;
            }
        }
        
        try {
            clip = AudioSystem.getClip();
            
            clip.open(AUDIO_FORMAT, soundBuffer, 0, soundBuffer.length);
            
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            
            clip.start();
            
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    //location is 0, 1, 2, 3, 4
    public void handleByte(int location, int toWrite) {
        switch(location){
            case 0:
                //do nothing
                break;
            case 1:
                this.duty = (toWrite >> 6) & 0x3;
                this.lengthLoad = toWrite & 0x3f;
                break;
            case 2:
                this.startingVolume = (toWrite >> 4) & 0xf;
                this.envelopeAdd = ((toWrite >> 3) & 1) == 1;
                this.envelopePeriod = toWrite & 0x7;
                break;
            case 3:
                this.frequency = (this.frequency >> 8) << 8;
                this.frequency |= toWrite & 0xff;
                break;
            case 4:
                this.playing = (toWrite >> 7) == 1;
                this.lengthEnabled = (toWrite >> 6) == 1;
                this.frequency &= 0xff;
                this.frequency |= ((toWrite & 0x7) << 8);
        }
        
        if(this.lengthEnabled){
            this.lengthCounter = this.lengthLoad;
            System.out.println(this.lengthCounter);
        }
        
        updateSound();
    }
}