package org.gheith.gameboy;

import javax.sound.sampled.*;

public interface SoundChannel {
    void handleByte(int location, int toWrite);
    void tick();
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
    
    private int currentVolume = 0;
    private long ticks = 0;
    
    private byte[] soundBuffer = new byte[SAMPLE_RATE];
    private byte[] zeros = new byte[SAMPLE_RATE];
    
    private SourceDataLine sourceDL;
    
    private static final int SAMPLE_RATE = 131072 / 3;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

    SquareWave() {
        try {
            sourceDL = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
            sourceDL.open(AUDIO_FORMAT);
            sourceDL.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static int getWaveform(int duty) {
        switch(duty){
            case 0:
                return 0b00100000;
            case 1:
                return 0b00110000;
            case 2:
                return 0b00111100;
            case 3:
                return 0b01111110;
            default:
                throw new IllegalArgumentException("duty must be in [0,4)");
        }
    }

    public void tick() {
        if (!this.playing) {
            sourceDL.stop();
            return;
        }else{
            sourceDL.start();
        }
        if (sourceDL == null) {
            return;
        }
        
        ticks++;

        if (lengthEnabled) {
            lengthCounter-= 4;
            if (lengthCounter <= 0) {
                this.playing = false;
            }
        }
        
        if(envelopePeriod != 0 && ticks % envelopePeriod == 0) {
            this.currentVolume += (envelopeAdd? 3: -3);
            if(this.currentVolume < 0) this.currentVolume = 0;
            if(this.currentVolume > 15) this.currentVolume = 15;
        }
        
        sourceDL.flush();
        
        int waveForm = getWaveform(this.duty);
        double chunkSize = (2048.0 - frequency) / 8 / 3;
        
        int waveLength = (int)(8 * chunkSize);
        
        for(int i = 0; i < waveLength; i++){
            int loc = (int)Math.round(i / chunkSize);
            soundBuffer[i] = (((waveForm >> loc) & 1) == 1)? (byte)(currentVolume): (byte)(-currentVolume);
        }
        
        int samplesToWrite = sourceDL.available();
        int samplesWritten;
        for(samplesWritten = 0; samplesWritten < samplesToWrite - waveLength; samplesWritten += waveLength){
            sourceDL.write(soundBuffer, 0, waveLength);
        }
        
        sourceDL.write(zeros, 0, samplesToWrite - samplesWritten);
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
                this.currentVolume = this.startingVolume;
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
    }
}