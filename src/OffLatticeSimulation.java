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

    private Particle cloneAndUpdateParticle(Particle p, Set<Particle> neighbors){
        Particle resp = p.clone();
        // Updateamos la posicion
        Coordinate oldPos = resp.getPos();
        oldPos.setX( posWrapping( oldPos.getX() + Math.cos(resp.getAngle()) * v ) );
        oldPos.setY( posWrapping( oldPos.getY() + Math.sin(resp.getAngle()) * v ) );

        // Updateamos el angulo
        double sinTotal = Math.sin(resp.getAngle());
        double cosTotal = Math.cos(resp.getAngle());
        for(Particle neighbor: neighbors){
            sinTotal += Math.sin(neighbor.getAngle());
            cosTotal += Math.cos(neighbor.getAngle());
        }
        resp.setAngle( angleWrapping( Math.atan2(sinTotal / (neighbors.size() + 1), cosTotal / (neighbors.size() + 1)) + generateNoise() ) );

        return resp;
    }

    public List<Particle> simulate(){

        CellIndexMethod.setPeriodicPlane(true);

        Map<Particle, Set<Particle>> neighbors = CellIndexMethod.computeNeighbors(particles, l, m, r);

        List<Particle> updatedParticles = particles.stream().map(p -> cloneAndUpdateParticle(p, neighbors.getOrDefault(p, new HashSet<>()))).collect(Collectors.toList());

        particles = updatedParticles;
        return updatedParticles;
    }

    public List<Particle> simulateParallel() {
        CellIndexMethod.setPeriodicPlane(true);

        // Calculamos los neighbors de forma paralela
        Map<Particle, Set<Particle>> n = CellIndexMethod.computeNeighborsParallel(particles, l, m, r);

        List<Particle> newParticles = particles.parallelStream().map(p -> cloneAndUpdateParticle(p, n.getOrDefault(p, new HashSet<>()))).toList();

        particles = newParticles;
        return newParticles;
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

    public List<Particle> getParticles(){
        return this.particles;
    }





}
