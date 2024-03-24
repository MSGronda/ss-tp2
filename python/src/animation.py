import cv2
import numpy as np
from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation

from src.utils import get_static_data, get_visits


def angle_to_color(angle):
    hue = angle * 180 / np.pi
    hue = (hue + 180) % 360
    hue /= 2
    return cv2.cvtColor(np.array([[[hue, 255, 255]]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0].tolist()


# angle_color:
# True: usa una gradiente de colores para mostrar el angulo
# False: usa una linea para mostrar el angulo

def generate_video(particle_filename: str, static_data_filename: str, angle_color: bool, size_multiplier: int):
    static_data = get_static_data(static_data_filename)

    fps = 30
    screen_size = int(static_data['l'])
    width, height = screen_size * size_multiplier, screen_size * size_multiplier
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    line_length = 15

    out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (width, height))

    with open(particle_filename, 'r') as f:
        lines = f.readlines()

    prev_x = []
    prev_y = []
    prev_angle = []
    timestep = 0

    for line in lines:
        if line.strip() == '':
            continue

        if line.strip().endswith(','):
            if prev_x and prev_y and prev_angle:
                frame = np.ones((height, width, 3), dtype=np.uint8) * 255
                for x, y, angle in zip(prev_x, prev_y, prev_angle):

                    color = angle_to_color(angle) if angle_color else (0, 0, 255)

                    cv2.circle(frame, (int(x * size_multiplier), int(y * size_multiplier)), radius=4, color=color,
                               thickness=-1)

                    if not angle_color:
                        end_x = int(x * size_multiplier + line_length * np.cos(angle))
                        end_y = int(y * size_multiplier + line_length * np.sin(angle))
                        cv2.line(frame, (int(x * size_multiplier), int(y * size_multiplier)), (end_x, end_y),
                                 (255, 0, 0), thickness=2)

                text = f'Timestep: {timestep}'
                cv2.putText(frame, text, (int(width / 2) - len(text) * 7, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0),
                            2, cv2.LINE_AA)

                out.write(frame)

            prev_x.clear()
            prev_y.clear()
            prev_angle.clear()
            timestep += 1
            continue

        data = line.split(',')
        x = float(data[0])
        y = float(data[1])
        angle = float(data[2])
        prev_x.append(x)
        prev_y.append(y)
        prev_angle.append(angle)

    out.release()

def generate_video_counting_visits(particle_filename: str, visits_filename: str, static_data_filename: str, angle_color: bool, size_multiplier: int):
    static_data = get_static_data(static_data_filename)

    fps = 30
    screen_size = int(static_data['l'])
    width, height = screen_size * size_multiplier, screen_size * size_multiplier
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    line_length = 15

    out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (width, height))

    with open(particle_filename, 'r') as f:
        particle_lines = f.readlines()

    visits_per_epoch = get_visits(visits_filename)

    prev_x = []
    prev_y = []
    prev_angle = []
    timestep = 0
    total_visits = 0

    for line in particle_lines:
        if line.strip() == '':
            continue

        if line.strip().endswith(','):
            if prev_x and prev_y and prev_angle:
                frame = np.ones((height, width, 3), dtype=np.uint8) * 255

                cv2.circle(frame, (int(width/2), int(height/2)), radius=int(static_data['visitRadius'] * size_multiplier), color=(0, 0, 255),  thickness=1)

                for x, y, angle in zip(prev_x, prev_y, prev_angle):

                    color = angle_to_color(angle) if angle_color else (0, 0, 255)

                    cv2.circle(frame, (int(x * size_multiplier), int(y * size_multiplier)), radius=4, color=color, thickness=-1)

                    if not angle_color:
                        end_x = int(x * size_multiplier + line_length * np.cos(angle))
                        end_y = int(y * size_multiplier + line_length * np.sin(angle))
                        cv2.line(frame, (int(x * size_multiplier), int(y * size_multiplier)), (end_x, end_y),
                                 (255, 0, 0), thickness=2)

                timestep_text = f'Timestep: {timestep}'
                cv2.putText(frame, timestep_text, (int(width / 2) - len(timestep_text) * 7, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0),
                            2, cv2.LINE_AA)

                total_visits += visits_per_epoch[timestep]
                visitText = f'Total visits: {total_visits}'
                cv2.putText(frame, visitText, (int(width / 2) - len(visitText) * 7, height - 20), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0),
                            2, cv2.LINE_AA)
                timestep += 1

                out.write(frame)

            prev_x.clear()
            prev_y.clear()
            prev_angle.clear()
            continue

        data = line.split(',')
        x = float(data[0])
        y = float(data[1])
        angle = float(data[2])
        prev_x.append(x)
        prev_y.append(y)
        prev_angle.append(angle)

    out.release()



def calculate_line_color(dx, dy):
    angle1 = np.arctan2(dy, dx)
    if angle1 < 0:
        angle1 += 2 * np.pi
    hue = angle1 / (2 * np.pi)
    return hue


def generate_gif(particle_filename: str, static_data_filename: str):
    with open(particle_filename, 'r') as file:
        lines = file.readlines()

    static_data = get_static_data(static_data_filename)

    timesteps = []
    x_values = []
    y_values = []
    dx_values = []
    dy_values = []
    velocity = static_data['v']

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
        x_values.append(x)
        y_values.append(y)

        angle = float(data[2])
        dx = velocity * np.cos(angle)
        dy = velocity * np.sin(angle)

        dx_values.append(dx)
        dy_values.append(dy)

    def update(frame):
        ax.clear()
        ax.set_title(f'Time Step: {frame}')
        ax.set_xlabel('X Coordinate')
        ax.set_ylabel('Y Coordinate')
        ax.grid(True)
        ax.set_aspect('equal', adjustable='box')
        ax.set_xlim(0, int(static_data['l']))
        ax.set_ylim(0, int(static_data['l']))
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


