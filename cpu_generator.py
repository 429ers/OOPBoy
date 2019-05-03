import re

special_operands = {
    "r8": "cpu.d8()", #sign is handled in CPU code
    "d8": "cpu.d8()",
    "a8": "cpu.a8()",
    "d16": "cpu.d16()",
    "a16": "cpu.a16()",
    "NZ": "Condition.NZ",
    "Z": "Condition.Z",
    "NC": "Condition.NC",
    "C(cond)": "Condition.C",
    "(a16)": "cpu.mem.a16Location(cpu.regs.PC)",
    "(a8)": "cpu.mem.a8Location(cpu.regs.PC)",
    "(d8)": "cpu.mem.d8Location(cpu.regs.PC)",
    "(C)": "cpu.mem.shortRegisterLocation(cpu.regs.C)",
    "(HL+)": "cpu.mem.registerLocation(selfIncrement(cpu.regs.HL))",
    "(HL-)": "cpu.mem.registerLocation(selfDecrement(cpu.regs.HL))",
    "SP+r8": "cpu.mem.SPr8Location(cpu.regs.SP, cpu.regs.PC, cpu.regs.flags)",
}

registers = ['A', 'B', 'D', 'H', 'F', 'C', 'E', 'L', 'AF', 'BC', 'DE', 'HL', 'SP', 'PC']

jumps = ['RET', 'JP', 'CALL', 'RST', 'RETI', 'JR']

def convert_op(op): #converts the operand to the Java equivalent
    if op in special_operands:
        return special_operands[op]
    if op in registers:
        return 'cpu.regs.' + op
    re_matches = re.match(r'^\(([A-Z][A-Z])\)$', op)
    if re_matches:
        address_reg = re_matches.group(1)
        if address_reg in registers:
            return 'cpu.mem.registerLocation(cpu.regs.' + address_reg + ')'
    
    re_matches = re.match(r'^(\d\d)H$', op)
    if re_matches:
        return '0x' + re_matches.group(1)
    print('unsupported operand: ' + op)

    exit(1)

def generate_lambda(mnemonic, operands):
    if len(operands) == 0:
        return 'CPU::' + mnemonic
    operands = map(convert_op, operands)

    statement = 'cpu.'+mnemonic + '(' + (', '.join(operands)) + ')'
    
    return '(CPU cpu) -> ' + statement

def assemble_operation():
    if op_mnemonic in jumps:
        return 'new Jump("' + op_description + '", ' + op_lambda + ', ' + op_length + ', "' + op_flags + '", ' + op_ticks[0] + ', ' + op_ticks[1] + ')'
    else:
        return 'new Operation("' + op_description + '", ' + op_lambda + ', ' + op_length + ', "' + op_flags + '", ' + op_ticks[0] + ')'

file = open("instructions_corrected.txt", "r")

output = open("generated_code.txt", "w")

line_num = 0
op_lambda = ''
op_ticks = ['0', '0']
op_length = '0'
op_id = 0
op_flags = ''
op_mnemonic = ''
op_description = ''
operations = [''] * 256
for line in file:
    line = line[:-1] #remove newline

    op_id = line_num // 3
    
    if line_num % 3 == 0:
        op_description = line
        temp = line.split(' ')
        op_mnemonic = temp[0]
        operands = (temp[1].split(',')) if (len(temp) > 1) else []
        op_lambda = generate_lambda(op_mnemonic, operands)
    
    if line_num % 3 == 1:
        if op_mnemonic == 'XXX': line = '0  0'
        temp = line.split('  ') #the numbers are separated by two of these characters for some reason
        op_length = temp[0]
        if '/' in temp[1]:
            op_ticks = temp[1].split('/')
        else:
            op_ticks = [temp[1], temp[1]]
    
    if line_num % 3 == 2:
        if op_mnemonic == 'XXX': line = '- - - -'
        op_flags = line
        row_num = op_id % 16
        col_num = op_id // 16
        operations[row_num * 16 + col_num] = assemble_operation()
    
    line_num += 1

for i in range(len(operations)):
    output.write('operations[' + hex(i) + '] = ' + operations[i] + ';\n')
output.close()