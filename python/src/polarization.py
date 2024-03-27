import concurrent.futures
import numpy as np
import matplotlib.pyplot as plt
import glob
from src.utils import get_static_data, get_all_files


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
            angle = float(data[3])

            x_sum += np.cos(angle) * v
            y_sum += np.sin(angle) * v

    polarization.pop(0)
    return polarization


def process_files(p, s):
    data = get_static_data(s)
    pol = calculate_polarization(p, data)
    return data, pol


def calculate_all_polarizations():
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    polarization = []
    static_data = []

    with concurrent.futures.ProcessPoolExecutor() as executor:
        results = executor.map(process_files, particle_files, static_files)

        for data, pol in results:
            static_data.append(data)
            polarization.append(pol)

    return polarization, static_data


def graph_polarization_time(polarization: []):
    plt.figure(figsize=(8, 6))
    time = [i for i in range(0, len(polarization))]
    plt.scatter(time, polarization, color='blue', label='Polarizacion')
    plt.xlabel('Tiempo')
    plt.ylabel('Polarizacion')
    plt.grid(True)
    plt.legend()
    plt.show()


def graph_multiple_polarization_time(polarizations: [[]], static_datas: [{}], variable: str, variable_name: str):
    plt.figure(figsize=(10, 6))
    for static_data, polarization in zip(static_datas, polarizations):
        time = [i for i in range(0, len(polarization))]
        plt.scatter(time, polarization, label=f'Polarizacion ({variable_name}={static_data[variable]:.2f})')
    plt.xlabel('Tiempo')
    plt.ylabel('Polarizacion')
    plt.grid(True)
    plt.legend()
    plt.show()


def compare_polarizations(polarizations: [[]], static_datas: [{}], variable: str, starting: int, variable_name: str):

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
    plt.xlabel(variable_name)
    plt.ylabel('Polarizacion promedio')
    plt.grid(True)
    plt.show()






