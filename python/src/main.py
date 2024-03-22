from src.polarization import *
from src.utils import get_static_data

if __name__ == '__main__':
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    polarization = []
    static_data = []

    for p, s in zip(particle_files, static_files):
        data = get_static_data(s)
        pol = calculate_polarization(p, data)

        static_data.append(data)
        polarization.append(pol)


    graph_multiple_polarization_time(
        [polarization[0], polarization[5], polarization[10]],
        [static_data[0], static_data[5], static_data[10]],
        'noiseAmplitude'
    )


