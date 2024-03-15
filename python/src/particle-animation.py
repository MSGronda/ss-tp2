import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

# Read the data
with open('../output-files/particle-movement.txt', 'r') as file:
    lines = file.readlines()

# Initialize variables
timesteps = []
x_values = []
y_values = []
dx_values = []
dy_values = []

# Iterate through each line in the file
for line in lines:
    if line.strip() == '':  # Skip empty lines
        continue

    if line.strip().endswith(','):  # Check for time step line
        if x_values:  # Store particles for the previous time step
            timesteps.append((x_values, y_values, dx_values, dy_values))

        # Reset lists for the new time step
        x_values = []
        y_values = []
        dx_values = []
        dy_values = []

        continue

    # Extract particle data
    data = line.split(',')
    x = float(data[0])
    y = float(data[1])
    angle = float(data[2])
    velocity = float(data[3])

    # Calculate velocity components
    dx = velocity * np.cos(angle)
    dy = velocity * np.sin(angle)

    # Store particle data
    x_values.append(x)
    y_values.append(y)
    dx_values.append(dx)
    dy_values.append(dy)

# Function to update the plot for each frame of the animation
def update(frame):
    ax.clear()
    ax.set_title(f'Time Step: {frame}')
    ax.set_xlabel('X Coordinate')
    ax.set_ylabel('Y Coordinate')
    ax.grid(True)
    ax.set_aspect('equal', adjustable='box')
    ax.set_xlim(0, 5)  # Set x-axis limits
    ax.set_ylim(0, 5)  # Set y-axis limits
    ax.scatter(timesteps[frame][0], timesteps[frame][1], color='b')
    ax.quiver(timesteps[frame][0], timesteps[frame][1], timesteps[frame][2], timesteps[frame][3],
              angles='xy', scale_units='xy', scale=1, color='r')

# Create a figure and axis object
fig, ax = plt.subplots(figsize=(8, 6))

# Set up the animation
ani = FuncAnimation(fig, update, frames=len(timesteps), interval=100)

# Save the animation as a GIF
ani.save('particle_animation.gif', writer='pillow')

plt.show()
