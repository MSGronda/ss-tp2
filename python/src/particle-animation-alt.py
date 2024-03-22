import cv2
import numpy as np

from src.utils import get_static_data


def angle_to_color(angle):
    hue = angle * 180 / np.pi
    hue = (hue + 180) % 360
    hue /= 2
    return cv2.cvtColor(np.array([[[hue, 255, 255]]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0].tolist()


# True: usa una gradiente de colores para mostrar el angulo
# False: usa una linea para mostrar el angulo
angle_color = True


# Comienzo
static_data = get_static_data('../output-files/static-data.txt')

size_multiplier = 4
fps = 30
screen_size = int(static_data['l'])
width, height = screen_size * size_multiplier, screen_size * size_multiplier
fourcc = cv2.VideoWriter_fourcc(*'mp4v')
line_length = 15

out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (width, height))

with open('../output-files/particle-movement.txt', 'r') as f:
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

                cv2.circle(frame, (int(x * size_multiplier), int(y * size_multiplier)), radius=4, color=color, thickness=-1)

                if not angle_color:
                    end_x = int(x * size_multiplier + line_length * np.cos(angle))
                    end_y = int(y * size_multiplier + line_length * np.sin(angle))
                    cv2.line(frame, (int(x * size_multiplier), int(y * size_multiplier)), (end_x, end_y), (255,0,0), thickness=2)

            text = f'Timestep: {timestep}'
            cv2.putText(frame, text, (int(width/2) - len(text) * 7, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0), 2, cv2.LINE_AA)

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
