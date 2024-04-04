import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Main {
    private static String decimalFormat = "%.4f";

    public static void main(String[] args) {

        int n = 150;
        double l = 5;
        double r = 0.2;
        double v = 0.1;
        double noiseAmplitude = 0.0;
        int epochs = 1000;

        for(double i = 0; i <= 5.1 ; i += 0.25){
            for( int j = 0; j < 7 ; j++) {
                long timestamp = System.currentTimeMillis();
                double noise = (double) Math.round((noiseAmplitude + i) * 100) / 100;
                writeStaticFile(n, l, r, v, noise, epochs, timestamp);
                runSemiParallel(n, l, r, v, noise, epochs, timestamp);
            }
        }
//        long timestamp = System.currentTimeMillis();
//        writeStaticFile(n, l, r, v, noiseAmplitude, epochs, timestamp);
//        runSemiParallel(n, l, r, v, noiseAmplitude, epochs, timestamp);
    }

    private static void writeStaticFile(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp){
        try(FileWriter writer = new FileWriter("./python/output-files/static-data-" + timestamp + ".txt")) {
            writer.write("n," + n + "\n");
            writer.write("v," + v+ "\n");
            writer.write("l," + l + "\n");
            writer.write("rc," + r + "\n");
            writer.write("noiseAmplitude," + noiseAmplitude + "\n");
            writer.write("epochs," + epochs + "\n");
            writer.write("density," + (float) n / (l*l) + "\n");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }



    private static long runNormal(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {
        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
            writer.write(0 + ",\n");
            simulation.getParticles().forEach(p -> saveParticleData(p, writer));

            for (int i = 1; i <= epochs; i++) {
                writer.write(i + ",\n");
                simulation.simulate().forEach(p -> saveParticleData(p, writer));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long runSemiParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
            writer.write(0 + ",\n");
            simulation.getParticles().forEach(p -> saveParticleData(p, writer));

            for (int i = 1; i <= epochs; i++) {
                writer.write(i + ",\n");
                simulation.simulateParallel().forEach(p -> saveParticleData(p, writer));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long runParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        IOThread task = new IOThread(timestamp);
        Thread thread = new Thread(task);
        thread.start();

        task.writes.add(simulation.getParticles());
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

        long end = System.currentTimeMillis();
        return end - start;
    }

    private static void saveParticleData(Particle p, FileWriter writer) {
        try{
            writer.write(p.getId() + "," + String.format(Locale.US, decimalFormat, p.getPos().getX()) + "," + String.format(Locale.US, decimalFormat, p.getPos().getY()) + "," + String.format(Locale.US, decimalFormat, p.getAngle()) + "\n");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    private static class IOThread implements Runnable {
        private final BlockingQueue<List<Particle>> writes = new ArrayBlockingQueue<>(20);// TODO: check
        private boolean finished = false;
        private final long timestamp;
        public IOThread(long timestamp){
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            int i = 0;

            try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
                while (!finished) {
                    writer.write(i + ",\n");
                    writes.take().forEach(p -> saveParticleData(p, writer));
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
}
