import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation


with open('../output-files/particle-movement.txt', 'r') as file:
    lines = file.readlines()

timesteps = []
x_values = []
y_values = []
dx_values = []
dy_values = []


for line in lines:
    if line.strip() == '':
        continue

    if line.strip().endswith(','):
        if x_values:
            timesteps.append((x_values, y_values, dx_values, dy_values))

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

def update(frame):
    ax.clear()
    ax.set_title(f'Time Step: {frame}')
    ax.set_xlabel('X Coordinate')
    ax.set_ylabel('Y Coordinate')
    ax.grid(True)
    ax.set_aspect('equal', adjustable='box')
    ax.set_xlim(0, 5)
    ax.set_ylim(0, 5)
    ax.scatter(timesteps[frame][0], timesteps[frame][1], color='b')
    ax.quiver(timesteps[frame][0], timesteps[frame][1], timesteps[frame][2], timesteps[frame][3],
              angles='xy', scale_units='xy', scale=1, color='r')


fig, ax = plt.subplots(figsize=(8, 6))

ani = FuncAnimation(fig, update, frames=len(timesteps), interval=100)

ani.save('../animations/particle_animation.gif', writer='pillow')

plt.show()
