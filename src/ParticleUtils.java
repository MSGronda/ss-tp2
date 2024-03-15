import java.util.*;
import java.util.stream.Collectors;

public class ParticleUtils {

    private static final Random random = new Random(2);

    public static List<Particle> createParticles(int n, double l) {
        List<Particle> particles = new ArrayList<>();
        for(int p = 0 ; p < n; p++){

            Particle particle = new Particle(
                    p,
                    new Coordinate(generateRandom(0 , l),generateRandom(0 , l)),
                    generateRandom(0, 2 * Math.PI)
            );

            particles.add(particle);
        }
        return particles;
    }

    private static double generateRandom(double lower, double upper){
        return lower + (upper - lower) * random.nextDouble();
    }

}
