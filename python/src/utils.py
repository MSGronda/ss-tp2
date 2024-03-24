import csv

def is_float(element: any) -> bool:
    if element is None:
        return False
    try:                    # Full cabeza
        float(element)
        return True
    except ValueError:
        return False

def get_static_data(file_name: str):
    data = {}

    with open(file_name, mode='r') as file:
        csv_reader = csv.reader(file)
        for row in csv_reader:
            key = row[0]

            if row[1].isdigit():
                value = int(row[1])
            elif is_float(row[1]):
                value = float(row[1])
            else:
                value = row[1]

            data[key] = value
    return data


def get_visits(file_name: str) -> []:
    with open(file_name, 'r') as f:

        visits_per_epoch = []

        csv_reader = csv.reader(f)
        for row in csv_reader:
            visits_per_epoch.append(int(row[1]))

        return visits_per_epoch

