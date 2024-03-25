from src.polarization import *

from src.animation import generate_video, generate_gif


def compare_and_graph_polarization():
    polarization, static_data = calculate_all_polarizations()

    compare_polarizations(polarization, static_data, 'noiseAmplitude', 400, 'Amplitud de Ruido')


def calc_and_graph_multi_polarization():
    polarization, static_data = calculate_all_polarizations()

    indices = [0,1,5]

    graph_multiple_polarization_time([polarization[i] for i in indices], [static_data[i] for i in indices], 'noiseAmplitude', 'Amplitud de Ruido')


def generate_video_from_first():
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    generate_video(particle_files[0], static_files[0], False, 100)


def generate_gif_from_first():
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    generate_gif(particle_files[0], static_files[0])


if __name__ == '__main__':
    generate_gif_from_first()


