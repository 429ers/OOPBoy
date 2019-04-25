import re

special_operands = {
    "r8": "d8()", #sign is handled in CPU code
    "d8": "d8()",
    "a8": "a8()",
    "d16": "d16()",
    "a16": "a16()",
    "NZ": "Condition.NZ",
    "Z": "Condition.Z",
    "NC": "Condition.NC",
    "C(cond)": "Condition.C",
    "(a16)": "mem.a16Location(regs.PC)",
    "(a8)": "mem.a8Location(regs.PC)",
    "(d8)": "mem.d8Location(regs.PC)",
    "(C)": "mem.shortRegisterLocation(regs.C)",
    "(HL+)": "mem.registerLocation(selfIncrement(regs.HL))",
    "(HL-)": "mem.registerLocation(selfDecrement(regs.HL))",
    "SP+r8": "mem.SPr8Location(regs.SP, regs.PC)",
}

registers = ['A', 'B', 'D', 'H', 'F', 'C', 'E', 'L', 'AF', 'BC', 'DE', 'HL', 'SP', 'PC']

jumps = ['RET', 'JP', 'CALL', 'RST', 'RETI', 'JR']

def convert_op(op): #converts the operand to the Java equivalent
    if op in special_operands:
        return special_operands[op]
    if op in registers:
        return 'regs.' + op
    re_matches = re.match(r'^\(([A-Z][A-Z])\)$', op)
    if re_matches:
        address_reg = re_matches.group(1)
        if address_reg in registers:
            return 'mem.registerLocation(regs.' + address_reg + ')'
    
    re_matches = re.match(r'^(\d\d)H$', op)
    if re_matches:
        return '0x' + re_matches.group(1)
    print('unsupported operand: ' + op)

    exit(1)

def generate_lambda(mnemonic, operands):
    operands = map(convert_op, operands)

    statement = mnemonic + '(' + (', '.join(operands)) + ');'
    
    return '() -> { return ' + statement + " }"

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
        if op_mnemonic == 'XXX': line = '0\xa0\xa00'
        temp = line.split('\xa0\xa0') #the numbers are separated by two of these characters for some reason
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