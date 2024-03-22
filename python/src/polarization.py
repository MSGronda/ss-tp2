import os
import numpy as np
import matplotlib.pyplot as plt
import glob


def calculate_polarization(particles: str, static_data: {}) -> []:
    v = static_data['v']
    n = static_data['n']

    with open(particles, 'r') as f:
        lines = f.readlines()

    polarization = []
    x_sum = 0
    y_sum = 0

    for line in lines:
        if line.strip().endswith(','):
            polarization.append(np.sqrt(x_sum ** 2 + y_sum ** 2) / (n * v))
            x_sum, y_sum = 0, 0
        elif line.strip() != '':
            data = line.split(',')
            angle = float(data[2])

            x_sum += np.cos(angle) * v
            y_sum += np.sin(angle) * v

    polarization.pop(0)
    return polarization


def graph_polarization_time(polarization: []):
    plt.figure(figsize=(8, 6))
    time = [i for i in range(0, len(polarization))]
    plt.scatter(time, polarization, color='blue', label='Polarization')
    plt.title('Scatter Plot of Polarization vs. Time')
    plt.xlabel('Time')
    plt.ylabel('Polarization')
    plt.grid(True)
    plt.legend()
    plt.show()

def graph_multiple_polarization_time(polarizations: [[]], static_datas: [{}], variable: str):
    plt.figure(figsize=(8, 6))
    for static_data, polarization in zip(static_datas, polarizations):
        time = [i for i in range(0, len(polarization))]
        plt.scatter(time, polarization, label=f'Polarization ({variable}={static_data[variable]})')
    plt.title('Scatter Plot of Polarization vs. Time')
    plt.xlabel('Time')
    plt.ylabel('Polarization')
    plt.grid(True)
    plt.legend()
    plt.show()

def compare_polarizations(polarizations: [[]], static_datas: [{}], variable: str ,starting: int):

    avg_polarizations = []
    std_deviations = []
    parameters = []

    for polarization in polarizations:
        cut = polarization[starting:]
        avg_va = np.mean(cut)
        std_va = np.std(cut)

        avg_polarizations.append(avg_va)
        std_deviations.append(std_va)

    for data in static_datas:
        parameters.append(data[variable])

    plt.figure(figsize=(8, 6))
    plt.errorbar(parameters, avg_polarizations, yerr=std_deviations, fmt='o', color='red', ecolor='black', capsize=5)
    plt.xlabel(variable)
    plt.ylabel('Average Polarization')
    plt.grid(True)
    plt.show()


def get_all_files(prefix: str) -> [str]:
    pattern = f"{prefix}-*.txt"
    return glob.glob(pattern)





