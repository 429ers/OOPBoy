package org.gheith.gameboy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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

interface Lambda{
    int exec();
}

public class CPU {
    
    MMU mem = new MMU();
    RegisterFile regs = new RegisterFile();
    InterruptHandler interruptHandler = new InterruptHandler(this);

    public int getClockCycles() {
        return clockCycles;
    }

    private int clockCycles = 0;

    public static final int ZFLAG = RegisterFile.ZFLAG;
    public static final int NFLAG = RegisterFile.NFLAG;
    public static final int HFLAG = RegisterFile.HFLAG;
    public static final int CFLAG = RegisterFile.CFLAG;
    
    public static final int NOJUMP = -1;
    public static final int RELJUMP = 0;
    public static final int ABSJUMP = 1;

    public static void main(String[] args) throws IOException {
        //System.setOut(new PrintStream(new File("test.out")));
        
        new CPU().test();
    }
    
    public void test() {
        while(regs.PC.read() < 256){
            executeOneInstruction(true);
            
        }
    }
    
    public void executeOneInstruction(boolean printOutput) {
        int opcode = mem.readByte(regs.PC.read());
        
        Operation op = operations[opcode];
        
        int result = op.execute();
        
        if(printOutput) {
            System.out.println(regs.PC + ": " + op.description);

            System.out.println("result: " + result);

            regs.dump();
        }
    }

    class Operation{ //any operation that is not a jump
        String description;
        Lambda lambda;
        int ticks;
        int length; //length of operation in bytes
        String flagsAffected; //e.g. "- - - -"
        public Operation(String description, Lambda lambda, int length, String flagsAffected, int ticks){
            this.description = description;
            this.lambda = lambda;
            this.ticks = ticks;
            this.length = length;
            this.flagsAffected = flagsAffected;
        }
        
        //sets the flags to the values they need to be and allows flags to be written to only when the instruction should affect the flag
        protected void handleFlags() {
            final int[] flags = new int[] { ZFLAG, NFLAG, HFLAG, CFLAG };
            boolean[] writable = new boolean[8];
            
            regs.flags.enableFlagWrites(true, true, true, true);
            
            for(int i = 0; i < flagsAffected.length(); i+= 2) {
                char descriptor = flagsAffected.charAt(i);
                
                int flag = flags[i / 2];
                
                switch(descriptor){
                    case '0':
                        regs.flags.setFlag(flag, false);
                        writable[flag] = false;
                        break;
                    case '1':
                        regs.flags.setFlag(flag, true);
                        writable[flag] = false;
                        break;
                    case '-':
                        writable[flag] = false;
                        break;
                    default:
                        writable[flag] = true;
                }
            }
            
            regs.flags.enableFlagWrites(writable[ZFLAG], writable[NFLAG], writable[HFLAG], writable[CFLAG]);
        }

        public int execute() {
            handleFlags();
            
            int result = this.lambda.exec();
            
            CPU.this.clockCycles += this.ticks;
            CPU.this.regs.PC.write(CPU.this.regs.PC.read() + length);
            
            return result;
        }
    }
    
    class Jump extends Operation{//this includes relative, conditional, calls, and returns
        int ticksIfJumped, ticksIfNotJumped;
        public Jump(String description, Lambda lambda, int length, String flagsAffected, int ticksIfJumped, int ticksIfNotJumped) {
            super(description, lambda, length, flagsAffected, ticksIfJumped);
            this.ticksIfJumped = ticksIfJumped;
            this.ticksIfNotJumped = ticksIfNotJumped;
        }

        public int execute() {
            handleFlags();
            
            int result = this.lambda.exec();

            if (result == RELJUMP || result == NOJUMP){
                //apparently offsets are calculated based on the future PC
                CPU.this.regs.PC.write(CPU.this.regs.PC.read() + length);
            }

            if(result == NOJUMP) {
                CPU.this.clockCycles += this.ticksIfNotJumped;
            }else{
                CPU.this.clockCycles += this.ticksIfJumped;
            }
            
            return result;
        }
    }
    
    enum Condition {
        NZ, Z, NC, C
    }
    
    //represents an 8-bit immediate value. assumes it's placed right after PC
    Readable d8() {
        int value = mem.readByte(regs.PC.read()+1);

        return new Readable() {
            @Override
            public int read() {
                return value;
            }
        };
    }

    //represents an 8-bit signed immediate value, which is added to 0xff00
    Readable a8() {
        int value = 0xff00 + mem.readByte(regs.PC.read()+1);

        return new Readable() {
            @Override
            public int read() {
                return value;
            }
        };
    }

    //represents a 16-bit immediate value right after PC
    Readable d16() {
        int value = mem.readWord(regs.PC.read()+1);

        return new Readable() {
            @Override
            public int read() {
                return value;
            }
        };
    }
    
    //represents a 16-bit address right after PC
    Readable a16() {
        return d16();
    }
    
    // a wrapper around a register that automatically increments itself after being read or written to
    // selfIncrement(regs.HL) := (HL+)
    ReadWritable selfIncrement(LongRegister reg){
        return new ReadWritable() {
            boolean incremented = false;
            @Override
            public int read() {
                if(!incremented){
                    incremented = true;
                    reg.write(reg.read() + 1);
                }
                return reg.read() - 1;
            }

            @Override
            public void write(int val) {
                if(!incremented){
                    incremented = true;
                    reg.write(val + 1);
                }else{
                    reg.write(val);
                }
            }
        };
    }

    //same as above, but decrements instead
    ReadWritable selfDecrement(LongRegister reg){
        return new ReadWritable() {
            boolean decremented = false;
            @Override
            public int read() {
                if(!decremented){
                    decremented = true;
                    reg.write(reg.read() -1);
                }
                return reg.read() + 1;
            }

            @Override
            public void write(int val) {
                if(!decremented){
                    decremented = true;
                    reg.write(val - 1);
                }else{
                    reg.write(val);
                }
            }
        };
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
    
    int XXX() {
        throw new IllegalArgumentException("invalid opcode");
    }
    
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
        
        System.out.println("turn off the display lmao");
        
        return 0;
    }
    
    int DI() {
        this.interruptHandler.setInterruptsEnabled(false);
        return 0;
    }
    
    int EI() {
        this.interruptHandler.setInterruptsEnabled(true);
        return 1;
    }
    
    int RLCA() {
        return RLC(regs.A);
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
    
    int RLA(){
        return RL(regs.A);
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
    
    int RRCA(){
        return RRC(regs.A);
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
    
    int RRA(){
        return RR(regs.A);
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
        
        return ABSJUMP;
    }
    
    int JP(Condition cond, Readable jumpLocation) {
        if(evaluateCondition(cond)){
            return JP(jumpLocation);
        }else{
            return NOJUMP;
        }
    }
    
    int JR(Readable offset){
        int location = regs.PC.read() + (byte)offset.read(); //the offset is signed
        
        regs.PC.write(location);
        
        return RELJUMP;
    }
    
    int JR(Condition cond, Readable offset) {
        if(evaluateCondition(cond)){
            return JR(offset);
        }else{
            return NOJUMP;
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
            return NOJUMP;
        }
    }
    
    //push next pc onto stack and jump to n
    int RST(int n){ //n = 0, 8, 16, 24, 32, ... 56
        LongRegister nextPC = new LongRegister();
        nextPC.write(regs.PC.read() + 1); //an RST instruction is one byte long
        
        PUSH(nextPC);
        
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
            return NOJUMP;
        }
    }
    
    //return while enabling interrupts
    int RETI(){
        EI();
        
        return RET();
    }
    
    int CB() {
        int cbOpcode = mem.readByte(regs.PC.read() + 1); //the cb opcode follows directly after cb
        Operation cbOperation = cbOperations[cbOpcode];
        cbOperation.length = 1; //the operation increases the PC by 1 because CB already increases it by 1
        
        return cbOperation.execute();
    }

    Operation[] operations = new Operation[256];
    {
        operations[0x0] = new Operation("NOP", this::NOP, 1, "- - - -", 4);
        operations[0x1] = new Operation("LD BC,d16", () -> LD(regs.BC, d16()), 3, "- - - -", 12);
        operations[0x2] = new Operation("LD (BC),A", () -> LD(mem.registerLocation(regs.BC), regs.A), 1, "- - - -", 8);
        operations[0x3] = new Operation("INC BC", () -> INC(regs.BC), 1, "- - - -", 8);
        operations[0x4] = new Operation("INC B", () -> INC(regs.B), 1, "Z 0 H -", 4);
        operations[0x5] = new Operation("DEC B", () -> DEC(regs.B), 1, "Z 1 H -", 4);
        operations[0x6] = new Operation("LD B,d8", () -> LD(regs.B, d8()), 2, "- - - -", 8);
        operations[0x7] = new Operation("RLCA", this::RLCA, 1, "0 0 0 C", 4);
        operations[0x8] = new Operation("LD (a16),SP", () -> LD(mem.a16Location(regs.PC), regs.SP), 3, "- - - -", 20);
        operations[0x9] = new Operation("ADD HL,BC", () -> ADD(regs.HL, regs.BC), 1, "- 0 H C", 8);
        operations[0xa] = new Operation("LD A,(BC)", () -> LD(regs.A, mem.registerLocation(regs.BC)), 1, "- - - -", 8);
        operations[0xb] = new Operation("DEC BC", () -> DEC(regs.BC), 1, "- - - -", 8);
        operations[0xc] = new Operation("INC C", () -> INC(regs.C), 1, "Z 0 H -", 4);
        operations[0xd] = new Operation("DEC C", () -> DEC(regs.C), 1, "Z 1 H -", 4);
        operations[0xe] = new Operation("LD C,d8", () -> LD(regs.C, d8()), 2, "- - - -", 8);
        operations[0xf] = new Operation("RRCA", this::RRCA, 1, "0 0 0 C", 4);
        operations[0x10] = new Operation("STOP", this::STOP, 2, "- - - -", 4);
        operations[0x11] = new Operation("LD DE,d16", () -> LD(regs.DE, d16()), 3, "- - - -", 12);
        operations[0x12] = new Operation("LD (DE),A", () -> LD(mem.registerLocation(regs.DE), regs.A), 1, "- - - -", 8);
        operations[0x13] = new Operation("INC DE", () -> INC(regs.DE), 1, "- - - -", 8);
        operations[0x14] = new Operation("INC D", () -> INC(regs.D), 1, "Z 0 H -", 4);
        operations[0x15] = new Operation("DEC D", () -> DEC(regs.D), 1, "Z 1 H -", 4);
        operations[0x16] = new Operation("LD D,d8", () -> LD(regs.D, d8()), 2, "- - - -", 8);
        operations[0x17] = new Operation("RLA", this::RLA, 1, "0 0 0 C", 4);
        operations[0x18] = new Jump("JR r8", () -> JR(d8()), 2, "- - - -", 12, 12);
        operations[0x19] = new Operation("ADD HL,DE", () -> ADD(regs.HL, regs.DE), 1, "- 0 H C", 8);
        operations[0x1a] = new Operation("LD A,(DE)", () -> LD(regs.A, mem.registerLocation(regs.DE)), 1, "- - - -", 8);
        operations[0x1b] = new Operation("DEC DE", () -> DEC(regs.DE), 1, "- - - -", 8);
        operations[0x1c] = new Operation("INC E", () -> INC(regs.E), 1, "Z 0 H -", 4);
        operations[0x1d] = new Operation("DEC E", () -> DEC(regs.E), 1, "Z 1 H -", 4);
        operations[0x1e] = new Operation("LD E,d8", () -> LD(regs.E, d8()), 2, "- - - -", 8);
        operations[0x1f] = new Operation("RRA", this::RRA, 1, "0 0 0 C", 4);
        operations[0x20] = new Jump("JR NZ,r8", () -> JR(Condition.NZ, d8()), 2, "- - - -", 12, 8);
        operations[0x21] = new Operation("LD HL,d16", () -> LD(regs.HL, d16()), 3, "- - - -", 12);
        operations[0x22] = new Operation("LD (HL+),A", () -> LD(mem.registerLocation(selfIncrement(regs.HL)), regs.A), 1, "- - - -", 8);
        operations[0x23] = new Operation("INC HL", () -> INC(regs.HL), 1, "- - - -", 8);
        operations[0x24] = new Operation("INC H", () -> INC(regs.H), 1, "Z 0 H -", 4);
        operations[0x25] = new Operation("DEC H", () -> DEC(regs.H), 1, "Z 1 H -", 4);
        operations[0x26] = new Operation("LD H,d8", () -> LD(regs.H, d8()), 2, "- - - -", 8);
        operations[0x27] = new Operation("DAA", this::DAA, 1, "Z - 0 C", 4);
        operations[0x28] = new Jump("JR Z,r8", () -> JR(Condition.Z, d8()), 2, "- - - -", 12, 8);
        operations[0x29] = new Operation("ADD HL,HL", () -> ADD(regs.HL, regs.HL), 1, "- 0 H C", 8);
        operations[0x2a] = new Operation("LD A,(HL+)", () -> LD(regs.A, mem.registerLocation(selfIncrement(regs.HL))), 1, "- - - -", 8);
        operations[0x2b] = new Operation("DEC HL", () -> DEC(regs.HL), 1, "- - - -", 8);
        operations[0x2c] = new Operation("INC L", () -> INC(regs.L), 1, "Z 0 H -", 4);
        operations[0x2d] = new Operation("DEC L", () -> DEC(regs.L), 1, "Z 1 H -", 4);
        operations[0x2e] = new Operation("LD L,d8", () -> LD(regs.L, d8()), 2, "- - - -", 8);
        operations[0x2f] = new Operation("CPL", this::CPL, 1, "- 1 1 -", 4);
        operations[0x30] = new Jump("JR NC,r8", () -> JR(Condition.NC, d8()), 2, "- - - -", 12, 8);
        operations[0x31] = new Operation("LD SP,d16", () -> LD(regs.SP, d16()), 3, "- - - -", 12);
        operations[0x32] = new Operation("LD (HL-),A", () -> LD(mem.registerLocation(selfDecrement(regs.HL)), regs.A), 1, "- - - -", 8);
        operations[0x33] = new Operation("INC SP", () -> INC(regs.SP), 1, "- - - -", 8);
        operations[0x34] = new Operation("INC (HL)", () -> INC(mem.registerLocation(regs.HL)), 1, "Z 0 H -", 12);
        operations[0x35] = new Operation("DEC (HL)", () -> DEC(mem.registerLocation(regs.HL)), 1, "Z 1 H -", 12);
        operations[0x36] = new Operation("LD (HL),d8", () -> LD(mem.registerLocation(regs.HL), d8()), 2, "- - - -", 12);
        operations[0x37] = new Operation("SCF", this::SCF, 1, "- 0 0 1", 4);
        operations[0x38] = new Jump("JR C(cond),r8", () -> JR(Condition.C, d8()), 2, "- - - -", 12, 8);
        operations[0x39] = new Operation("ADD HL,SP", () -> ADD(regs.HL, regs.SP), 1, "- 0 H C", 8);
        operations[0x3a] = new Operation("LD A,(HL-)", () -> LD(regs.A, mem.registerLocation(selfDecrement(regs.HL))), 1, "- - - -", 8);
        operations[0x3b] = new Operation("DEC SP", () -> DEC(regs.SP), 1, "- - - -", 8);
        operations[0x3c] = new Operation("INC A", () -> INC(regs.A), 1, "Z 0 H -", 4);
        operations[0x3d] = new Operation("DEC A", () -> DEC(regs.A), 1, "Z 1 H -", 4);
        operations[0x3e] = new Operation("LD A,d8", () -> LD(regs.A, d8()), 2, "- - - -", 8);
        operations[0x3f] = new Operation("CCF", this::CCF, 1, "- 0 0 C", 4);
        operations[0x40] = new Operation("LD B,B", () -> LD(regs.B, regs.B), 1, "- - - -", 4);
        operations[0x41] = new Operation("LD B,C", () -> LD(regs.B, regs.C), 1, "- - - -", 4);
        operations[0x42] = new Operation("LD B,D", () -> LD(regs.B, regs.D), 1, "- - - -", 4);
        operations[0x43] = new Operation("LD B,E", () -> LD(regs.B, regs.E), 1, "- - - -", 4);
        operations[0x44] = new Operation("LD B,H", () -> LD(regs.B, regs.H), 1, "- - - -", 4);
        operations[0x45] = new Operation("LD B,L", () -> LD(regs.B, regs.L), 1, "- - - -", 4);
        operations[0x46] = new Operation("LD B,(HL)", () -> LD(regs.B, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x47] = new Operation("LD B,A", () -> LD(regs.B, regs.A), 1, "- - - -", 4);
        operations[0x48] = new Operation("LD C,B", () -> LD(regs.C, regs.B), 1, "- - - -", 4);
        operations[0x49] = new Operation("LD C,C", () -> LD(regs.C, regs.C), 1, "- - - -", 4);
        operations[0x4a] = new Operation("LD C,D", () -> LD(regs.C, regs.D), 1, "- - - -", 4);
        operations[0x4b] = new Operation("LD C,E", () -> LD(regs.C, regs.E), 1, "- - - -", 4);
        operations[0x4c] = new Operation("LD C,H", () -> LD(regs.C, regs.H), 1, "- - - -", 4);
        operations[0x4d] = new Operation("LD C,L", () -> LD(regs.C, regs.L), 1, "- - - -", 4);
        operations[0x4e] = new Operation("LD C,(HL)", () -> LD(regs.C, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x4f] = new Operation("LD C,A", () -> LD(regs.C, regs.A), 1, "- - - -", 4);
        operations[0x50] = new Operation("LD D,B", () -> LD(regs.D, regs.B), 1, "- - - -", 4);
        operations[0x51] = new Operation("LD D,C", () -> LD(regs.D, regs.C), 1, "- - - -", 4);
        operations[0x52] = new Operation("LD D,D", () -> LD(regs.D, regs.D), 1, "- - - -", 4);
        operations[0x53] = new Operation("LD D,E", () -> LD(regs.D, regs.E), 1, "- - - -", 4);
        operations[0x54] = new Operation("LD D,H", () -> LD(regs.D, regs.H), 1, "- - - -", 4);
        operations[0x55] = new Operation("LD D,L", () -> LD(regs.D, regs.L), 1, "- - - -", 4);
        operations[0x56] = new Operation("LD D,(HL)", () -> LD(regs.D, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x57] = new Operation("LD D,A", () -> LD(regs.D, regs.A), 1, "- - - -", 4);
        operations[0x58] = new Operation("LD E,B", () -> LD(regs.E, regs.B), 1, "- - - -", 4);
        operations[0x59] = new Operation("LD E,C", () -> LD(regs.E, regs.C), 1, "- - - -", 4);
        operations[0x5a] = new Operation("LD E,D", () -> LD(regs.E, regs.D), 1, "- - - -", 4);
        operations[0x5b] = new Operation("LD E,E", () -> LD(regs.E, regs.E), 1, "- - - -", 4);
        operations[0x5c] = new Operation("LD E,H", () -> LD(regs.E, regs.H), 1, "- - - -", 4);
        operations[0x5d] = new Operation("LD E,L", () -> LD(regs.E, regs.L), 1, "- - - -", 4);
        operations[0x5e] = new Operation("LD E,(HL)", () -> LD(regs.E, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x5f] = new Operation("LD E,A", () -> LD(regs.E, regs.A), 1, "- - - -", 4);
        operations[0x60] = new Operation("LD H,B", () -> LD(regs.H, regs.B), 1, "- - - -", 4);
        operations[0x61] = new Operation("LD H,C", () -> LD(regs.H, regs.C), 1, "- - - -", 4);
        operations[0x62] = new Operation("LD H,D", () -> LD(regs.H, regs.D), 1, "- - - -", 4);
        operations[0x63] = new Operation("LD H,E", () -> LD(regs.H, regs.E), 1, "- - - -", 4);
        operations[0x64] = new Operation("LD H,H", () -> LD(regs.H, regs.H), 1, "- - - -", 4);
        operations[0x65] = new Operation("LD H,L", () -> LD(regs.H, regs.L), 1, "- - - -", 4);
        operations[0x66] = new Operation("LD H,(HL)", () -> LD(regs.H, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x67] = new Operation("LD H,A", () -> LD(regs.H, regs.A), 1, "- - - -", 4);
        operations[0x68] = new Operation("LD L,B", () -> LD(regs.L, regs.B), 1, "- - - -", 4);
        operations[0x69] = new Operation("LD L,C", () -> LD(regs.L, regs.C), 1, "- - - -", 4);
        operations[0x6a] = new Operation("LD L,D", () -> LD(regs.L, regs.D), 1, "- - - -", 4);
        operations[0x6b] = new Operation("LD L,E", () -> LD(regs.L, regs.E), 1, "- - - -", 4);
        operations[0x6c] = new Operation("LD L,H", () -> LD(regs.L, regs.H), 1, "- - - -", 4);
        operations[0x6d] = new Operation("LD L,L", () -> LD(regs.L, regs.L), 1, "- - - -", 4);
        operations[0x6e] = new Operation("LD L,(HL)", () -> LD(regs.L, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x6f] = new Operation("LD L,A", () -> LD(regs.L, regs.A), 1, "- - - -", 4);
        operations[0x70] = new Operation("LD (HL),B", () -> LD(mem.registerLocation(regs.HL), regs.B), 1, "- - - -", 8);
        operations[0x71] = new Operation("LD (HL),C", () -> LD(mem.registerLocation(regs.HL), regs.C), 1, "- - - -", 8);
        operations[0x72] = new Operation("LD (HL),D", () -> LD(mem.registerLocation(regs.HL), regs.D), 1, "- - - -", 8);
        operations[0x73] = new Operation("LD (HL),E", () -> LD(mem.registerLocation(regs.HL), regs.E), 1, "- - - -", 8);
        operations[0x74] = new Operation("LD (HL),H", () -> LD(mem.registerLocation(regs.HL), regs.H), 1, "- - - -", 8);
        operations[0x75] = new Operation("LD (HL),L", () -> LD(mem.registerLocation(regs.HL), regs.L), 1, "- - - -", 8);
        operations[0x76] = new Operation("HALT", this::HALT, 1, "- - - -", 4);
        operations[0x77] = new Operation("LD (HL),A", () -> LD(mem.registerLocation(regs.HL), regs.A), 1, "- - - -", 8);
        operations[0x78] = new Operation("LD A,B", () -> LD(regs.A, regs.B), 1, "- - - -", 4);
        operations[0x79] = new Operation("LD A,C", () -> LD(regs.A, regs.C), 1, "- - - -", 4);
        operations[0x7a] = new Operation("LD A,D", () -> LD(regs.A, regs.D), 1, "- - - -", 4);
        operations[0x7b] = new Operation("LD A,E", () -> LD(regs.A, regs.E), 1, "- - - -", 4);
        operations[0x7c] = new Operation("LD A,H", () -> LD(regs.A, regs.H), 1, "- - - -", 4);
        operations[0x7d] = new Operation("LD A,L", () -> LD(regs.A, regs.L), 1, "- - - -", 4);
        operations[0x7e] = new Operation("LD A,(HL)", () -> LD(regs.A, mem.registerLocation(regs.HL)), 1, "- - - -", 8);
        operations[0x7f] = new Operation("LD A,A", () -> LD(regs.A, regs.A), 1, "- - - -", 4);
        operations[0x80] = new Operation("ADD A,B", () -> ADD(regs.A, regs.B), 1, "Z 0 H C", 4);
        operations[0x81] = new Operation("ADD A,C", () -> ADD(regs.A, regs.C), 1, "Z 0 H C", 4);
        operations[0x82] = new Operation("ADD A,D", () -> ADD(regs.A, regs.D), 1, "Z 0 H C", 4);
        operations[0x83] = new Operation("ADD A,E", () -> ADD(regs.A, regs.E), 1, "Z 0 H C", 4);
        operations[0x84] = new Operation("ADD A,H", () -> ADD(regs.A, regs.H), 1, "Z 0 H C", 4);
        operations[0x85] = new Operation("ADD A,L", () -> ADD(regs.A, regs.L), 1, "Z 0 H C", 4);
        operations[0x86] = new Operation("ADD A,(HL)", () -> ADD(regs.A, mem.registerLocation(regs.HL)), 1, "Z 0 H C", 8);
        operations[0x87] = new Operation("ADD A,A", () -> ADD(regs.A, regs.A), 1, "Z 0 H C", 4);
        operations[0x88] = new Operation("ADC A,B", () -> ADC(regs.A, regs.B), 1, "Z 0 H C", 4);
        operations[0x89] = new Operation("ADC A,C", () -> ADC(regs.A, regs.C), 1, "Z 0 H C", 4);
        operations[0x8a] = new Operation("ADC A,D", () -> ADC(regs.A, regs.D), 1, "Z 0 H C", 4);
        operations[0x8b] = new Operation("ADC A,E", () -> ADC(regs.A, regs.E), 1, "Z 0 H C", 4);
        operations[0x8c] = new Operation("ADC A,H", () -> ADC(regs.A, regs.H), 1, "Z 0 H C", 4);
        operations[0x8d] = new Operation("ADC A,L", () -> ADC(regs.A, regs.L), 1, "Z 0 H C", 4);
        operations[0x8e] = new Operation("ADC A,(HL)", () -> ADC(regs.A, mem.registerLocation(regs.HL)), 1, "Z 0 H C", 8);
        operations[0x8f] = new Operation("ADC A,A", () -> ADC(regs.A, regs.A), 1, "Z 0 H C", 4);
        operations[0x90] = new Operation("SUB B", () -> SUB(regs.B), 1, "Z 1 H C", 4);
        operations[0x91] = new Operation("SUB C", () -> SUB(regs.C), 1, "Z 1 H C", 4);
        operations[0x92] = new Operation("SUB D", () -> SUB(regs.D), 1, "Z 1 H C", 4);
        operations[0x93] = new Operation("SUB E", () -> SUB(regs.E), 1, "Z 1 H C", 4);
        operations[0x94] = new Operation("SUB H", () -> SUB(regs.H), 1, "Z 1 H C", 4);
        operations[0x95] = new Operation("SUB L", () -> SUB(regs.L), 1, "Z 1 H C", 4);
        operations[0x96] = new Operation("SUB (HL)", () -> SUB(mem.registerLocation(regs.HL)), 1, "Z 1 H C", 8);
        operations[0x97] = new Operation("SUB A", () -> SUB(regs.A), 1, "Z 1 H C", 4);
        operations[0x98] = new Operation("SBC B", () -> SBC(regs.B), 1, "Z 1 H C", 4);
        operations[0x99] = new Operation("SBC C", () -> SBC(regs.C), 1, "Z 1 H C", 4);
        operations[0x9a] = new Operation("SBC D", () -> SBC(regs.D), 1, "Z 1 H C", 4);
        operations[0x9b] = new Operation("SBC E", () -> SBC(regs.E), 1, "Z 1 H C", 4);
        operations[0x9c] = new Operation("SBC H", () -> SBC(regs.H), 1, "Z 1 H C", 4);
        operations[0x9d] = new Operation("SBC L", () -> SBC(regs.L), 1, "Z 1 H C", 4);
        operations[0x9e] = new Operation("SBC (HL)", () -> SBC(mem.registerLocation(regs.HL)), 1, "Z 1 H C", 8);
        operations[0x9f] = new Operation("SBC A", () -> SBC(regs.A), 1, "Z 1 H C", 4);
        operations[0xa0] = new Operation("AND B", () -> AND(regs.B), 1, "Z 0 1 0", 4);
        operations[0xa1] = new Operation("AND C", () -> AND(regs.C), 1, "Z 0 1 0", 4);
        operations[0xa2] = new Operation("AND D", () -> AND(regs.D), 1, "Z 0 1 0", 4);
        operations[0xa3] = new Operation("AND E", () -> AND(regs.E), 1, "Z 0 1 0", 4);
        operations[0xa4] = new Operation("AND H", () -> AND(regs.H), 1, "Z 0 1 0", 4);
        operations[0xa5] = new Operation("AND L", () -> AND(regs.L), 1, "Z 0 1 0", 4);
        operations[0xa6] = new Operation("AND (HL)", () -> AND(mem.registerLocation(regs.HL)), 1, "Z 0 1 0", 8);
        operations[0xa7] = new Operation("AND A", () -> AND(regs.A), 1, "Z 0 1 0", 4);
        operations[0xa8] = new Operation("XOR B", () -> XOR(regs.B), 1, "Z 0 0 0", 4);
        operations[0xa9] = new Operation("XOR C", () -> XOR(regs.C), 1, "Z 0 0 0", 4);
        operations[0xaa] = new Operation("XOR D", () -> XOR(regs.D), 1, "Z 0 0 0", 4);
        operations[0xab] = new Operation("XOR E", () -> XOR(regs.E), 1, "Z 0 0 0", 4);
        operations[0xac] = new Operation("XOR H", () -> XOR(regs.H), 1, "Z 0 0 0", 4);
        operations[0xad] = new Operation("XOR L", () -> XOR(regs.L), 1, "Z 0 0 0", 4);
        operations[0xae] = new Operation("XOR (HL)", () -> XOR(mem.registerLocation(regs.HL)), 1, "Z 0 0 0", 8);
        operations[0xaf] = new Operation("XOR A", () -> XOR(regs.A), 1, "Z 0 0 0", 4);
        operations[0xb0] = new Operation("OR B", () -> OR(regs.B), 1, "Z 0 0 0", 4);
        operations[0xb1] = new Operation("OR C", () -> OR(regs.C), 1, "Z 0 0 0", 4);
        operations[0xb2] = new Operation("OR D", () -> OR(regs.D), 1, "Z 0 0 0", 4);
        operations[0xb3] = new Operation("OR E", () -> OR(regs.E), 1, "Z 0 0 0", 4);
        operations[0xb4] = new Operation("OR H", () -> OR(regs.H), 1, "Z 0 0 0", 4);
        operations[0xb5] = new Operation("OR L", () -> OR(regs.L), 1, "Z 0 0 0", 4);
        operations[0xb6] = new Operation("OR (HL)", () -> OR(mem.registerLocation(regs.HL)), 1, "Z 0 0 0", 8);
        operations[0xb7] = new Operation("OR A", () -> OR(regs.A), 1, "Z 0 0 0", 4);
        operations[0xb8] = new Operation("CP B", () -> CP(regs.B), 1, "Z 1 H C", 4);
        operations[0xb9] = new Operation("CP C", () -> CP(regs.C), 1, "Z 1 H C", 4);
        operations[0xba] = new Operation("CP D", () -> CP(regs.D), 1, "Z 1 H C", 4);
        operations[0xbb] = new Operation("CP E", () -> CP(regs.E), 1, "Z 1 H C", 4);
        operations[0xbc] = new Operation("CP H", () -> CP(regs.H), 1, "Z 1 H C", 4);
        operations[0xbd] = new Operation("CP L", () -> CP(regs.L), 1, "Z 1 H C", 4);
        operations[0xbe] = new Operation("CP (HL)", () -> CP(mem.registerLocation(regs.HL)), 1, "Z 1 H C", 8);
        operations[0xbf] = new Operation("CP A", () -> CP(regs.A), 1, "Z 1 H C", 4);
        operations[0xc0] = new Jump("RET NZ", () -> RET(Condition.NZ), 1, "- - - -", 20, 8);
        operations[0xc1] = new Operation("POP BC", () -> POP(regs.BC), 1, "- - - -", 12);
        operations[0xc2] = new Jump("JP NZ,a16", () -> JP(Condition.NZ, a16()), 3, "- - - -", 16, 12);
        operations[0xc3] = new Jump("JP a16", () -> JP(a16()), 3, "- - - -", 16, 16);
        operations[0xc4] = new Jump("CALL NZ,a16", () -> CALL(Condition.NZ, a16()), 3, "- - - -", 24, 12);
        operations[0xc5] = new Operation("PUSH BC", () -> PUSH(regs.BC), 1, "- - - -", 16);
        operations[0xc6] = new Operation("ADD A,d8", () -> ADD(regs.A, d8()), 2, "Z 0 H C", 8);
        operations[0xc7] = new Jump("RST 00H", () -> RST(0x00), 1, "- - - -", 16, 16);
        operations[0xc8] = new Jump("RET Z", () -> RET(Condition.Z), 1, "- - - -", 20, 8);
        operations[0xc9] = new Jump("RET", this::RET, 1, "- - - -", 16, 16);
        operations[0xca] = new Jump("JP Z,a16", () -> JP(Condition.Z, a16()), 3, "- - - -", 16, 12);
        operations[0xcb] = new Operation("CB", this::CB, 1, "- - - -", 4);
        operations[0xcc] = new Jump("CALL Z,a16", () -> CALL(Condition.Z, a16()), 3, "- - - -", 24, 12);
        operations[0xcd] = new Jump("CALL a16", () -> CALL(a16()), 3, "- - - -", 24, 24);
        operations[0xce] = new Operation("ADC A,d8", () -> ADC(regs.A, d8()), 2, "Z 0 H C", 8);
        operations[0xcf] = new Jump("RST 08H", () -> RST(0x08), 1, "- - - -", 16, 16);
        operations[0xd0] = new Jump("RET NC", () -> RET(Condition.NC), 1, "- - - -", 20, 8);
        operations[0xd1] = new Operation("POP DE", () -> POP(regs.DE), 1, "- - - -", 12);
        operations[0xd2] = new Jump("JP NC,a16", () -> JP(Condition.NC, a16()), 3, "- - - -", 16, 12);
        operations[0xd3] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xd4] = new Jump("CALL NC,a16", () -> CALL(Condition.NC, a16()), 3, "- - - -", 24, 12);
        operations[0xd5] = new Operation("PUSH DE", () -> PUSH(regs.DE), 1, "- - - -", 16);
        operations[0xd6] = new Operation("SUB d8", () -> SUB(d8()), 2, "Z 1 H C", 8);
        operations[0xd7] = new Jump("RST 10H", () -> RST(0x10), 1, "- - - -", 16, 16);
        operations[0xd8] = new Jump("RET C(cond)", () -> RET(Condition.C), 1, "- - - -", 20, 8);
        operations[0xd9] = new Jump("RETI", this::RETI, 1, "- - - -", 16, 16);
        operations[0xda] = new Jump("JP C(cond),a16", () -> JP(Condition.C, a16()), 3, "- - - -", 16, 12);
        operations[0xdb] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xdc] = new Jump("CALL C(cond),a16", () -> CALL(Condition.C, a16()), 3, "- - - -", 24, 12);
        operations[0xdd] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xde] = new Operation("SBC d8", () -> SBC(d8()), 2, "Z 1 H C", 8);
        operations[0xdf] = new Jump("RST 18H", () -> RST(0x18), 1, "- - - -", 16, 16);
        operations[0xe0] = new Operation("LD (a8),A", () -> LD(mem.a8Location(regs.PC), regs.A), 2, "- - - -", 12);
        operations[0xe1] = new Operation("POP HL", () -> POP(regs.HL), 1, "- - - -", 12);
        operations[0xe2] = new Operation("LD (C),A", () -> LD(mem.shortRegisterLocation(regs.C), regs.A), 1, "- - - -", 8);
        operations[0xe3] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xe4] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xe5] = new Operation("PUSH HL", () -> PUSH(regs.HL), 1, "- - - -", 16);
        operations[0xe6] = new Operation("AND d8", () -> AND(d8()), 2, "Z 0 1 0", 8);
        operations[0xe7] = new Jump("RST 20H", () -> RST(0x20), 1, "- - - -", 16, 16);
        operations[0xe8] = new Operation("ADD SP,r8", () -> ADD(regs.SP, d8()), 2, "0 0 H C", 16);
        operations[0xe9] = new Jump("JP (HL)", () -> JP(mem.registerLocation(regs.HL)), 1, "- - - -", 4, 4);
        operations[0xea] = new Operation("LD (a16),A", () -> LD(mem.a16Location(regs.PC), regs.A), 3, "- - - -", 16);
        operations[0xeb] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xec] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xed] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xee] = new Operation("XOR d8", () -> XOR(d8()), 2, "Z 0 0 0", 8);
        operations[0xef] = new Jump("RST 28H", () -> RST(0x28), 1, "- - - -", 16, 16);
        operations[0xf0] = new Operation("LD A,(a8)", () -> LD(regs.A, mem.a8Location(regs.PC)), 2, "- - - -", 12);
        operations[0xf1] = new Operation("POP AF", () -> POP(regs.AF), 1, "Z N H C", 12);
        operations[0xf2] = new Operation("LD A,(C)", () -> LD(regs.A, mem.shortRegisterLocation(regs.C)), 1, "- - - -", 8);
        operations[0xf3] = new Operation("DI", this::DI, 1, "- - - -", 4);
        operations[0xf4] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xf5] = new Operation("PUSH AF", () -> PUSH(regs.AF), 1, "- - - -", 16);
        operations[0xf6] = new Operation("OR d8", () -> OR(d8()), 2, "Z 0 0 0", 8);
        operations[0xf7] = new Jump("RST 30H", () -> RST(0x30), 1, "- - - -", 16, 16);
        operations[0xf8] = new Operation("LD HL,SP+r8", () -> LD(regs.HL, mem.SPr8Location(regs.SP, regs.PC, regs.flags)), 2, "0 0 H C", 12);
        operations[0xf9] = new Operation("LD SP,HL", () -> LD(regs.SP, regs.HL), 1, "- - - -", 8);
        operations[0xfa] = new Operation("LD A,(a16)", () -> LD(regs.A, mem.a16Location(regs.PC)), 3, "- - - -", 16);
        operations[0xfb] = new Operation("EI", this::EI, 1, "- - - -", 4);
        operations[0xfc] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xfd] = new Operation("XXX", this::XXX, 0, "- - - -", 0);
        operations[0xfe] = new Operation("CP d8", () -> CP(d8()), 2, "Z 1 H C", 8);
        operations[0xff] = new Jump("RST 38H", () -> RST(0x38), 1, "- - - -", 16, 16);
    }
    
    Operation[] cbOperations = new Operation[256];
    {
        cbOperations[0x0] = new Operation("RLC B", () -> RLC(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x1] = new Operation("RLC C", () -> RLC(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0x2] = new Operation("RLC D", () -> RLC(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0x3] = new Operation("RLC E", () -> RLC(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0x4] = new Operation("RLC H", () -> RLC(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0x5] = new Operation("RLC L", () -> RLC(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0x6] = new Operation("RLC (HL)", () -> RLC(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0x7] = new Operation("RLC A", () -> RLC(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x8] = new Operation("RRC B", () -> RRC(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x9] = new Operation("RRC C", () -> RRC(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0xa] = new Operation("RRC D", () -> RRC(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0xb] = new Operation("RRC E", () -> RRC(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0xc] = new Operation("RRC H", () -> RRC(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0xd] = new Operation("RRC L", () -> RRC(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0xe] = new Operation("RRC (HL)", () -> RRC(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0xf] = new Operation("RRC A", () -> RRC(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x10] = new Operation("RL B", () -> RL(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x11] = new Operation("RL C", () -> RL(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0x12] = new Operation("RL D", () -> RL(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0x13] = new Operation("RL E", () -> RL(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0x14] = new Operation("RL H", () -> RL(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0x15] = new Operation("RL L", () -> RL(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0x16] = new Operation("RL (HL)", () -> RL(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0x17] = new Operation("RL A", () -> RL(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x18] = new Operation("RR B", () -> RR(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x19] = new Operation("RR C", () -> RR(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0x1a] = new Operation("RR D", () -> RR(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0x1b] = new Operation("RR E", () -> RR(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0x1c] = new Operation("RR H", () -> RR(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0x1d] = new Operation("RR L", () -> RR(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0x1e] = new Operation("RR (HL)", () -> RR(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0x1f] = new Operation("RR A", () -> RR(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x20] = new Operation("SLA B", () -> SLA(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x21] = new Operation("SLA C", () -> SLA(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0x22] = new Operation("SLA D", () -> SLA(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0x23] = new Operation("SLA E", () -> SLA(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0x24] = new Operation("SLA H", () -> SLA(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0x25] = new Operation("SLA L", () -> SLA(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0x26] = new Operation("SLA (HL)", () -> SLA(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0x27] = new Operation("SLA A", () -> SLA(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x28] = new Operation("SRA B", () -> SRA(regs.B), 2, "Z 0 0 0", 8);
        cbOperations[0x29] = new Operation("SRA C", () -> SRA(regs.C), 2, "Z 0 0 0", 8);
        cbOperations[0x2a] = new Operation("SRA D", () -> SRA(regs.D), 2, "Z 0 0 0", 8);
        cbOperations[0x2b] = new Operation("SRA E", () -> SRA(regs.E), 2, "Z 0 0 0", 8);
        cbOperations[0x2c] = new Operation("SRA H", () -> SRA(regs.H), 2, "Z 0 0 0", 8);
        cbOperations[0x2d] = new Operation("SRA L", () -> SRA(regs.L), 2, "Z 0 0 0", 8);
        cbOperations[0x2e] = new Operation("SRA (HL)", () -> SRA(mem.registerLocation(regs.HL)), 2, "Z 0 0 0", 16);
        cbOperations[0x2f] = new Operation("SRA A", () -> SRA(regs.A), 2, "Z 0 0 0", 8);
        cbOperations[0x30] = new Operation("SWAP B", () -> SWAP(regs.B), 2, "Z 0 0 0", 8);
        cbOperations[0x31] = new Operation("SWAP C", () -> SWAP(regs.C), 2, "Z 0 0 0", 8);
        cbOperations[0x32] = new Operation("SWAP D", () -> SWAP(regs.D), 2, "Z 0 0 0", 8);
        cbOperations[0x33] = new Operation("SWAP E", () -> SWAP(regs.E), 2, "Z 0 0 0", 8);
        cbOperations[0x34] = new Operation("SWAP H", () -> SWAP(regs.H), 2, "Z 0 0 0", 8);
        cbOperations[0x35] = new Operation("SWAP L", () -> SWAP(regs.L), 2, "Z 0 0 0", 8);
        cbOperations[0x36] = new Operation("SWAP (HL)", () -> SWAP(mem.registerLocation(regs.HL)), 2, "Z 0 0 0", 16);
        cbOperations[0x37] = new Operation("SWAP A", () -> SWAP(regs.A), 2, "Z 0 0 0", 8);
        cbOperations[0x38] = new Operation("SRL B", () -> SRL(regs.B), 2, "Z 0 0 C", 8);
        cbOperations[0x39] = new Operation("SRL C", () -> SRL(regs.C), 2, "Z 0 0 C", 8);
        cbOperations[0x3a] = new Operation("SRL D", () -> SRL(regs.D), 2, "Z 0 0 C", 8);
        cbOperations[0x3b] = new Operation("SRL E", () -> SRL(regs.E), 2, "Z 0 0 C", 8);
        cbOperations[0x3c] = new Operation("SRL H", () -> SRL(regs.H), 2, "Z 0 0 C", 8);
        cbOperations[0x3d] = new Operation("SRL L", () -> SRL(regs.L), 2, "Z 0 0 C", 8);
        cbOperations[0x3e] = new Operation("SRL (HL)", () -> SRL(mem.registerLocation(regs.HL)), 2, "Z 0 0 C", 16);
        cbOperations[0x3f] = new Operation("SRL A", () -> SRL(regs.A), 2, "Z 0 0 C", 8);
        cbOperations[0x40] = new Operation("BIT 0,B", () -> BIT(0, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x41] = new Operation("BIT 0,C", () -> BIT(0, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x42] = new Operation("BIT 0,D", () -> BIT(0, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x43] = new Operation("BIT 0,E", () -> BIT(0, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x44] = new Operation("BIT 0,H", () -> BIT(0, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x45] = new Operation("BIT 0,L", () -> BIT(0, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x46] = new Operation("BIT 0,(HL)", () -> BIT(0, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x47] = new Operation("BIT 0,A", () -> BIT(0, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x48] = new Operation("BIT 1,B", () -> BIT(1, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x49] = new Operation("BIT 1,C", () -> BIT(1, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x4a] = new Operation("BIT 1,D", () -> BIT(1, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x4b] = new Operation("BIT 1,E", () -> BIT(1, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x4c] = new Operation("BIT 1,H", () -> BIT(1, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x4d] = new Operation("BIT 1,L", () -> BIT(1, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x4e] = new Operation("BIT 1,(HL)", () -> BIT(1, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x4f] = new Operation("BIT 1,A", () -> BIT(1, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x50] = new Operation("BIT 2,B", () -> BIT(2, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x51] = new Operation("BIT 2,C", () -> BIT(2, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x52] = new Operation("BIT 2,D", () -> BIT(2, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x53] = new Operation("BIT 2,E", () -> BIT(2, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x54] = new Operation("BIT 2,H", () -> BIT(2, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x55] = new Operation("BIT 2,L", () -> BIT(2, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x56] = new Operation("BIT 2,(HL)", () -> BIT(2, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x57] = new Operation("BIT 2,A", () -> BIT(2, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x58] = new Operation("BIT 3,B", () -> BIT(3, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x59] = new Operation("BIT 3,C", () -> BIT(3, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x5a] = new Operation("BIT 3,D", () -> BIT(3, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x5b] = new Operation("BIT 3,E", () -> BIT(3, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x5c] = new Operation("BIT 3,H", () -> BIT(3, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x5d] = new Operation("BIT 3,L", () -> BIT(3, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x5e] = new Operation("BIT 3,(HL)", () -> BIT(3, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x5f] = new Operation("BIT 3,A", () -> BIT(3, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x60] = new Operation("BIT 4,B", () -> BIT(4, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x61] = new Operation("BIT 4,C", () -> BIT(4, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x62] = new Operation("BIT 4,D", () -> BIT(4, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x63] = new Operation("BIT 4,E", () -> BIT(4, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x64] = new Operation("BIT 4,H", () -> BIT(4, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x65] = new Operation("BIT 4,L", () -> BIT(4, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x66] = new Operation("BIT 4,(HL)", () -> BIT(4, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x67] = new Operation("BIT 4,A", () -> BIT(4, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x68] = new Operation("BIT 5,B", () -> BIT(5, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x69] = new Operation("BIT 5,C", () -> BIT(5, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x6a] = new Operation("BIT 5,D", () -> BIT(5, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x6b] = new Operation("BIT 5,E", () -> BIT(5, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x6c] = new Operation("BIT 5,H", () -> BIT(5, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x6d] = new Operation("BIT 5,L", () -> BIT(5, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x6e] = new Operation("BIT 5,(HL)", () -> BIT(5, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x6f] = new Operation("BIT 5,A", () -> BIT(5, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x70] = new Operation("BIT 6,B", () -> BIT(6, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x71] = new Operation("BIT 6,C", () -> BIT(6, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x72] = new Operation("BIT 6,D", () -> BIT(6, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x73] = new Operation("BIT 6,E", () -> BIT(6, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x74] = new Operation("BIT 6,H", () -> BIT(6, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x75] = new Operation("BIT 6,L", () -> BIT(6, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x76] = new Operation("BIT 6,(HL)", () -> BIT(6, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x77] = new Operation("BIT 6,A", () -> BIT(6, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x78] = new Operation("BIT 7,B", () -> BIT(7, regs.B), 2, "Z 0 1 -", 8);
        cbOperations[0x79] = new Operation("BIT 7,C", () -> BIT(7, regs.C), 2, "Z 0 1 -", 8);
        cbOperations[0x7a] = new Operation("BIT 7,D", () -> BIT(7, regs.D), 2, "Z 0 1 -", 8);
        cbOperations[0x7b] = new Operation("BIT 7,E", () -> BIT(7, regs.E), 2, "Z 0 1 -", 8);
        cbOperations[0x7c] = new Operation("BIT 7,H", () -> BIT(7, regs.H), 2, "Z 0 1 -", 8);
        cbOperations[0x7d] = new Operation("BIT 7,L", () -> BIT(7, regs.L), 2, "Z 0 1 -", 8);
        cbOperations[0x7e] = new Operation("BIT 7,(HL)", () -> BIT(7, mem.registerLocation(regs.HL)), 2, "Z 0 1 -", 16);
        cbOperations[0x7f] = new Operation("BIT 7,A", () -> BIT(7, regs.A), 2, "Z 0 1 -", 8);
        cbOperations[0x80] = new Operation("RES 0,B", () -> RES(0, regs.B), 2, "- - - -", 8);
        cbOperations[0x81] = new Operation("RES 0,C", () -> RES(0, regs.C), 2, "- - - -", 8);
        cbOperations[0x82] = new Operation("RES 0,D", () -> RES(0, regs.D), 2, "- - - -", 8);
        cbOperations[0x83] = new Operation("RES 0,E", () -> RES(0, regs.E), 2, "- - - -", 8);
        cbOperations[0x84] = new Operation("RES 0,H", () -> RES(0, regs.H), 2, "- - - -", 8);
        cbOperations[0x85] = new Operation("RES 0,L", () -> RES(0, regs.L), 2, "- - - -", 8);
        cbOperations[0x86] = new Operation("RES 0,(HL)", () -> RES(0, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0x87] = new Operation("RES 0,A", () -> RES(0, regs.A), 2, "- - - -", 8);
        cbOperations[0x88] = new Operation("RES 1,B", () -> RES(1, regs.B), 2, "- - - -", 8);
        cbOperations[0x89] = new Operation("RES 1,C", () -> RES(1, regs.C), 2, "- - - -", 8);
        cbOperations[0x8a] = new Operation("RES 1,D", () -> RES(1, regs.D), 2, "- - - -", 8);
        cbOperations[0x8b] = new Operation("RES 1,E", () -> RES(1, regs.E), 2, "- - - -", 8);
        cbOperations[0x8c] = new Operation("RES 1,H", () -> RES(1, regs.H), 2, "- - - -", 8);
        cbOperations[0x8d] = new Operation("RES 1,L", () -> RES(1, regs.L), 2, "- - - -", 8);
        cbOperations[0x8e] = new Operation("RES 1,(HL)", () -> RES(1, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0x8f] = new Operation("RES 1,A", () -> RES(1, regs.A), 2, "- - - -", 8);
        cbOperations[0x90] = new Operation("RES 2,B", () -> RES(2, regs.B), 2, "- - - -", 8);
        cbOperations[0x91] = new Operation("RES 2,C", () -> RES(2, regs.C), 2, "- - - -", 8);
        cbOperations[0x92] = new Operation("RES 2,D", () -> RES(2, regs.D), 2, "- - - -", 8);
        cbOperations[0x93] = new Operation("RES 2,E", () -> RES(2, regs.E), 2, "- - - -", 8);
        cbOperations[0x94] = new Operation("RES 2,H", () -> RES(2, regs.H), 2, "- - - -", 8);
        cbOperations[0x95] = new Operation("RES 2,L", () -> RES(2, regs.L), 2, "- - - -", 8);
        cbOperations[0x96] = new Operation("RES 2,(HL)", () -> RES(2, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0x97] = new Operation("RES 2,A", () -> RES(2, regs.A), 2, "- - - -", 8);
        cbOperations[0x98] = new Operation("RES 3,B", () -> RES(3, regs.B), 2, "- - - -", 8);
        cbOperations[0x99] = new Operation("RES 3,C", () -> RES(3, regs.C), 2, "- - - -", 8);
        cbOperations[0x9a] = new Operation("RES 3,D", () -> RES(3, regs.D), 2, "- - - -", 8);
        cbOperations[0x9b] = new Operation("RES 3,E", () -> RES(3, regs.E), 2, "- - - -", 8);
        cbOperations[0x9c] = new Operation("RES 3,H", () -> RES(3, regs.H), 2, "- - - -", 8);
        cbOperations[0x9d] = new Operation("RES 3,L", () -> RES(3, regs.L), 2, "- - - -", 8);
        cbOperations[0x9e] = new Operation("RES 3,(HL)", () -> RES(3, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0x9f] = new Operation("RES 3,A", () -> RES(3, regs.A), 2, "- - - -", 8);
        cbOperations[0xa0] = new Operation("RES 4,B", () -> RES(4, regs.B), 2, "- - - -", 8);
        cbOperations[0xa1] = new Operation("RES 4,C", () -> RES(4, regs.C), 2, "- - - -", 8);
        cbOperations[0xa2] = new Operation("RES 4,D", () -> RES(4, regs.D), 2, "- - - -", 8);
        cbOperations[0xa3] = new Operation("RES 4,E", () -> RES(4, regs.E), 2, "- - - -", 8);
        cbOperations[0xa4] = new Operation("RES 4,H", () -> RES(4, regs.H), 2, "- - - -", 8);
        cbOperations[0xa5] = new Operation("RES 4,L", () -> RES(4, regs.L), 2, "- - - -", 8);
        cbOperations[0xa6] = new Operation("RES 4,(HL)", () -> RES(4, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xa7] = new Operation("RES 4,A", () -> RES(4, regs.A), 2, "- - - -", 8);
        cbOperations[0xa8] = new Operation("RES 5,B", () -> RES(5, regs.B), 2, "- - - -", 8);
        cbOperations[0xa9] = new Operation("RES 5,C", () -> RES(5, regs.C), 2, "- - - -", 8);
        cbOperations[0xaa] = new Operation("RES 5,D", () -> RES(5, regs.D), 2, "- - - -", 8);
        cbOperations[0xab] = new Operation("RES 5,E", () -> RES(5, regs.E), 2, "- - - -", 8);
        cbOperations[0xac] = new Operation("RES 5,H", () -> RES(5, regs.H), 2, "- - - -", 8);
        cbOperations[0xad] = new Operation("RES 5,L", () -> RES(5, regs.L), 2, "- - - -", 8);
        cbOperations[0xae] = new Operation("RES 5,(HL)", () -> RES(5, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xaf] = new Operation("RES 5,A", () -> RES(5, regs.A), 2, "- - - -", 8);
        cbOperations[0xb0] = new Operation("RES 6,B", () -> RES(6, regs.B), 2, "- - - -", 8);
        cbOperations[0xb1] = new Operation("RES 6,C", () -> RES(6, regs.C), 2, "- - - -", 8);
        cbOperations[0xb2] = new Operation("RES 6,D", () -> RES(6, regs.D), 2, "- - - -", 8);
        cbOperations[0xb3] = new Operation("RES 6,E", () -> RES(6, regs.E), 2, "- - - -", 8);
        cbOperations[0xb4] = new Operation("RES 6,H", () -> RES(6, regs.H), 2, "- - - -", 8);
        cbOperations[0xb5] = new Operation("RES 6,L", () -> RES(6, regs.L), 2, "- - - -", 8);
        cbOperations[0xb6] = new Operation("RES 6,(HL)", () -> RES(6, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xb7] = new Operation("RES 6,A", () -> RES(6, regs.A), 2, "- - - -", 8);
        cbOperations[0xb8] = new Operation("RES 7,B", () -> RES(7, regs.B), 2, "- - - -", 8);
        cbOperations[0xb9] = new Operation("RES 7,C", () -> RES(7, regs.C), 2, "- - - -", 8);
        cbOperations[0xba] = new Operation("RES 7,D", () -> RES(7, regs.D), 2, "- - - -", 8);
        cbOperations[0xbb] = new Operation("RES 7,E", () -> RES(7, regs.E), 2, "- - - -", 8);
        cbOperations[0xbc] = new Operation("RES 7,H", () -> RES(7, regs.H), 2, "- - - -", 8);
        cbOperations[0xbd] = new Operation("RES 7,L", () -> RES(7, regs.L), 2, "- - - -", 8);
        cbOperations[0xbe] = new Operation("RES 7,(HL)", () -> RES(7, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xbf] = new Operation("RES 7,A", () -> RES(7, regs.A), 2, "- - - -", 8);
        cbOperations[0xc0] = new Operation("SET 0,B", () -> SET(0, regs.B), 2, "- - - -", 8);
        cbOperations[0xc1] = new Operation("SET 0,C", () -> SET(0, regs.C), 2, "- - - -", 8);
        cbOperations[0xc2] = new Operation("SET 0,D", () -> SET(0, regs.D), 2, "- - - -", 8);
        cbOperations[0xc3] = new Operation("SET 0,E", () -> SET(0, regs.E), 2, "- - - -", 8);
        cbOperations[0xc4] = new Operation("SET 0,H", () -> SET(0, regs.H), 2, "- - - -", 8);
        cbOperations[0xc5] = new Operation("SET 0,L", () -> SET(0, regs.L), 2, "- - - -", 8);
        cbOperations[0xc6] = new Operation("SET 0,(HL)", () -> SET(0, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xc7] = new Operation("SET 0,A", () -> SET(0, regs.A), 2, "- - - -", 8);
        cbOperations[0xc8] = new Operation("SET 1,B", () -> SET(1, regs.B), 2, "- - - -", 8);
        cbOperations[0xc9] = new Operation("SET 1,C", () -> SET(1, regs.C), 2, "- - - -", 8);
        cbOperations[0xca] = new Operation("SET 1,D", () -> SET(1, regs.D), 2, "- - - -", 8);
        cbOperations[0xcb] = new Operation("SET 1,E", () -> SET(1, regs.E), 2, "- - - -", 8);
        cbOperations[0xcc] = new Operation("SET 1,H", () -> SET(1, regs.H), 2, "- - - -", 8);
        cbOperations[0xcd] = new Operation("SET 1,L", () -> SET(1, regs.L), 2, "- - - -", 8);
        cbOperations[0xce] = new Operation("SET 1,(HL)", () -> SET(1, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xcf] = new Operation("SET 1,A", () -> SET(1, regs.A), 2, "- - - -", 8);
        cbOperations[0xd0] = new Operation("SET 2,B", () -> SET(2, regs.B), 2, "- - - -", 8);
        cbOperations[0xd1] = new Operation("SET 2,C", () -> SET(2, regs.C), 2, "- - - -", 8);
        cbOperations[0xd2] = new Operation("SET 2,D", () -> SET(2, regs.D), 2, "- - - -", 8);
        cbOperations[0xd3] = new Operation("SET 2,E", () -> SET(2, regs.E), 2, "- - - -", 8);
        cbOperations[0xd4] = new Operation("SET 2,H", () -> SET(2, regs.H), 2, "- - - -", 8);
        cbOperations[0xd5] = new Operation("SET 2,L", () -> SET(2, regs.L), 2, "- - - -", 8);
        cbOperations[0xd6] = new Operation("SET 2,(HL)", () -> SET(2, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xd7] = new Operation("SET 2,A", () -> SET(2, regs.A), 2, "- - - -", 8);
        cbOperations[0xd8] = new Operation("SET 3,B", () -> SET(3, regs.B), 2, "- - - -", 8);
        cbOperations[0xd9] = new Operation("SET 3,C", () -> SET(3, regs.C), 2, "- - - -", 8);
        cbOperations[0xda] = new Operation("SET 3,D", () -> SET(3, regs.D), 2, "- - - -", 8);
        cbOperations[0xdb] = new Operation("SET 3,E", () -> SET(3, regs.E), 2, "- - - -", 8);
        cbOperations[0xdc] = new Operation("SET 3,H", () -> SET(3, regs.H), 2, "- - - -", 8);
        cbOperations[0xdd] = new Operation("SET 3,L", () -> SET(3, regs.L), 2, "- - - -", 8);
        cbOperations[0xde] = new Operation("SET 3,(HL)", () -> SET(3, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xdf] = new Operation("SET 3,A", () -> SET(3, regs.A), 2, "- - - -", 8);
        cbOperations[0xe0] = new Operation("SET 4,B", () -> SET(4, regs.B), 2, "- - - -", 8);
        cbOperations[0xe1] = new Operation("SET 4,C", () -> SET(4, regs.C), 2, "- - - -", 8);
        cbOperations[0xe2] = new Operation("SET 4,D", () -> SET(4, regs.D), 2, "- - - -", 8);
        cbOperations[0xe3] = new Operation("SET 4,E", () -> SET(4, regs.E), 2, "- - - -", 8);
        cbOperations[0xe4] = new Operation("SET 4,H", () -> SET(4, regs.H), 2, "- - - -", 8);
        cbOperations[0xe5] = new Operation("SET 4,L", () -> SET(4, regs.L), 2, "- - - -", 8);
        cbOperations[0xe6] = new Operation("SET 4,(HL)", () -> SET(4, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xe7] = new Operation("SET 4,A", () -> SET(4, regs.A), 2, "- - - -", 8);
        cbOperations[0xe8] = new Operation("SET 5,B", () -> SET(5, regs.B), 2, "- - - -", 8);
        cbOperations[0xe9] = new Operation("SET 5,C", () -> SET(5, regs.C), 2, "- - - -", 8);
        cbOperations[0xea] = new Operation("SET 5,D", () -> SET(5, regs.D), 2, "- - - -", 8);
        cbOperations[0xeb] = new Operation("SET 5,E", () -> SET(5, regs.E), 2, "- - - -", 8);
        cbOperations[0xec] = new Operation("SET 5,H", () -> SET(5, regs.H), 2, "- - - -", 8);
        cbOperations[0xed] = new Operation("SET 5,L", () -> SET(5, regs.L), 2, "- - - -", 8);
        cbOperations[0xee] = new Operation("SET 5,(HL)", () -> SET(5, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xef] = new Operation("SET 5,A", () -> SET(5, regs.A), 2, "- - - -", 8);
        cbOperations[0xf0] = new Operation("SET 6,B", () -> SET(6, regs.B), 2, "- - - -", 8);
        cbOperations[0xf1] = new Operation("SET 6,C", () -> SET(6, regs.C), 2, "- - - -", 8);
        cbOperations[0xf2] = new Operation("SET 6,D", () -> SET(6, regs.D), 2, "- - - -", 8);
        cbOperations[0xf3] = new Operation("SET 6,E", () -> SET(6, regs.E), 2, "- - - -", 8);
        cbOperations[0xf4] = new Operation("SET 6,H", () -> SET(6, regs.H), 2, "- - - -", 8);
        cbOperations[0xf5] = new Operation("SET 6,L", () -> SET(6, regs.L), 2, "- - - -", 8);
        cbOperations[0xf6] = new Operation("SET 6,(HL)", () -> SET(6, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xf7] = new Operation("SET 6,A", () -> SET(6, regs.A), 2, "- - - -", 8);
        cbOperations[0xf8] = new Operation("SET 7,B", () -> SET(7, regs.B), 2, "- - - -", 8);
        cbOperations[0xf9] = new Operation("SET 7,C", () -> SET(7, regs.C), 2, "- - - -", 8);
        cbOperations[0xfa] = new Operation("SET 7,D", () -> SET(7, regs.D), 2, "- - - -", 8);
        cbOperations[0xfb] = new Operation("SET 7,E", () -> SET(7, regs.E), 2, "- - - -", 8);
        cbOperations[0xfc] = new Operation("SET 7,H", () -> SET(7, regs.H), 2, "- - - -", 8);
        cbOperations[0xfd] = new Operation("SET 7,L", () -> SET(7, regs.L), 2, "- - - -", 8);
        cbOperations[0xfe] = new Operation("SET 7,(HL)", () -> SET(7, mem.registerLocation(regs.HL)), 2, "- - - -", 16);
        cbOperations[0xff] = new Operation("SET 7,A", () -> SET(7, regs.A), 2, "- - - -", 8);

    }
}
