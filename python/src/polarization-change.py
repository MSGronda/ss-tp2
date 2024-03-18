import matplotlib.pyplot as plt

# Read data from the file
file_path = '../output-files/polarization-change.txt'
with open(file_path, 'r') as file:
    lines = file.readlines()

# Extract time and polarization values
time_steps = []
polarizations = []
for line in lines[1:]:  # Skip the header line
    t, va = line.strip().split(',')
    time_steps.append(float(t))
    polarizations.append(float(va))

# Plot the scatter graph
plt.figure(figsize=(8, 6))
plt.scatter(time_steps, polarizations, color='blue', label='Polarization')
plt.title('Scatter Plot of Polarization vs. Time')
plt.xlabel('Time')
plt.ylabel('Polarization')
plt.grid(True)
plt.legend()
plt.show()
