import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        OffLatticeSimulation simulation = new OffLatticeSimulation(100,5, 0.25, 0.1, 0.1);
        try(FileWriter writer = new FileWriter("./python/output-files/particle-movement.txt")) {
            for(int i=0; i<100; i++){
                writer.write(i + ",\n");
                List<Particle> particles = simulation.simulate();

                for( Particle p : particles){
                    writer.write(p.getPos().getX() + "," + p.getPos().getY() + "," + p.getAngle() + "," + simulation.getV() + "\n");
                }
                writer.write("\n");

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
