import java.util.Objects;

public class Particle implements Cloneable{
    private int id;
    private Coordinate pos;
    private double angle;

    public Particle(int id, Coordinate pos, double angle) {
        this.id = id;
        this.pos = pos;
        this.angle = angle;
    }

    public boolean isSuperposed(double x, double y){
        return Double.compare(pos.getX(), x) == 0 && Double.compare(pos.getY(), y) == 0;
    }

    public double calculateDistance(Particle particle){
        return Math.sqrt(Math.pow((this.pos.getX() - particle.getPos().getX()), 2) + (Math.pow((this.pos.getY() - particle.getPos().getY()), 2)));
    }

    public boolean isInRadius(Particle particle, double radius){
        return calculateDistance(particle) <= radius;
    }


    @Override
    public String toString() {
        return "Particle{" +
                "id=" + id +
                ", pos=" + pos +
                ", angle=" + angle +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle particle)) return false;
        return id == particle.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public Particle clone() {
        return new Particle(id, pos.clone(), angle);
    }
}
