# Trabajo Práctico 2 - SS

## Requisitos:
1) Java 17 o mayor
2) Python 3
2) Pip 3

## Preparacion del entorno:
Ejecutar en una linea de comnados (con pip instalado):
```
pip install -r requirements.txt
```
## Ejecucion de simulación:
1. Editar configuracion de funcion main en el archivo Main.
```agsl
int n = 150;
double l = 5;
double r = 0.2;
double v = 0.1;
double noiseAmplitude = 0;
int epochs = 1000;
```
2. Ejecutar funcion main en archivo Main.
3. La salida la simulacion se encuentra en ```python/output-files```.

## Graficos y Animaciones
Las funciones para generar los graficos y animaciones se encuentran en ```python/src/main.py```.
Debe elegir la correspondiente y ejecutar el archivo:
```
python3 python/src/main.py
```
La salida las animaciones se encuentran en  ```python/animations```.
