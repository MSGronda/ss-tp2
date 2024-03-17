import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class OffLatticeSimulation {

    private List<Particle> particles;
    private final double l;
    private final double v;
    private final int n;
    private final double r;
    private final int m;
    private final double noiseAmplitude;

    private final Random random;

    public OffLatticeSimulation(int n, double l, double r, double v, double noiseAmplitude){
        this.n = n;
        this.l = l;
        this.r = r;
        this.v = v;
        this.noiseAmplitude = noiseAmplitude;

        this.m = (int) Math.floor((l/r) - 0.00001);

        this.particles = ParticleUtils.createParticles(n, l);
        this.random = new Random(2);
    }

    public List<Particle> simulate(){

        CellIndexMethod.setPeriodicPlane(true);

        Map<Particle, Set<Particle>> neighbors = CellIndexMethod.computeNeighbors(particles, l, m, r);

        List<Particle> updatedParticles = particles.stream().map(Particle::clone).collect(Collectors.toList());

        for(Particle p : updatedParticles){
            Set<Particle> pNeighbors  = neighbors.getOrDefault(p, new HashSet<>());
            updateParticle(p, pNeighbors);
        }

        particles = updatedParticles;
        return updatedParticles;
    }

    private void updateParticle(Particle p, Set<Particle> neighbors){
        // Updateamos la posicion
        Coordinate oldPos = p.getPos();
        oldPos.setX( posWrapping( oldPos.getX() + Math.cos(p.getAngle()) * v ) );
        oldPos.setY( posWrapping( oldPos.getY() + Math.sin(p.getAngle()) * v ) );

        // Updateamos el angulo
        double sinTotal = Math.sin(p.getAngle());
        double cosTotal = Math.cos(p.getAngle());
        for(Particle neighbor: neighbors){
            sinTotal += Math.sin(neighbor.getAngle());
            cosTotal += Math.cos(neighbor.getAngle());
        }
        p.setAngle( angleWrapping( Math.atan2(sinTotal / (neighbors.size() + 1), cosTotal / (neighbors.size() + 1)) + generateNoise() ) );
    }

    public double calculatePolarization(){
        double xSum = 0;
        double ySum = 0;
        for(Particle p : particles){
            xSum += Math.cos(p.getAngle()) * v;
            ySum += Math.sin(p.getAngle()) * v;
        }
        return (Math.sqrt(xSum * xSum + ySum * ySum)) / (n * v);
    }

    private double angleWrapping(double angle) {
        double newAngle = angle;
        while (newAngle > Math.PI){
            newAngle -= 2*Math.PI;
        }
        while (newAngle < -Math.PI){
            newAngle += 2*Math.PI;
        }
        return newAngle;
    }

    private double posWrapping(double pos){
        double newPos = pos;
        while(newPos<0){
            newPos += l;
        }
        while(newPos>l) {
            newPos -= l;
        }
        return newPos;
    }

    private double generateNoise(){
        return noiseAmplitude * (random.nextDouble() - 0.5);
    }

    public double getV() {
        return v;
    }


    ///////////////////////// MULTITHREADING ///////////////////////////////
    private ExecutorService pool = null;
    private Map<Particle, Set<Particle>> neighbors;
    private List<Particle> newParticles;
    private List<UpdateParticleTask> updateTasks = null;

    private class UpdateParticleTask implements Callable<Integer> {
        private int from;
        private int to;
        public UpdateParticleTask(int from, int to){
            this.from = from;
            this.to = to;
        }

        @Override
        public Integer call() {
            if(newParticles == null) {
                return -1;
            }
            Iterator<Particle> iterator = newParticles.listIterator(from);

            int count = 0;
            while(iterator.hasNext() && count < to - from){
                Particle p = iterator.next();
                Set<Particle> pNeighbors  = neighbors.getOrDefault(p, new HashSet<>());
                updateParticle(p, pNeighbors);
                count++;
            }
            return count;
        }
    }

    public void setupParallel(int threadCount) {
        pool = Executors.newCachedThreadPool();
        updateTasks = new ArrayList<>();

        int particlesPerThread = particles.size()/threadCount;
        for(int i=0; i<threadCount; i++){
            if(i==threadCount-1){
                updateTasks.add(new UpdateParticleTask(i * particlesPerThread, particles.size()));
            }
            else{
                updateTasks.add(new UpdateParticleTask(i * particlesPerThread, (i+1) * particlesPerThread));
            }
        }

    }

    public List<Particle> simulateParallel() {
        if(pool == null){
            throw new RuntimeException("Inicializamelo");
        }

        CellIndexMethod.setPeriodicPlane(true);

        // Calculamos los neighbors de forma paralela
        this.neighbors = CellIndexMethod.computeNeighborsParallel(particles, l, m, r, pool, updateTasks.size());

        this.newParticles = particles.stream().map(Particle::clone).collect(Collectors.toList());

        // Updateamos las particulas de forma paralela
        try{
            pool.invokeAll(updateTasks);
        }
        catch (InterruptedException e){
            System.out.println(e);
        }

        particles = newParticles;
        return newParticles;
    }

    public void finishParallel(){
        if(pool != null){
            pool.shutdown();
        }
    }

    ////////////////////////////////////////////////////////
}
