import matplotlib.pyplot as plt
import numpy as np

# Read data from the file
file_path = '../output-files/polarization-with-noise.txt'
with open(file_path, 'r') as file:
    lines = file.readlines()

# Create dictionaries to store polarization values for each noise amplitude
polarizations_dict = {}

# Extract noise amplitude and polarization values
for line in lines[1:]:  # Skip the header line
    n, va = line.strip().split(',')
    n = float(n)
    va = float(va)
    if n in polarizations_dict:
        polarizations_dict[n].append(va)
    else:
        polarizations_dict[n] = [va]

# Calculate average polarization and standard deviation for each noise amplitude
noise_amplitudes = []
avg_polarizations = []
std_deviations = []
for n, va_values in polarizations_dict.items():
    avg_va = np.mean(va_values)
    std_va = np.std(va_values)
    noise_amplitudes.append(n)
    avg_polarizations.append(avg_va)
    std_deviations.append(std_va)

# Plot the graph with error bars
plt.figure(figsize=(8, 6))
plt.errorbar(noise_amplitudes, avg_polarizations, yerr=std_deviations, fmt='o', color='red', ecolor='black', capsize=5)
plt.xlabel('Noise Amplitude')
plt.ylabel('Average Polarization')
plt.grid(True)
plt.show()
