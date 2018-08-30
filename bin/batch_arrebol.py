from __future__ import print_function
import sys

def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

def emit_jdf_task(commands):
    return "\n".join(["task:"] + [commands])

def generate_jdf(jdf_text, sweep):

    #it generates a jdf compliant str based on the placeholders
    #defined in jdf_text and the values specified in sweep list.

    #each element in the sweep list is a list of values to be
    #used for generate each task in the jdf.
    tasks = []
    for task_values in sweep:
        replaced = replace(jdf_text, task_values)
        task = emit_jdf_task(replaced)
        tasks.append(task)

    return "\n".join(["job:"] + tasks)

def replace(text, values_to_replace):
    #replace strings in values_to_replace list in placeholders from text string
    #the i-th str in values_to_replace should replace the $i string in text
    replaced = text
    for i in range(len(values_to_replace)):
        placeholder = "$" + str(i)
        value = values_to_replace[i]
        replaced = replaced.replace(placeholder, value)

    return replaced

if __name__ == "__main__":
    """
        It generates a jdf based on jdf blueprint (arg 1) and a parameter sweep file (arg 2)
        Usage: python $0 jdf_blueprint_path param_sweep_path > jdf

        The blueprint file specifies the placeholders:

            put $1 /tmp/$1
            cmd < /tmp/$1 > /tmp/out-$2
            get /tmp/out-$2 out-$2

        The parameter sweep file defines values to replace the placeholders:
            in_path1 outpath_1
            in_path2 outpath_2

        IT generates a jdf task for each line declared in parameter sweep file:
        TODO:
    """

    #check args
    if len(sys.argv) < 3:
        eprint("Usage: " + sys.argv[0] + " script_file input_file")
        exit(1)

    #load data
    script_filepath = sys.argv[1]
    load_input_filepath = sys.argv[2]

    #load blueprint file
    try:
        with open(script_filepath, 'r') as script_file:
            script_txt = script_file.read()
            #input should not be empty
            if not script_txt.strip():
                eprint("Jdf blueprint cannot be empty")
                exit(1)
    except IOError:
        eprint(script_filepath + " is not a regular file or does not exist")

    #load sweep file
    try:
        with open(load_input_filepath, 'r') as sweep_file:
            sweep = []
            sweep_separator = " "
            for line in sweep_file.readlines():
                sweep.append(line.strip().split(sweep_separator))

            #all lines of the sweep file should have the same number of tokens
            num_tokens = len(sweep[0])
            if not all(len(l) == num_tokens for l in sweep):
                eprint("Malformed sweep file. All line should have the same number of tokens")
                exit(1)
    except IOError:
        eprint(load_input_filepath + " is not a regular file or does not exist")

    #do the job
    jdf = generate_jdf(script_txt, sweep)

    #print output to stdout
    print(jdf)
