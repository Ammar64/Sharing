import subprocess

# get unix timestamp of the last git commit
command = ['git', '--no-pager', 'log', '-1', '--format=%at']
output = subprocess.check_output(command)

# Subtract 1779530803 so we get smaller numbers
version_number = int(output.decode().strip()) - 1779530803
print(version_number)


