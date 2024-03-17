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


def calculate_line_color(dx, dy):
    angle1 = np.arctan2(dy, dx)
    if angle1 < 0:
        angle1 += 2 * np.pi
    hue = angle1 / (2 * np.pi)
    return hue


def update(frame):
    ax.clear()
    ax.set_title(f'Time Step: {frame}')
    ax.set_xlabel('X Coordinate')
    ax.set_ylabel('Y Coordinate')
    ax.grid(True)
    ax.set_aspect('equal', adjustable='box')
    ax.set_xlim(0, 5)
    ax.set_ylim(0, 5)
    for i in range(len(timesteps[frame][0])):
        x = timesteps[frame][0][i]
        y = timesteps[frame][1][i]
        dx = timesteps[frame][2][i]
        dy = timesteps[frame][3][i]
        line_color = calculate_line_color(dx, dy)
        cmap = plt.cm.get_cmap('hsv', 20)
        ax.plot([x, x + dx], [y, y + dy], color=cmap(line_color), linewidth=2)


fig, ax = plt.subplots(figsize=(8, 6))

ani = FuncAnimation(fig, update, frames=len(timesteps), interval=100)

ani.save('../animations/particle_animation.gif', writer='pillow')

plt.show()
