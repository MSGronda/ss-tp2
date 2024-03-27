from src.polarization import *
from src.animation import generate_video, generate_gif, generate_video_visits
from src.visits import visits_graph, calculate_all_visits, compare_total_visits


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


def generate_visits_graph(option: str):
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    visits_graph(particle_files[0], static_files[0], option == "PBC")


def compare_and_graph_total_visits(type: str):
    total_visits, static_datas = calculate_all_visits(type == "PBC")

    compare_total_visits(total_visits, static_datas, 'noiseAmplitude', 'ƞ', type == "PBC")
    #compare_total_visits(total_visits, static_datas, 'n', 'Number of particles')



if __name__ == '__main__':
    # particle_files = get_all_files("../output-files/particle-movement")
    # static_files = get_all_files("../output-files/static-data")
    #
    # generate_video_visits(particle_files[0], static_files[0], False, 200, False, 0.5)
    #generate_visits_graph("OBC")
    compare_and_graph_total_visits("OBC")
