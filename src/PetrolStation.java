import java.util.Random;
import java.util.concurrent.*;

public class PetrolStation {
    private volatile int amount;
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(3);
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Random random = new Random();

    public PetrolStation(int amount) {
        this.amount = amount;
    }

    public void doRefuel(int fuel) {
        try {
            queue.put(() -> {
                try {
                    int waitTime = 3000 + random.nextInt(7000);
                    System.out.println("Refueling " + fuel + " liters, will take " + waitTime / 1000 + " seconds.");
                    Thread.sleep(waitTime);

                    synchronized (this) {
                        if (amount >= fuel) {
                            amount -= fuel;
                            System.out.println("Car left with " + fuel + " liters. Fuel remaining: " + amount);
                        } else {
                            System.out.println("Not enough fuel. Request denied.");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.execute(queue.take());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void shutDown(){
        executor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        PetrolStation station = new PetrolStation(100);

        for (int i = 0; i < 5; i++) {
            int fuelRequest = 10 + new Random().nextInt(20);
            new Thread(() -> station.doRefuel(fuelRequest)).start();
        }

        Thread.sleep(15000);

        station.shutDown();
    }
}
