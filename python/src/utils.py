import csv
import glob


def get_static_data(file_name: str):
    data = {}

    with open(file_name, mode='r') as file:
        csv_reader = csv.reader(file)
        for row in csv_reader:
            key = row[0]
            value = int(row[1]) if row[1].isdigit() else float(row[1])
            data[key] = value
    return data


def get_all_files(prefix: str) -> [str]:
    pattern = f"{prefix}-*.txt"
    return glob.glob(pattern)
