package org.the429ers.gameboy;

import java.io.Serializable;

public class LinkCable implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 6027951294304800594L;
    private boolean isTransferring;
    private char currentData;
    private MMU mem;
    private InterruptHandler interruptHandler;
    private int counter;
    
    public LinkCable(MMU mem, InterruptHandler interruptHandler) {
        this.mem = mem;
        this.interruptHandler = interruptHandler;
    }
    
    public void tick() {
        int serialTransferData = mem.readByte(0xFF01) & 0xFF;
        int serialTransferControl = mem.readByte(0xFF02) & 0xFF;

        
        if (serialTransferControl == 0x81 && !isTransferring) {
            currentData = (char) serialTransferData;
            //System.out.println("here");
            System.out.print(currentData);
            System.out.flush();
            isTransferring = true;
        }
        if (isTransferring){
            counter++;
            if (counter == 8) {
                counter = 0;
                isTransferring = false;
                mem.writeByte(0xFF02, 0x1);
                //interruptHandler.issueInterruptIfEnabled(InterruptHandler.SERIAL_COMPLETION);
            }
        }
    }
}
