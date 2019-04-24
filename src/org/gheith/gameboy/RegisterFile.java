package org.gheith.gameboy;

interface Register {
    int read();
    void write(int val);
}

class LongRegister implements Register {
    private int value;
    Register lowerByte = new Register() {
        @Override
        public int read() {
            return LongRegister.this.value & 0xff;
        }

        @Override
        public void write(int val) {
            val &= 0xff; //truncate value to single byte
            LongRegister.this.value &= 0xff00; //reset lower byte
            LongRegister.this.value |= val; //set lower byte to value
        }
    };

    Register upperByte = new Register() {
        @Override
        public int read() {
            return (LongRegister.this.value & 0xff00) >> 8;
        }

        @Override
        public void write(int val) {
            val = (val & 0xff) << 8; //truncate value to single byte and move it up a byte
            LongRegister.this.value &= 0x00FF; //reset upper byte
            LongRegister.this.value |= val; //set upper byte to value
        }
    };

    public int read() {
        return this.value;
    }

    public void write(int val){
        this.value = val & 0xffff;
    }
}

public class RegisterFile {
    public LongRegister AF, BC, DE, HL, SP, PC;
    public Register A, F, B, C, D, E, H, L;

    public RegisterFile(){
        AF = new LongRegister();
        A = AF.upperByte;
        F = AF.lowerByte;
        
        BC = new LongRegister();
        B = BC.upperByte;
        C = BC.lowerByte;
        
        DE = new LongRegister();
        D = DE.upperByte;
        E = DE.lowerByte;
        
        HL = new LongRegister();
        H = HL.upperByte;
        L = HL.lowerByte;
        
        SP = new LongRegister();
        PC = new LongRegister();
    }
}
