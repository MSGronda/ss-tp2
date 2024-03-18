import cv2
import numpy as np

def angle_to_color(angle):
    hue = angle * 180 / np.pi
    hue = (hue + 180) % 360
    hue /= 2
    return cv2.cvtColor(np.array([[[hue, 255, 255]]], dtype=np.uint8), cv2.COLOR_HSV2BGR)[0][0].tolist()


size_multiplier = 2
fps = 30
width, height = 500 * size_multiplier, 500 * size_multiplier
fourcc = cv2.VideoWriter_fourcc(*'mp4v')

out = cv2.VideoWriter('../animations/simulation_video.mp4', fourcc, fps, (width, height))

with open('../output-files/particle-movement.txt', 'r') as f:
    lines = f.readlines()

prev_x = []
prev_y = []
prev_angle = []

for line in lines:
    if line.strip() == '':
        continue

    if line.strip().endswith(','):
        if prev_x and prev_y and prev_angle:
            frame = np.ones((height, width, 3), dtype=np.uint8) * 255
            for x, y, angle in zip(prev_x, prev_y, prev_angle):
                color = angle_to_color(angle)
                cv2.circle(frame, (int(x * size_multiplier), int(y * size_multiplier)), radius=2, color=color, thickness=-1)

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
