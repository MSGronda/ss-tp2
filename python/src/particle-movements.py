import numpy as np
import matplotlib.pyplot as plt


with open('../output-files/particle-movement.txt', 'r') as file:
    lines = file.readlines()

x_values = []
y_values = []
dx_values = []
dy_values = []
timestep = None


for line in lines:
    if line.strip() == '':
        continue

    if line.strip().endswith(','):
        if x_values:
            fig, ax = plt.subplots(figsize=(8, 6))
            ax.set_title(f'Time Step: {timestep}')
            ax.set_xlabel('X Coordinate')
            ax.set_ylabel('Y Coordinate')
            ax.grid(True)
            ax.set_aspect('equal', adjustable='box')
            ax.scatter(x_values, y_values, color='b')
            ax.quiver(x_values, y_values, dx_values, dy_values, angles='xy', scale_units='xy', scale=1, color='r')
            plt.show()

        timestep = int(line.strip()[:-1])
        x_values = []
        y_values = []
        dx_values = []
        dy_values = []
        continue

    data = line.split(',')
    x = float(data[0])
    y = float(data[1])
    angle = float(data[2])
    velocity = float(data[3])

    dx = velocity * np.cos(angle)
    dy = velocity * np.sin(angle)

    x_values.append(x)
    y_values.append(y)
    dx_values.append(dx)
    dy_values.append(dy)

if x_values:
    fig, ax = plt.subplots(figsize=(8, 6))
    ax.set_title(f'Time Step: {timestep}')
    ax.set_xlabel('X Coordinate')
    ax.set_ylabel('Y Coordinate')
    ax.grid(True)
    ax.set_aspect('equal', adjustable='box')
    ax.scatter(x_values, y_values, color='b')
    ax.quiver(x_values, y_values, dx_values, dy_values, angles='xy', scale_units='xy', scale=1, color='r')
    plt.show()
