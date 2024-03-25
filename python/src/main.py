from src.polarization import *
from src.animation import generate_video, generate_gif
from src.animation import generate_video, generate_gif, generate_video_counting_visits
from src.visits import calculate_pbc, calculate_obc


def compare_and_graph_polarization():
    polarization, static_data = calculate_all_polarizations()

    compare_polarizations(polarization, static_data, 'noiseAmplitude', 400, 'Amplitud de Ruido')


def calc_and_graph_multi_polarization():
    polarization, static_data = calculate_all_polarizations()

    indices = [0, 3, 6, 9]

    graph_multiple_polarization_time([polarization[i] for i in indices], [static_data[i] for i in indices], 'density',
                                     'Densidad de poblacion')


def generate_video_from_index(index):
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    generate_video(particle_files[index], static_files[index], False, 200)


def generate_gif_from_first():
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    generate_gif(particle_files[0], static_files[0])


if __name__ == '__main__':
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    for i, s in enumerate(calculate_obc(particle_files[0], get_static_data(static_files[0]), 2)):
        print(f"{i},{len(s)}")

    # generate_video_with_visits_from_first()
