import re

registers = ['A', 'B', 'D', 'H', 'F', 'C', 'E', 'L', 'AF', 'BC', 'DE', 'HL', 'SP', 'PC']

def convert_op(op): #converts the operand to the Java equivalent
    if op in registers:
        return 'cpu.regs.' + op
    re_matches = re.match(r'^\(([A-Z][A-Z])\)$', op)
    if re_matches:
        address_reg = re_matches.group(1)
        if address_reg in registers:
            return 'cpu.mem.registerLocation(cpu.regs.' + address_reg + ')'
    
    re_matches = re.match(r'^(\d)$', op)
    if re_matches:
        return re_matches.group(1)
    print('unsupported operand: ' + op)

    exit(1)

def generate_lambda(mnemonic, operands):
    operands = map(convert_op, operands)

    statement = 'cpu.' + mnemonic + '(' + (', '.join(operands)) + ')'
    
    return '(CPU cpu) -> ' + statement

def assemble_operation():
    return 'new Operation("' + op_description + '", ' + op_lambda + ', ' + op_length + ', "' + op_flags + '", ' + op_ticks[0] + ')'

file = open("cb_instructions.txt", "r")

output = open("generated_cb_code.txt", "w")

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
    output.write('cbOperations[' + hex(i) + '] = ' + operations[i] + ';\n')
output.close()