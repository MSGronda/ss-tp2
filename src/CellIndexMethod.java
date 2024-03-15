import javax.naming.directory.InvalidAttributesException;
import java.util.*;
import java.util.stream.Collectors;

public class CellIndexMethod {

    // Define si hay condiciones de contornos periodicos.
    private static boolean periodicPlane = false;

    public static void setPeriodicPlane(boolean periodicPlane) {
        CellIndexMethod.periodicPlane = periodicPlane;
    }

    public static boolean periodicPlane() {
        return periodicPlane;
    }

    public static Map<Particle, Set<Particle>> computeNeighbors(List<Particle> particles, double planeWidth, int gridWidth, double r){

        CellGrid grid = new CellGrid(particles, gridWidth, planeWidth);

        Map<Particle, Set<Particle>> neighborMap = new HashMap<>();

        for(Particle particle : particles) {
            Coordinate cellCoord = grid.getCellNumber(particle);
            int i = (int) cellCoord.getY();
            int j = (int) cellCoord.getX();

            // Este set contiene todos los neighbors validos.
            // Asumimos que los que estan en el mismo cell caen por dentro (dado que m = l / r)
            Set<Particle> neighbors = grid.getCell(i, j).getParticles().stream().filter(p -> !p.equals(particle) && p.isInRadius(particle, r)).collect(Collectors.toSet());

            // Arriba
            if(grid.validCell(i-1, j)) {
                neighbors.addAll(grid.getParticlesWithinRadius(particle,i-1, j, r));
            }
            // Diagonal arriba a la derecha
            if(grid.validCell(i-1, j+1)) {
                neighbors.addAll(grid.getParticlesWithinRadius(particle, i-1,j+1, r));
            }
            // A la derecha
            if(grid.validCell(i, j+1)) {
                neighbors.addAll(grid.getParticlesWithinRadius(particle, i,j+1, r));
            }
            // Diagonal abajo a la derecha
            if(grid.validCell(i+1, j+1)) {
                neighbors.addAll(grid.getParticlesWithinRadius(particle, i+1,j+1, r));
            }

            if(!neighbors.isEmpty()){
                neighborMap.putIfAbsent(particle, new HashSet<>());
                neighborMap.get(particle).addAll(neighbors);

                // Debemos agregar entries tmb para los neighbors
                neighbors.forEach(other -> {
                    neighborMap.putIfAbsent(other, new HashSet<>());
                    neighborMap.get(other).add(particle);
                });
            }
        }
        return neighborMap;
    }

    public static class CellGrid {

        private List<List<Cell>> cells;
        private int gridWidth;
        private double planeWidth;
        private double cellSize;

        public CellGrid(List<Particle> particles, int gridWidth, double planeWidth) {
            this.gridWidth = gridWidth;
            this.planeWidth = planeWidth;
            this.cellSize = planeWidth/gridWidth;

            cells = createCells(gridWidth);

            for(Particle particle : particles) {
                Coordinate cellCoord = getCellNumber(particle);
                int i = (int) cellCoord.getY();
                int j = (int) cellCoord.getX();

                cells.get(i).get(j).getParticles().add(particle);
            }
        }

        public CellGrid(int gridWidth, double planeWidth) {
            this.gridWidth = gridWidth;
            this.planeWidth = planeWidth;
            this.cellSize = planeWidth/gridWidth;

            cells = createCells(gridWidth);
        }

        private static List<List<Cell>> createCells(int m){
            List<List<Cell>> resp = new ArrayList<>();
            for( int i = 0; i < m; i++){
                List<Cell> row = new ArrayList<>();
                for( int j = 0; j < m; j++){
                    row.add(new Cell(new ArrayList<>(), i, j));
                }
                resp.add(row);
            }
            return resp;
        }
        public boolean validCell(int i, int j){
            if(periodicPlane){
                return true;
            }
            return i>=0 && j>=0 && i<gridWidth && j<gridWidth;
        }

        public Coordinate getCellNumber(Particle particle){
            return new Coordinate(Math.floor(particle.getPos().getX() / cellSize), Math.floor(particle.getPos().getY() / cellSize));
        }

        public void addParticleToCell(Particle particle){
            Coordinate coord = getCellNumber(particle);
            int i = (int) coord.getY();
            int j = (int) coord.getX();

            Cell cell = getCell(i,j);

            cell.particles.add(particle);
        }

        public Cell getCell(int i, int j){
            int i_pos, j_pos;
            if(periodicPlane){
                i_pos = (i % gridWidth + gridWidth) % gridWidth;
                j_pos = (j % gridWidth + gridWidth) % gridWidth;
            }
            else{
                i_pos = i;
                j_pos = j;
            }
            return cells.get(i_pos).get(j_pos);
        }

        public Coordinate getDisplacement(int i, int j){
            if(!periodicPlane || i>=0 && j>=0 && i<gridWidth && j<gridWidth){
                return new Coordinate(0,0);
            }
            if(i<0 && j>=0 && j<gridWidth){
                return new Coordinate(0, planeWidth);
            }
            if(i<0 && j>=gridWidth){
                return new Coordinate(-planeWidth, planeWidth);
            }
            if(i>=0 && i<gridWidth && j>=gridWidth){
                return new Coordinate(-planeWidth, 0);
            }
            if(i>=gridWidth && j>=0 && j<gridWidth){
                return new Coordinate(-planeWidth, planeWidth/gridWidth);
            }
            if(i>=gridWidth && j>=gridWidth){
                return new Coordinate(-planeWidth,  planeWidth/gridWidth);
            }
            throw new RuntimeException("Hay algo mal con el displacement ");
        }

        public List<Particle> getParticlesWithinRadius(Particle centerParticle, int i, int j, double r) {

            List<Particle> particles = getCell(i, j).particles;
            Coordinate displacement = getDisplacement(i, j);

            return particles.stream().filter(otherParticle -> isInRadius(centerParticle, otherParticle, displacement, r) && !otherParticle.equals(centerParticle)).collect(Collectors.toList());
        }

        public static boolean isInRadius(Particle centerParticle, Particle otherParticle, Coordinate displacement, double r){
            return Math.sqrt(
                    Math.pow((centerParticle.getPos().getX() + displacement.getX() - otherParticle.getPos().getX()), 2)+ (Math.pow((centerParticle.getPos().getY() + displacement.getY() - otherParticle.getPos().getY()), 2)
                    )) - centerParticle.getRadius() - otherParticle.getRadius() <= r;
        }

    }

    public static class Cell {
        private List<Particle> particles;
        private int i;
        private int j;

        public Cell(List<Particle> particles, int i, int j) {
            this.particles = particles;
            this.i = i;
            this.j = j;
        }

        public List<Particle> getParticles() {
            return particles;
        }

        public void setParticles(List<Particle> particles) {
            this.particles = particles;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }
    }

}
