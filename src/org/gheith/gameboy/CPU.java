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
    
    enum Condition {
        NZ, Z, NC, C
    }
    
    boolean evaluateCondition(Condition c) {
        switch(c){
            case NZ:
                return !regs.flags.getFlag(ZFLAG);
            case Z:
                return regs.flags.getFlag(ZFLAG);
            case NC:
                return !regs.flags.getFlag(CFLAG);
            case C:
                return regs.flags.getFlag(CFLAG);
        }
        
        throw new InvalidParameterException("this shouldn't happen");
    }
    
    boolean halted = false;
    
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
    
    int DAA() {
        int original = regs.A.read();
        int decimalVal = original % 100;
        
        int tens = decimalVal / 10;
        int ones = decimalVal % 10;
        
        int result = (tens << 4) | ones;
        
        regs.flags.setFlag(ZFLAG, (decimalVal == 0)); //may be wrong
        regs.flags.setFlag(CFLAG, decimalVal == original);
        
        regs.A.write(result);
        
        return result;
    }
    
    int CPL() {
        int original = regs.A.read();
        int result = (~original) & 0xff;
        
        regs.A.write(result);
        
        return result;
    }
    
    int CCF() {
        regs.flags.setFlag(CFLAG, !regs.flags.getFlag(CFLAG));
        
        return 0;
    }
    
    int SCF() {
        regs.flags.setFlag(CFLAG, true);
        
        return 0;
    }
    
    int NOP(){
        return 0;
    }
    
    int HALT() {
        halted = true;
        
        return 0;
    }
    
    int STOP() {
        halted = true;
        
        return 0;
    }
    
    int DI() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    int EI() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    //rotates op left by one bit, puts 7th bit in C
    int RLC(ReadWritable op){
        int original = op.read();
        int bit7 = (original >> 7) & 1;
        
        regs.flags.setFlag(CFLAG, bit7 == 1);
        
        int result = (original << 1) | bit7;
        
        regs.flags.setFlag(ZFLAG, result == 0);
        
        op.write(result);
        
        return result;
    }
    
    //rotates op left, with C treated as bit 8
    int RL(ReadWritable op) {
        int original = op.read();
        int bit7 = (original >> 7) & 1;
        
        int carryBit = (regs.flags.getFlag(CFLAG)? 1 : 0);
        
        int result = (original << 1) | carryBit;
        
        regs.flags.setFlag(CFLAG, bit7 == 1);
        regs.flags.setFlag(ZFLAG, result == 0);
        
        op.write(result);
        
        return result;
    }
    
    //rotates op right, C holds original 0th bit
    int RRC(ReadWritable op){
        int original = op.read();
        int bit0 = original & 1;
        
        regs.flags.setFlag(CFLAG, bit0 == 1);
        
        int result = (original >> 1) | (bit0 << 7);
        
        regs.flags.setFlag(ZFLAG, result == 0);
        
        op.write(result);
        
        return result;
    }
    
    //rotates op right, with C treated as the -1th bit
    int RR(ReadWritable op){
        int original = op.read();
        int bit0 = original & 1;

        int carryBit = (regs.flags.getFlag(CFLAG)? 1 : 0);
        
        int result = (original >> 1) | (carryBit << 7);
        
        regs.flags.setFlag(CFLAG, bit0 == 1);
        regs.flags.setFlag(ZFLAG, result == 0);
        
        op.write(result);
        
        return result;
    }
    
    int SLA(ReadWritable op){
        int original = op.read();
        int bit7 = (original >> 7) & 1;

        int result = (original << 1);

        regs.flags.setFlag(CFLAG, bit7 == 1);
        regs.flags.setFlag(ZFLAG, result == 0);

        op.write(result);

        return result;
    }
    
    int SRA(ReadWritable op) {
        int original = op.read();
        int bit0 = original & 1;
        int bit7 = (original << 7) & 1;

        int result = (original >> 1) | (bit7 << 7);

        regs.flags.setFlag(CFLAG, bit0 == 1);
        regs.flags.setFlag(ZFLAG, result == 0);

        op.write(result);

        return result;
    }

    int SRL(ReadWritable op) {
        int original = op.read();
        int bit0 = original & 1;

        int result = (original >> 1);

        regs.flags.setFlag(CFLAG, bit0 == 1);
        regs.flags.setFlag(ZFLAG, result == 0);

        op.write(result);

        return result;
    }
    
    int BIT(int bitnum, Readable op) {
        int val = op.read();
        
        regs.flags.setFlag(ZFLAG, ((val >> bitnum) & 1) == 0);
        
        return val;
    }
    
    int SET(int bitnum, ReadWritable op) {
        int val = op.read();
        
        val |= (1 << bitnum);
        
        op.write(val);
        
        return val;
    }
    
    int RES(int bitnum, ReadWritable op) {
        int val = op.read();

        val &= ~(1 << bitnum);
        
        op.write(val);
        
        return val;
    }
    
    int JP(Readable jumpLocation) {
        int location = jumpLocation.read();
        
        regs.PC.write(location);
        
        return location;
    }
    
    int JP(Condition cond, Readable jumpLocation) {
        if(evaluateCondition(cond)){
            return JP(jumpLocation);
        }else{
            return 0;
        }
    }
    
    int JR(Readable offset){
        int location = regs.PC.read() + (byte)offset.read(); //the offset is signed
        
        regs.PC.write(location);
        
        return location;
    }
    
    int JR(Condition cond, Readable offset) {
        if(evaluateCondition(cond)){
            return JR(offset);
        }else{
            return 0;
        }
    }
    
    int CALL(Readable jumpLocation) {
        int nextPC = regs.PC.read() + 3; //CALL is 3 bytes long
        
        LongRegister temp = new LongRegister();
        temp.write(nextPC);

        //push next PC onto stack
        PUSH(temp);
        
        return JP(jumpLocation);
    }
    
    int CALL(Condition cond, Readable jumpLocation) {
        if(evaluateCondition(cond)){
            return CALL(jumpLocation);
        }else{
            return 0;
        }
    }
    
    //push current pc onto stack and jump to n
    int RST(int n){ //n = 0, 8, 16, 24, 32, ... 56
        PUSH(regs.PC);
        
        LongRegister temp = new LongRegister();
        temp.write(n);
        
        return JP(temp);
    }
    
    //pop two bytes from stack & jump there
    int RET(){
        LongRegister temp = new LongRegister();
        
        POP(temp);
        
        return JP(temp);
    }
    
    int RET(Condition cond){
        if(evaluateCondition(cond)){
            return RET();
        }else{
            return 0;
        }
    }
    
    //return while enabling interrupts
    int RETI(){
        EI();
        
        return RET();
    }
}
