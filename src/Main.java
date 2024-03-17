import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Main {
    public static void main(String[] args) {
        long timeTaken = runParallel(25000,250, 5, 0.2, 0.5, 1000, 10);
        System.out.println("Time taken: " + timeTaken/1000 + "s");
    }
    public static long runNormal(int n, double l, double r, double v, double noiseAmplitude, int epochs) {
        long start = System.currentTimeMillis();
        OffLatticeSimulation simulation = new OffLatticeSimulation(n,l, r, v, noiseAmplitude);

        try(FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
            for(int i=0; i<epochs; i++){
                List<Particle> particles = simulation.simulate();

                writer.write(i + ",\n");
                for( Particle p : particles){
                    writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "," + simulation.getV() + "\n");
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
        OffLatticeSimulation simulation = new OffLatticeSimulation(n,l, r, v, noiseAmplitude);

        simulation.setupParallel(threadCount);

        try(FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
            for(int i=0; i<epochs; i++){
                List<Particle> particles = simulation.simulateParallel();

                writer.write(i + ",\n");
                for( Particle p : particles){
                    writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "," + simulation.getV() + "\n");
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
        OffLatticeSimulation simulation = new OffLatticeSimulation(n,l, r, v, noiseAmplitude);

        IOThread task = new IOThread();
        task.v = simulation.getV();
        Thread thread = new Thread(task);
        thread.start();

        simulation.setupParallel(threadCount);

        for(int i=0; i<epochs; i++){
            List<Particle> particles = simulation.simulateParallel();
            task.writes.add(particles);
        }

        task.endThread();
        try {
            thread.join();
        }
        catch (InterruptedException e){
            System.out.println(e);
        }

        simulation.finishParallel();
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static class IOThread implements Runnable {
        private final BlockingQueue<List<Particle>> writes = new ArrayBlockingQueue<>(20);// TODO: check
        private boolean finished = false;
        private double v;

        @Override
        public void run() {
            int i=0;

            try(FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
                while(!finished){
                    List<Particle> particles = writes.take();

                    writer.write(i + ",\n");
                    for(Particle p : particles){
                        writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "," + v + "\n");
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
