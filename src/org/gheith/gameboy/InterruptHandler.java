package org.gheith.gameboy;

import java.util.HashMap;

public class InterruptHandler {
    private CPU cpu;
    
    public static final int VBLANK = 0x0040;
    public static final int LCDC = 0x0048;
    public static final int TIMER_OVERFLOW = 0x0050;
    public static final int SERIAL_COMPLETION = 0x0058;
    public static final int HIGH_TO_LOW = 0x0060;
    
    public HashMap<Integer, Boolean> specificEnabled = new HashMap<>();
    {
        specificEnabled.put(VBLANK, true);
        specificEnabled.put(LCDC, true);
        specificEnabled.put(TIMER_OVERFLOW, true);
        specificEnabled.put(SERIAL_COMPLETION, true);
        specificEnabled.put(HIGH_TO_LOW, true);
    }
    
    private boolean interruptsEnabled = false; // this is the IME flag
    
    public void setInterruptsEnabled(boolean interruptsEnabled){
        this.interruptsEnabled = interruptsEnabled;
    }
    
    public void setSpecificEnabled(int handle, boolean enabled){
        specificEnabled.put(handle, enabled);
    }
    
    public InterruptHandler(CPU cpu){
        this.cpu = cpu;
    }
    
    public void issueInterruptIfEnabled(int handle){
        if(!interruptsEnabled) {
            return;
        }
        if(!specificEnabled.getOrDefault(handle, false)) return;
        
        interruptsEnabled = false;
        
        cpu.PUSH(cpu.regs.PC);
        cpu.regs.PC.write(handle);
    }
    
    public void handleIF(int IFflag){
        //read IFflag bit by bit based on priority and issue an interrupt if needed
        
        if((IFflag & 1) == 1){ //VBLANK requested
            this.issueInterruptIfEnabled(InterruptHandler.VBLANK);
            return; //lower priority simultaneous interrupts are ignored
        }
        IFflag >>= 1;
        if((IFflag & 1) == 1){ 
            this.issueInterruptIfEnabled(InterruptHandler.LCDC);
            return;
        }
        IFflag >>= 1;
        if((IFflag & 1) == 1){ 
            this.issueInterruptIfEnabled(InterruptHandler.TIMER_OVERFLOW);
            return;
        }
        IFflag >>= 1;
        if((IFflag & 1) == 1){
            this.issueInterruptIfEnabled(InterruptHandler.SERIAL_COMPLETION);
            return;
        }
        IFflag >>= 1;
        if((IFflag & 1) == 1){
            this.issueInterruptIfEnabled(InterruptHandler.HIGH_TO_LOW);
        }
    }
    
    public void handleIE(int IEflag) {
        //read IEflag bit by bit and set each interrupt enable depending on IEflag value
        this.setSpecificEnabled(InterruptHandler.VBLANK, (IEflag & 1) == 1); //enable vblank if bit 0 is 1
        IEflag >>= 1;
        this.setSpecificEnabled(InterruptHandler.LCDC, (IEflag & 1) == 1); //ditto
        IEflag >>= 1;
        this.setSpecificEnabled(InterruptHandler.TIMER_OVERFLOW, (IEflag & 1) == 1);
        IEflag >>= 1;
        this.setSpecificEnabled(InterruptHandler.SERIAL_COMPLETION, (IEflag & 1) == 1);
        IEflag >>= 1;
        this.setSpecificEnabled(InterruptHandler.HIGH_TO_LOW, (IEflag & 1) == 1);
    }
}
