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

    private int epoch = 0;

    // Visits
    private double visitRadius;
    private VisitType type;
    private Set<Map.Entry<Integer, Integer>> insideRadiusPreviously;
    private List<Integer> visistsPerEpoch;
    private Map<Particle, Integer> exitedParticles;
    private int currentEpochVisits;

    public OffLatticeSimulation(int n, double l, double r, double v, double noiseAmplitude){
        this.n = n;
        this.l = l;
        this.r = r;
        this.v = v;
        this.noiseAmplitude = noiseAmplitude;

        this.m = (int) Math.floor((l/r) - 0.00001);

        this.particles = ParticleUtils.createParticles(n, l);
        this.random = new Random(2);

        this.type = VisitType.None;
    }

    public OffLatticeSimulation(int n, double l, double r, double v, double noiseAmplitude, double visitRadius, VisitType type){
        this.n = n;
        this.l = l;
        this.r = r;
        this.v = v;
        this.noiseAmplitude = noiseAmplitude;

        this.m = (int) Math.floor((l/r) - 0.00001);

        this.particles = ParticleUtils.createParticles(n, l);
        this.random = new Random(2);

        this.type = type;

        // Visit
        if(this.type != VisitType.None){
            this.visitRadius = visitRadius;
            this.exitedParticles = new HashMap<>();
            this.insideRadiusPreviously = new HashSet<>();
            this.visistsPerEpoch = new ArrayList<>();
            this.currentEpochVisits = 0;

            this.particles.forEach(this::visit);
            visistsPerEpoch.add(currentEpochVisits);
            this.currentEpochVisits = 0;
        }
    }

    private Particle cloneAndUpdateParticle(Particle p, Set<Particle> neighbors){

        Particle resp = p.clone();

        updatePos(resp);
        updateAngle(resp, neighbors);

        if(this.type != VisitType.None){
            visit(resp);
        }

        return resp;
    }

    public List<Particle> simulate(){

        CellIndexMethod.setPeriodicPlane(true);
        Map<Particle, Set<Particle>> neighbors = CellIndexMethod.computeNeighbors(particles, l, m, r);

        List<Particle> updatedParticles = particles.stream().map(p -> cloneAndUpdateParticle(p, neighbors.getOrDefault(p, new HashSet<>()))).collect(Collectors.toList());

        particles = updatedParticles;

        updateEpoch();

        return updatedParticles;
    }

    public List<Particle> simulateParallel() {

        CellIndexMethod.setPeriodicPlane(true);
        // Calculamos los neighbors de forma paralela
        Map<Particle, Set<Particle>> n = CellIndexMethod.computeNeighborsParallel(particles, l, m, r);

        List<Particle> newParticles = particles.parallelStream().map(p -> cloneAndUpdateParticle(p, n.getOrDefault(p, new HashSet<>()))).toList();

        particles = newParticles;

        updateEpoch();

        return newParticles;
    }


    public void updatePos(Particle p){
        // Updateamos la posicion
        Coordinate oldPos = p.getPos();
        oldPos.setX( posWrapping( oldPos.getX() + Math.cos(p.getAngle()) * v , p) );
        oldPos.setY( posWrapping( oldPos.getY() + Math.sin(p.getAngle()) * v , p) );
    }

    private double posWrapping(double pos, Particle p){
        double newPos = pos;
        while(newPos<0){
            newPos += l;
        }
        while(newPos>l) {
            newPos -= l;
        }
        if(this.type == VisitType.OBC && newPos != pos){
            int exits = this.exitedParticles.getOrDefault(p, 0);
            this.exitedParticles.put(p, exits + 1);
        }
        return newPos;
    }

    public void updateAngle(Particle p, Set<Particle> neighbors){
        // Updateamos el angulo
        double sinTotal = Math.sin(p.getAngle());
        double cosTotal = Math.cos(p.getAngle());
        for(Particle neighbor: neighbors){
            sinTotal += Math.sin(neighbor.getAngle());
            cosTotal += Math.cos(neighbor.getAngle());
        }
        p.setAngle(Math.atan2(sinTotal / (neighbors.size() + 1), cosTotal / (neighbors.size() + 1)) + generateNoise());

    }

    public void updateEpoch(){
        epoch++;
        if(this.type == VisitType.OBC || this.type == VisitType.PBC){
            this.visistsPerEpoch.add(this.currentEpochVisits);
            this.currentEpochVisits = 0;
        }
    }

    /////////////////////////////// VISITS ///////////////////////////////
    public enum VisitType {
        OBC,
        PBC,
        None
    }
    private void visit(Particle p){

        if(
            Math.pow(p.getPos().getX() - l/2, 2)+ Math.pow(p.getPos().getY() - l/2, 2) <= visitRadius * visitRadius
        ) {
            Map.Entry<Integer, Integer> entry;
            switch (this.type){
                case PBC -> entry = new AbstractMap.SimpleEntry<>(p.getId(), 0);
                case OBC -> entry = new AbstractMap.SimpleEntry<>(p.getId(), exitedParticles.getOrDefault(p, 0));
                default -> throw new RuntimeException("No deberia ocurrir");
            }
            if(!this.insideRadiusPreviously.contains(entry)){
                insideRadiusPreviously.add(entry);
                this.currentEpochVisits++;
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////

    private double generateNoise(){
        return noiseAmplitude * (random.nextDouble() - 0.5);
    }

    public double getV() {
        return v;
    }
    public List<Integer> getVisistsPerEpoch(){
        return this.visistsPerEpoch;
    }

    public List<Particle> getParticles(){
        return this.particles;
    }
}
