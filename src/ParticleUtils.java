import java.util.*;
import java.util.stream.Collectors;

public class ParticleUtils {

    private static final Random random = new Random(2);

    public static Map<Particle, Set<Particle>> bruteForceNeighbors(List<Particle> particles, double r){
        Map<Particle, Set<Particle>> neighborMap = new HashMap<>();
        for(Particle particle : particles){
            for(Particle otherParticle : particles){
                if (!particle.equals(otherParticle) && particle.isInRadius(otherParticle, r)){
                    neighborMap.putIfAbsent(particle, new HashSet<>());
                    neighborMap.putIfAbsent(otherParticle, new HashSet<>());

                    neighborMap.get(particle).add(otherParticle);
                    neighborMap.get(otherParticle).add(particle);
                }
            }
        }
        return neighborMap;
    }

    public static boolean particleIsSuperposed(Particle particle, List<Particle> particles){
        for(Particle other : particles){
            if(other.isSuperposed(particle.getPos().getX(), particle.getPos().getY(), particle.getRadius())){
                return false;
            }
        }
        return true;
    }

    public static List<Particle> createParticles(int n, double particle_r, double l) {
        List<Particle> particles = new ArrayList<>();
        for(int p = 0 ; p < n; p++){
            Particle particle = ParticleUtils.createNewParticleUnsuperposed(particles, l, particle_r);
            particles.add(particle);
        }
        return particles;
    }

    public static List<Particle> createParticlesAlternative(int n, double particle_r, double l){
        List<Particle> particles = new ArrayList<>();

        while(particles.size() < n){
            for(int p = 0 ; p < n - particles.size(); p++){
                double xCandidate = generateRandom(0 + particle_r, l - particle_r);
                double yCandidate = generateRandom(0 + particle_r, l - particle_r);

                Particle candidate =  new Particle(p, new Coordinate(xCandidate, yCandidate), particle_r);
                particles.add(candidate);
            }
            Iterator<Particle> particleIterator = particles.iterator();
            while(particleIterator.hasNext()) {
                Particle particle = particleIterator.next();
                if(particleIsSuperposed(particle, particles)){
                    particleIterator.remove();
                }
            }
        }
        return particles;
    }

    public static List<Particle> createParticlesUsingCIM(int n, double particle_r, double l){
        List<Particle> particles = new ArrayList<>();

        CellIndexMethod.setPeriodicPlane(false);

        // El division_factor determina cuantas celdas terminas creando.
        // Se puede usar para reducir la cantidad de memoria. Por ejemplo, si pones d=2, con un l=1000 y particle_r=0.25,
        // terminas creando (1000/0.25)^2=^16000000 de celdas. Que es una banda. Por ende, si las hago un poco mas grande, reduzco
        // la cantidad de celdas que se crean.
        // Tener en cuenta que cuanto mas grande division_factor, peor es la performance.
        // Valores validos: {2,...}
        int division_factor = 4;

        CellIndexMethod.CellGrid grid = new CellIndexMethod.CellGrid((int) Math.floor((l / division_factor * particle_r)), l);
        
        for(int p = 0 ; p < n; p++){

            Particle particle = ParticleUtils.createNewParticleUsingCIM(grid, p, l, particle_r);
            particles.add(particle);

            grid.addParticleToCell(particle);
        }
        return particles;
    }
    private static List<Particle> getSurroundingParticles(CellIndexMethod.CellGrid grid, int i,  int j){
        List<Particle> particles = new ArrayList<>();
        for(int p=i-1; p<=i+1; p++){
            for(int q=j-1; q<=j+1; q++){
                if(grid.validCell(p,q)){
                    CellIndexMethod.Cell cell = grid.getCell(p, q);
                    particles.addAll(cell.getParticles());
                }
            }
        }
        return particles;
    }

    public static Particle createNewParticleUsingCIM(CellIndexMethod.CellGrid grid, int id, double l, double particle_r){
        while(true) {
            double xCandidate = generateRandom(0 + particle_r, l - particle_r);
            double yCandidate = generateRandom(0 + particle_r, l - particle_r);

            Particle candidate =  new Particle(id, new Coordinate(xCandidate, yCandidate), particle_r);

            Coordinate coord = grid.getCellNumber(candidate);
            int i = (int) coord.getY();
            int j = (int) coord.getX();

            List<Particle> particles = new ArrayList<>(getSurroundingParticles(grid, i, j));

            boolean valid = true;

            for(Particle particle : particles){
                if(particle.isSuperposed(xCandidate, yCandidate, particle_r)){
                    valid = false;
                    break;
                }
            }

            if( valid ){
                return candidate;
            }
        }
    }
    
    public static Particle createNewParticleUnsuperposed(List<Particle> particles, double l, double particle_r){
        while(true){
            double x_candidate = generateRandom(0 + particle_r, l - particle_r);
            double y_candidate = generateRandom(0 + particle_r, l - particle_r);

            boolean valid = true;

            for(Particle particle : particles){
                if(particle.isSuperposed(x_candidate, y_candidate, particle_r)){
                    valid = false;
                    break;
                }
            }

            if( valid ){
                return new Particle(particles.size() + 1, new Coordinate(x_candidate, y_candidate), particle_r);
            }
        }
    }

    private static double generateRandom(double lower, double upper){
        return lower + (upper - lower) * random.nextDouble();
    }

    public static List<Particle> getParticlesWithinRadius(Particle centerParticle, List<Particle> particles, double r) {
        return particles.stream().filter(otherParticle -> centerParticle.isInRadius(otherParticle, r) && !otherParticle.equals(centerParticle)).collect(Collectors.toList());
    }
}
