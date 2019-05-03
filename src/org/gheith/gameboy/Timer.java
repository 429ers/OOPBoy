package org.gheith.gameboy;

import java.io.Serializable;

public class Timer implements Serializable {
    private InterruptHandler interruptHandler;
    public static final int[] PERIODS = new int[] { //number of cpu cycles until update
            1024, //4.096 khz
            16, //262.144 khz
            64, //65.536 khz
            256 //16.384 khz
    };
    public static final int DIV_PERIOD = 256;
    
    private boolean timerEnabled = false;
    private int currentClock = 0;
    private int modulo = 0;
    
    private int counter = 0; //the small counter for TIMA
    private int countRegister = 0; //the big counter for TIMA
    private int divCounter = 0; //the small counter for DIV
    private int divRegister = 0; //the big counter for DIV
    
    public Timer(InterruptHandler interruptHandler) {
        this.interruptHandler = interruptHandler;
    }
    
    public void tick() {
        divCounter++;
        if(divCounter >= DIV_PERIOD){
            divRegister = (divRegister + 1) & 0xff;
            divCounter = 0;
        }
        
        if(timerEnabled){
            counter++;
            if(counter >= PERIODS[currentClock]){
                countRegister++;
                if(countRegister > 0xff){
                    countRegister = modulo;
                    interruptHandler.issueInterruptIfEnabled(InterruptHandler.TIMER_OVERFLOW);
                }
                counter = 0;
            }
        }
    }
    
    public void handleTAC(int TAC) {
        this.timerEnabled = ((TAC >> 2) & 1) == 1;
        
        this.currentClock = (TAC & 0x3);
    }
    
    public void setModulo(int modulo){
        this.modulo = modulo & 0xff;
    }
    
    public void setTIMA(int countRegister){
        this.countRegister = countRegister;
    }
    
    public void resetDIV(){
        this.divRegister = 0;
    }
    
    public int getDIV() {
        return divRegister;
    }
    
    public int getTIMA() {
        return countRegister;
    }
}
