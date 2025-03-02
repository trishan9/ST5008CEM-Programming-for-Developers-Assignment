package Q6A;

/*
You are given a class NumberPrinter with three methods: printZero, printEven, and printOdd. 
These methods are designed to print the numbers 0, even numbers, and odd numbers, respectively.

Task:
Create a ThreadController class that coordinates three threads:
- ZeroThread: Calls printZero to print 0s.
- EvenThread: Calls printEven to print even numbers.
- OddThread: Calls printOdd to print odd numbers.

These threads should work together to print the sequence "0102030405..." up to a specified number n.
The output should be interleaved, ensuring that the numbers are printed in the correct order.

Example:
If n = 5, the output should be "0102030405".

Constraints:
- The threads should be synchronized to prevent race conditions and ensure correct output.
- The NumberPrinter class is already provided and cannot be modified.
*/

class NumberPrinter {
  private int n;
  private int current = 1;
  private boolean zeroTurn = true; // Indicates whether it is the turn to print zero

  public NumberPrinter(int n) {
    this.n = n;
  }

  // Method to print the number zero 'n' times
  public synchronized void printZero() throws InterruptedException {
    for (int i = 1; i <= n; i++) {
      while (!zeroTurn) { // Wait if it's not zero's turn
        wait();
      }
      System.out.print("0"); // Print a zero
      zeroTurn = false; // Switch to the next printing phase (either even or odd number)
      notifyAll(); // Wake up the other threads waiting
    }
  }

  // Method to print even numbers in sequence
  public synchronized void printEven() throws InterruptedException {
    while (current <= n) {
      while (zeroTurn || (current % 2 != 0)) { // Wait if it's zero's turn or the current number is odd
        if (current > n)
          break; // Ensure we do not exceed the limit 'n'
        wait();
      }
      if (current > n)
        break; // Double-check the condition after waking up from wait
      System.out.print(current); // Print the current even number
      current++; // Increment to the next number
      zeroTurn = true; // Switch turn back to zero
      notifyAll(); // Notify other threads
    }
  }

  // Method to print odd numbers in sequence
  public synchronized void printOdd() throws InterruptedException {
    while (current <= n) {
      while (zeroTurn || (current % 2 == 0)) { // Wait if it's zero's turn or the current number is even
        if (current > n)
          break; // Ensure we do not exceed the limit 'n'
        wait();
      }
      if (current > n)
        break; // Double-check the condition after waking up from wait
      System.out.print(current); // Print the current odd number
      current++; // Increment to the next number
      zeroTurn = true; // Switch turn back to zero
      notifyAll(); // Notify other threads
    }
  }
}

public class ThreadController {
  public static void main(String[] args) {
    int n = 5; // Define the upper limit for printing numbers, e.g., "0102030405"
    NumberPrinter np = new NumberPrinter(n);

    // Thread responsible for printing zeros
    Thread zeroThread = new Thread(() -> {
      try {
        np.printZero();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Handle interruption
      }
    });

    // Thread responsible for printing even numbers
    Thread evenThread = new Thread(() -> {
      try {
        np.printEven();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Handle interruption
      }
    });

    // Thread responsible for printing odd numbers
    Thread oddThread = new Thread(() -> {
      try {
        np.printOdd();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Handle interruption
      }
    });

    // Start all threads concurrently
    zeroThread.start();
    evenThread.start();
    oddThread.start();

    // Wait for all threads to finish execution
    try {
      zeroThread.join();
      evenThread.join();
      oddThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace(); // Handle exceptions when waiting for threads to join
    }
  }
}
