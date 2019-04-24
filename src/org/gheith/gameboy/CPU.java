package org.gheith.gameboy;

public class CPU {
    
    Memory mem = new Memory();
    RegisterFile regs = new RegisterFile();
    
    int LD (Writable dest, Readable src){
        int val = src.read();
        dest.write(val);
        return val;
    }
    
    int PUSH(LongRegister reg) {
        int sp = regs.SP.read();
        
        sp--;
        mem.writeByte(sp, reg.upperByte.read());
        
        sp--;
        mem.writeByte(sp, reg.lowerByte.read());

        regs.SP.write(sp);
        
        return reg.read();
    }
    
    int POP(LongRegister reg){
        int sp = regs.SP.read();
        
        reg.lowerByte.write(mem.readByte(sp));
        sp++;
        
        reg.upperByte.write(mem.readByte(sp));
        sp++;
        
        regs.SP.write(sp);
        
        return reg.read();
    }
    
    int ADD(Register dest, Readable src){
        int op1 = src.read(), op2 = dest.read();
        
        int sum = op1 + op2;
        int result = sum & 0xff;
        
        //TODO: stub
        
        return op1+op2;
    }
    
    int ADD(LongRegister dest, Readable src){
        int op1 = src.read(), op2 = dest.read();
        
        //TODO: stub
        
        return op1+op2;
    }
}
