import java.util.Objects;

public class Particle {
    private int id;
    private Coordinate pos;
    private double radius;

    public Particle(int id, Coordinate pos, double radius) {
        this.id = id;
        this.pos = pos;
        this.radius = radius;
    }

    public boolean isSuperposed(double x, double y, double radius){
        return Math.sqrt(Math.pow((this.pos.getX() - x), 2) + (Math.pow((this.pos.getY() - y), 2))) - this.getRadius() - radius < 0;
    }

    public double calculateDistance(Particle particle){
        return Math.sqrt(Math.pow((this.pos.getX() - particle.getPos().getX()), 2) + (Math.pow((this.pos.getY() - particle.getPos().getY()), 2))) - this.radius - particle.getRadius();
    }
    public double calculateDistance(Particle particle, boolean considerWalls, double x_displacement, double y_displacement){
        if(considerWalls)
            return Math.sqrt(Math.pow((this.pos.getX() - particle.getPos().getX()), 2) + (Math.pow((this.pos.getY() - particle.getPos().getY()), 2))) - this.radius - particle.getRadius();
        return Math.sqrt(Math.pow((this.pos.getX() + x_displacement - particle.getPos().getX()), 2) + (Math.pow((this.pos.getY() + y_displacement - particle.getPos().getY()), 2))) - this.radius - particle.getRadius();

    }
    public boolean isInRadius(Particle particle, double radius){
        return calculateDistance(particle) <= radius;
    }

    public boolean isInRadius(Particle particle, double radius, boolean considerWalls, double x_displacement, double y_displacement){
        return calculateDistance(particle, considerWalls, x_displacement, y_displacement) + this.radius + particle.radius <= radius;
    }

    @Override
    public String toString() {
        return "Particle{" +
                "id=" + id +
                ", pos=" + pos +
                ", radius=" + radius +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle particle)) return false;
        return id == particle.id && Double.compare(particle.radius, radius) == 0 && Objects.equals(pos, particle.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pos, radius);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Coordinate getPos() {
        return pos;
    }

    public void setPos(Coordinate pos) {
        this.pos = pos;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
