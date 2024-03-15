import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Read the data
with open('../output-files/particle-movement.txt', 'r') as file:
    lines = file.readlines()

# Initialize variables
x_values = []
y_values = []
dx_values = []
dy_values = []
timestep = None

# Iterate through each line in the file
for line in lines:
    if line.strip() == '':  # Skip empty lines
        continue

    if line.strip().endswith(','):  # Check for time step line
        if x_values:  # Plot particles for the previous time step
            fig, ax = plt.subplots(figsize=(8, 6))
            ax.set_title(f'Time Step: {timestep}')  # Set title for the plot
            ax.set_xlabel('X Coordinate')
            ax.set_ylabel('Y Coordinate')
            ax.grid(True)
            ax.set_aspect('equal', adjustable='box')
            ax.scatter(x_values, y_values, color='b')
            ax.quiver(x_values, y_values, dx_values, dy_values, angles='xy', scale_units='xy', scale=1, color='r')
            plt.show()

        timestep = int(line.strip()[:-1])  # Extract time step
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

# Plot particles for the last time step
if x_values:
    fig, ax = plt.subplots(figsize=(8, 6))
    ax.set_title(f'Time Step: {timestep}')  # Set title for the plot
    ax.set_xlabel('X Coordinate')
    ax.set_ylabel('Y Coordinate')
    ax.grid(True)
    ax.set_aspect('equal', adjustable='box')
    ax.scatter(x_values, y_values, color='b')
    ax.quiver(x_values, y_values, dx_values, dy_values, angles='xy', scale_units='xy', scale=1, color='r')
    plt.show()
