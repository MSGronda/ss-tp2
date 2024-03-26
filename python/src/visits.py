import math
from matplotlib import pyplot as plt
from src.utils import get_static_data


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


