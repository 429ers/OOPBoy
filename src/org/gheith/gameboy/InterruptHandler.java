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
        
        cpu.PUSH(cpu.regs.PC);
        cpu.regs.PC.write(handle);
    }
}
