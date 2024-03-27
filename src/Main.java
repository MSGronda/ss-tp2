import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Main {
    public static void main(String[] args) {

        int n = 300;
        double l = 5;
        double r = 1;
        double v = 0.1;
        double noiseAmplitude = 0.0;
        int epochs = 1000;


//        writeStaticFile(n,l,r,v,noiseAmplitude, epochs, timestamp);
//        long timeTaken = runNormal(n,l,r,v,noiseAmplitude, epochs, timestamp);
//        System.out.println("Time taken: " + timeTaken/1000 + "s");
        for( double i = 0; i <= 5.1 ; i+= 0.2){
            long timestamp = System.currentTimeMillis();
            double noise = (double) Math.round((noiseAmplitude + i) * 100) / 100;
            writeStaticFile(n, l, r, v, noise, epochs, timestamp);
            runNormal(n, l, r, v, noise, epochs, timestamp);
        }
    }

    public static long runSemiParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {

            writer.write(0 + ",\n");
            for(Particle p : simulation.getParticles()){
                writer.write(p.getId() + "," + p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
            }

            for (int i = 1; i <= epochs; i++) {
                List<Particle> particles = simulation.simulateParallel();

                writer.write(i + ",\n");
                for(Particle p : particles){
                    writer.write(p.getId() + "," + p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    public static void writeStaticFile(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp){
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

    public static long runNormal(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {
        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
            writer.write(0 + ",\n");
            for(Particle p : simulation.getParticles()){
                writer.write(p.getId() + "," + p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
            }

            for (int i = 1; i <= epochs; i++) {
                List<Particle> particles = simulation.simulateParallel();

                writer.write(i + ",\n");
                for(Particle p : particles){
                    writer.write(p.getId() + "," + p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long runParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        IOThread task = new IOThread(timestamp);
        Thread thread = new Thread(task);
        thread.start();

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

    public static class IOThread implements Runnable {
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
}
