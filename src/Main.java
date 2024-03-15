public class Main {
    public static void main(String[] args) {
        OffLatticeSimulation simulation = new OffLatticeSimulation(300,5, 1, 0.03, 0.1);

        for(int i=0; i<1000; i++){
//            System.out.println("t=" + (i + 1));
            simulation.simulate();
            System.out.println(simulation.calculatePolarization());
        }
    }
}
