import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Main {
    public static void main(String[] args) {
        int n = 300;
        double l = 10;
        double r = 0.8;
        double v = 0.1;
        double noiseAmplitude = 0.5;
        int epochs = 2000;

        writeStaticFile(n,l,r,v,noiseAmplitude, epochs);

//        long timeTaken = runSemiParallel(n,l,r,v,noiseAmplitude, epochs, 10);
//        System.out.println("Time taken: " + timeTaken/1000 + "s");

        comparePolarization(n, l, r, v, epochs, 6);
    }

    public static void writeStaticFile(int n, double l, double r, double v, double noiseAmplitude, int epochs){
        try(FileWriter writer = new FileWriter("./python/output-files/static-data.txt")) {
            writer.write("n," + n + "\n");
            writer.write("v," + v+ "\n");
            writer.write("l," + l + "\n");
            writer.write("rc," + r + "\n");
            writer.write("noiseAmplitude," + noiseAmplitude + "\n");
            writer.write("epochs," + epochs + "\n");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }



    public static long runNormal(int n, double l, double r, double v, double noiseAmplitude, int epochs) {
        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
            for (int i = 0; i < epochs; i++) {
                List<Particle> particles = simulation.simulate();

                writer.write(i + ",\n");
                for( Particle p : particles) {
                    writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long runSemiParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, int threadCount) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        simulation.setupParallel(threadCount);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
            for (int i = 0; i < epochs; i++) {
                List<Particle> particles = simulation.simulateParallel();

                writer.write(i + ",\n");
                for(Particle p : particles){
                    writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        simulation.finishParallel();

        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long runParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, int threadCount) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        IOThread task = new IOThread();
        Thread thread = new Thread(task);
        thread.start();

        simulation.setupParallel(threadCount);

        for (int i = 0; i < epochs; i++) {
            List<Particle> particles = simulation.simulateParallel();
            task.writes.add(particles);
        }

        task.endThread();
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        simulation.finishParallel();
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static class IOThread implements Runnable {
        private final BlockingQueue<List<Particle>> writes = new ArrayBlockingQueue<>(20);// TODO: check
        private boolean finished = false;

        @Override
        public void run() {
            int i = 0;

            try (FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
                while (!finished) {
                    List<Particle> particles = writes.take();

                    writer.write(i + ",\n");
                    for(Particle p : particles){
                        writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                    }
                    writer.write("\n");

                    i++;
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void endThread() {
            finished = true;
        }
    }

    public static void polarizationChange(int n, double l, double r, double v, double noiseAmplitude, int epochs, int threadCount) {
        try (FileWriter writer = new FileWriter("./python/output-files/polarization-change.txt")) {
            writer.write("t, va\n");

            OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);
            simulation.setupParallel(threadCount);
            double polarization = simulation.calculatePolarization();

            writer.write("0," + polarization + "\n");

            for (int i = 0; i < epochs; i++) {
                simulation.simulateParallel();

                polarization = simulation.calculatePolarization();

                writer.write(i + 1 + "," + polarization + "\n");
            }
            simulation.finishParallel();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void comparePolarization(int n, double l, double r, double v, int epochs, int threadCount) {
        try (FileWriter writer = new FileWriter("./python/output-files/polarization-with-noise.txt")) {
            writer.write("n, va\n");

            for (double i = 0; i <= 5; i += 0.2) {
                OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, i);
                simulation.setupParallel(threadCount);

                int j = 0;
                while ( j < epochs ) {
                    simulation.simulateParallel();

                    if ( j >= 250){
                        writer.write(i + "," + simulation.calculatePolarization() + "\n");
                    }
                    j++;
                }
                simulation.finishParallel();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
