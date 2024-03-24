import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Main {
    public static void main(String[] args) {
        int n = 20;
        double l = 5;
        double r = 0.4;
        double v = 0.1;
        double noiseAmplitude = 0.5;
        int epochs = 1000;

        double visitRadius = 1;
        OffLatticeSimulation.VisitType type = OffLatticeSimulation.VisitType.OBC;

        long timestamp = System.currentTimeMillis();
        writeStaticFile(n,l,r,v,noiseAmplitude, epochs, timestamp, visitRadius, type);

        long timeTaken = runVisits(n,l,r,v, noiseAmplitude, epochs, timestamp, visitRadius, type);
        System.out.println("Time taken: " + timeTaken/1000 + "s");
    }

    public static long runVisits(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp, double visitRadius, OffLatticeSimulation.VisitType type) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude, visitRadius, type);

        try (
                FileWriter movements = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt");
                FileWriter visits = new FileWriter("./python/output-files/visits-" + timestamp + ".txt")
        ) {
            movements.write(0 + ",\n");
            for(Particle p : simulation.getParticles()){
                movements.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
            }

            for (int i = 1; i <= epochs; i++) {
                List<Particle> particles = simulation.simulate();

                movements.write(i + ",\n");
                for(Particle p : particles){
                    movements.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "\n");
                }
                movements.write("\n");
            }

            int i=0;
            for(Integer visitsOfEpoch : simulation.getVisistsPerEpoch()){
                visits.write(i + "," + visitsOfEpoch + "\n");
                i++;
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

    public static void writeStaticFile(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp, double visitRadius, OffLatticeSimulation.VisitType type){
        try(FileWriter writer = new FileWriter("./python/output-files/static-data-" + timestamp + ".txt")) {
            writer.write("n," + n + "\n");
            writer.write("v," + v+ "\n");
            writer.write("l," + l + "\n");
            writer.write("rc," + r + "\n");
            writer.write("noiseAmplitude," + noiseAmplitude + "\n");
            writer.write("epochs," + epochs + "\n");
            writer.write("density," + (float) n / (l*l) + "\n");

            writer.write("visitRadius," + visitRadius + "\n");
            writer.write("visitType," + type.name() + "\n");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    public static long runNormal(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {
        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
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

    public static long runSemiParallel(int n, double l, double r, double v, double noiseAmplitude, int epochs, long timestamp) {

        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n, l, r, v, noiseAmplitude);

        try (FileWriter writer = new FileWriter("./python/output-files/particle-movement-" + timestamp + ".txt")) {
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
