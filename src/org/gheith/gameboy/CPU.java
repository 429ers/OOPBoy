package org.gheith.gameboy;

import java.security.InvalidParameterException;

class ReadablePlusOne implements Readable {
    Readable wrapped;
    
    @Override
    public int read() {
        return wrapped.read() + 1;
    }

    public ReadablePlusOne(Readable r){
        wrapped = r;
    }
}

public class CPU {
    
    Memory mem = new Memory();
    RegisterFile regs = new RegisterFile();

    public static final int ZFLAG = RegisterFile.ZFLAG;
    public static final int NFLAG = RegisterFile.NFLAG;
    public static final int HFLAG = RegisterFile.HFLAG;
    public static final int CFLAG = RegisterFile.CFLAG;
    
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
    
    int ADD(ShortRegister dest, Readable src){
        int op1 = src.read(), op2 = dest.read();
        
        int sum = op1 + op2;
        int result = sum & 0xff;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        regs.flags.setFlag(CFLAG, (sum != result));
        regs.flags.setFlag(HFLAG, ((op1 & 0xf) + (op1 & 0xf) > 0xf) );
        
        dest.write(result);
        
        return result;
    }
    
    int ADD(LongRegister dest, Readable src){
        int op1 = src.read(), op2 = dest.read();

        int sum = op1 + op2;
        int result = sum & 0xffff;

        regs.flags.setFlag(ZFLAG, (result == 0));
        regs.flags.setFlag(CFLAG, (sum != result));
        regs.flags.setFlag(HFLAG, ((op1 & 0xff) + (op1 & 0xff) > 0xff));

        dest.write(result);
        
        return result;
    }

    int ADC(Register dest, Readable src){
        if(regs.flags.getFlag(CFLAG)) {
            src = new ReadablePlusOne(src);
        }
        
        if(dest instanceof LongRegister){
            return ADD((LongRegister) dest, src);
        }else{
            return ADD((ShortRegister) dest, src);
        }
    }
    
    //saves result in A
    int SUB(Readable toSubtract){
        int op1 = regs.A.read();
        int op2 = toSubtract.read();
        
        int diff = op1 - op2;
        int result = diff & 0xff;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        regs.flags.setFlag(CFLAG, (diff < 0)); //set if needed borrow
        regs.flags.setFlag(HFLAG, ((op1 & 0xf) - (op2 & 0xf) < 0)); //set if needs borrow from 4th bit
        //seems like GBCPUman is wrong?
        
        regs.A.write(result);
        
        return result;
    }
    
    //result in A
    int SBC(Readable toSubtract){
        if(regs.flags.getFlag(CFLAG)){
            toSubtract = new ReadablePlusOne(toSubtract);
        }
        
        return SUB(toSubtract);
    }
    
    //result in A
    int AND(Readable op){
        int op1 = regs.A.read();
        int op2 = op.read();

        if((op2 & 0xff) != op2) throw new InvalidParameterException("operand must be byte");
        
        int result = op1 & op2;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        
        regs.A.write(result);
        
        return result;
    }
    
    //result in A
    int OR(Readable op){
        int op1 = regs.A.read();
        int op2 = op.read();
        
        if((op2 & 0xff) != op2) throw new InvalidParameterException("operand must be byte");
        
        int result = op1 | op2;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        
        regs.A.write(result);
        
        return result;
    }
    
    //result in A
    int XOR(Readable op) {
        int op1 = regs.A.read();
        int op2 = op.read();

        if((op2 & 0xff) != op2) throw new InvalidParameterException("operand must be byte");

        int result = op1 ^ op2;

        regs.flags.setFlag(ZFLAG, (result == 0));

        regs.A.write(result);

        return result;
    }
    
    //result discarded
    int CP(Readable n) {
        int originalA = regs.A.read();
        
        int result = SUB(n);
        
        regs.A.write(originalA);
        
        return result;
    }
    
    //increments toInc
    int INC(ReadWritable toInc){
        int original = toInc.read();
        int result = original+1;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        regs.flags.setFlag(HFLAG, ((original & 0xf) + 1) > 0xf);
        //apparently C-flag is not affected
        
        toInc.write(result);
        
        return result;
    }
    
    //decrements toDev
    int DEC(ReadWritable toDec){
        int original = toDec.read();
        int result = original - 1;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        regs.flags.setFlag(HFLAG, (original & 0xf) < 1); //needs borrow from bit 4
        //C not affected
        
        toDec.write(result);
        
        return result;
    }
    
    //swaps upper and lower nibbles of op, which is a byte
    int SWAP(ReadWritable op){
        int original = op.read();
        
        if((original & 0xff) != original) throw new InvalidParameterException("operand must be byte");
        
        int upperNibble = (original & 0xf0) >> 4;
        int lowerNibble = (original & 0xf);
        
        int result = (lowerNibble << 4) | upperNibble;
        
        regs.flags.setFlag(ZFLAG, (result == 0));
        
        op.write(result);
        
        return result;
    }
}
