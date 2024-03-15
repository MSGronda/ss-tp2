import java.util.*;
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
        p.setAngle(Math.atan2(sinTotal / (neighbors.size() + 1), cosTotal / (neighbors.size() + 1)) + generateNoise());
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
}
