import concurrent.futures
from src.polarization import *
from src.utils import get_static_data


def process_files(p, s):
    data = get_static_data(s)
    pol = calculate_polarization(p, data)
    return data, pol


if __name__ == '__main__':
    particle_files = get_all_files("../output-files/particle-movement")
    static_files = get_all_files("../output-files/static-data")

    polarization = []
    static_data = []

    with concurrent.futures.ProcessPoolExecutor() as executor:
        results = executor.map(process_files, particle_files, static_files)

        for data, pol in results:
            static_data.append(data)
            polarization.append(pol)


    compare_polarizations(polarization, static_data, 'noiseAmplitude', 500)


