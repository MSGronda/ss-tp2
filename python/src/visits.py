import concurrent.futures
import math
from functools import partial

import numpy as np
from matplotlib import pyplot as plt
from src.utils import get_static_data, get_all_files


def in_visit_area(x: float, y: float, l: float, visit_area_radius: float):
    return (x - l / 2) ** 2 + (y - l / 2) ** 2 <= visit_area_radius ** 2


def calculate_pbc(position_filename: str, static_data: {}, visit_radius: float):
    visitors = []
    current_visitors = set()
    historic_visitors = set()

    l = static_data['l']

    with open(position_filename, 'r') as f:
        lines = f.readlines()

    for line in lines:
        if line.strip().endswith(','):
            visitors.append(current_visitors)
            current_visitors = set()

        elif line.strip() != '':
            data = line.split(',')
            particle_id, x, y = int(data[0]), float(data[1]), float(data[2])

            if in_visit_area(x, y, l, visit_radius) and particle_id not in historic_visitors:
                current_visitors.add(particle_id)
                historic_visitors.add(particle_id)

    # Agrego el ultimo epoch
    visitors.append(current_visitors)

    visitors.pop(0)  # Elimino el primer falso epoch

    return visitors


def particle_will_exit(x: float, y: float, angle: float, v: float, l: float):
    new_x = x + math.cos(angle) * v
    new_y = y + math.sin(angle) * v
    return new_x < 0 or new_x > l or new_y < 0 or new_y > l


def calculate_obc(position_filename: str, static_data: {}, visit_radius: float):
    visitors = []
    current_visitors = set()
    historic_visitors = set()

    exited_particles = {}

    l = static_data['l']
    v = static_data['v']

    with open(position_filename, 'r') as f:
        lines = f.readlines()

    for line in lines:
        if line.strip().endswith(','):
            visitors.append(current_visitors)
            current_visitors = set()

        elif line.strip() != '':
            data = line.split(',')
            particle_id, x, y, angle = int(data[0]), float(data[1]), float(data[2]), float(data[3])

            identity = (particle_id, exited_particles.get(particle_id, 0))
            if in_visit_area(x, y, l, visit_radius) and identity not in historic_visitors:
                current_visitors.add(particle_id)
                historic_visitors.add(identity)

            # Importante hacer esto despues del chequeo de visit area
            if particle_will_exit(x, y, angle, v, l):
                count = exited_particles.get(particle_id, 0)
                exited_particles[particle_id] = count + 1

    # Agrego el ultimo epoch
    visitors.append(current_visitors)

    visitors.pop(0)  # Elimino el primer falso epoch

    return visitors


def visits_graph(particle_filename: str, static_data_filename: str, pbc: bool):
    static_data = get_static_data(static_data_filename)
    n = static_data['n']

    if pbc:
        visits = calculate_pbc(particle_filename, static_data, 0.5)
    else:
        visits = calculate_obc(particle_filename, static_data, 0.5)

    accumulative_y = []
    total = 0

    for step in visits:
        total += len(step)
        if pbc:
            accumulative_y.append(round((total/n)*100, 2))
        else:
            accumulative_y.append(total)

    time_steps = range(1, len(accumulative_y) + 1)

    plt.scatter(time_steps, accumulative_y)

    plt.xlabel('Tiempo')
    if pbc:
        plt.ylabel('% de partículas que visitaron')
    else:
        plt.ylabel('Cantidad de visitas')

    plt.grid(True)
    plt.show()


def multiple_visits_graph(particle_filenames: [], static_data_filenames: [], pbc: bool, variable: str, variable_name: str):
    for particle_filename, static_data_filename in zip(particle_filenames, static_data_filenames):
        static_data = get_static_data(static_data_filename)
        n = static_data['n']

        if pbc:
            visits = calculate_pbc(particle_filename, static_data, 0.5)
        else:
            visits = calculate_obc(particle_filename, static_data, 0.5)

        accumulative_y = []
        total = 0

        for step in visits:
            total += len(step)
            if pbc:
                accumulative_y.append(round((total/n)*100, 2))
            else:
                accumulative_y.append(total)

        time_steps = range(1, len(accumulative_y) + 1)

        plt.scatter(time_steps, accumulative_y, label=f'{variable_name}={static_data[variable]:.2f}')

    plt.xlabel('Tiempo')
    if pbc:
        plt.ylabel('% de partículas que visitaron')
    else:
        plt.ylabel('Cantidad de visitas')

    plt.grid(True)
    plt.legend()
    plt.show()


def calculate_all_visits(pbc: bool):
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    total_visits = []
    static_data = []

    partial_process_files = partial(process_files, pbc)

    with concurrent.futures.ProcessPoolExecutor() as executor:
        results = executor.map(partial_process_files, particle_files, static_files)

        for data, t_visits in results:
            static_data.append(data)
            total_visits.append(t_visits)

    return total_visits, static_data


def process_files(pbc: bool, p, s):
    data = get_static_data(s)
    if pbc:
        visits = calculate_pbc(p, data, 0.5)
    else:
        visits = calculate_obc(p, data, 0.5)
    total_visits = calculate_total_visits(visits, data)
    return data, total_visits


def calculate_total_visits(visits: [], static_data: {}):
    total_visits = 0
    for step in visits:
        total_visits += len(step)

    return total_visits


def compare_total_visits(total_visits: [], static_datas: [{}], variable: str, variable_name: str, pbc: bool):
    parameters = []

    for data in static_datas:
        if not parameters.__contains__(data[variable]):
            parameters.append(data[variable])

    values = []
    same_variable_values = []
    std_deviations = []
    variable_value = -1

    for index, visits in enumerate(total_visits):
        if variable_value == -1:
            variable_value = static_datas[index][variable]
            if pbc:
                same_variable_values.append(round((visits / static_datas[index]['n']) * 100))
            else:
                same_variable_values.append(visits)

        elif static_datas[index][variable] == variable_value:
            if pbc:
                same_variable_values.append(round((visits / static_datas[index]['n']) * 100))
            else:
                same_variable_values.append(visits)
        else:
            mean = np.mean(same_variable_values)
            std_deviations.append(np.std(same_variable_values))
            values.append(mean)

            same_variable_values.clear()
            if pbc:
                same_variable_values.append(round((visits / static_datas[index]['n']) * 100))
            else:
                same_variable_values.append(visits)

            variable_value = static_datas[index][variable]

    # Una vez mas para el ultimo valor
    mean = np.mean(same_variable_values)
    std_deviations.append(np.std(same_variable_values))
    values.append(mean)

    plt.figure(figsize=(8, 6))
    plt.errorbar(parameters, values, yerr=std_deviations, fmt='o', color='red', ecolor='black', capsize=5)
    plt.xlabel(variable_name)
    if pbc:
        plt.ylabel('% de particulas que visitaron')
    else:
        plt.ylabel('Visitas totales')
    plt.grid(True)
    plt.show()
