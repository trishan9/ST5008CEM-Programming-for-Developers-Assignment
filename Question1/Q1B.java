// You have two sorted arrays of investment returns, returns1 and returns2, and
// a target number k. You
// want to find the kth lowest combined return that can be achieved by selecting
// one investment from each
// array.

// Rules:
// - The arrays are sorted in ascending order.
// - You can access any element in the arrays.

// Goal: Determine the kth lowest combined return that can be achieved.
// Input:
// - returns1: The first sorted array of investment returns.
// - returns2: The second sorted array of investment returns.
// - k: The target index of the lowest combined return.
// Output:
// - The kth lowest combined return that can be achieved.

// Example 1:
// Input: returns1= [2,5], returns2= [3,4], k = 2
// Output: 8
// Explanation: The 2 smallest investments are are:
// - returns1 [0] * returns2 [0] = 2 * 3 = 6
// - returns1 [0] * returns2 [1] = 2 * 4 = 8
// The 2nd smallest investment is 8.

// Example 2:
// Input: returns1= [-4,-2,0,3], returns2= [2,4], k = 6
// Output: 0
// Explanation: The 6 smallest products are:
// - returns1 [0] * returns2 [1] = (-4) * 4 = -16
// - returns1 [0] * returns2 [0] = (-4) * 2 = -8
// - returns1 [1] * returns2 [1] = (-2) * 4 = -8
// - returns1 [1] * returns2 [0] = (-2) * 2 = -4
// - returns1 [2] * returns2 [0] = 0 * 2 = 0
// - returns1 [2] * returns2 [1] = 0 * 4 = 0
// The 6th smallest investment is 0.

import java.util.PriorityQueue;

public class Q1B {
  // Function to determine the k-th lowest combined return
  public static int findKthLowest(int[] returns1, int[] returns2, int k) {
    // Initialize a min-heap to store the combined returns along with their indices
    PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));

    // product combinations of the first element in returns1 with the elements in
    // returns2
    for (int j = 0; j < returns2.length && j < k; j++) {
      int prod = returns1[0] * returns2[j]; // Calculate the product of returns
      minHeap.offer(new int[] { prod, 0, j }); // Insert the product and indices into the heap
    }

    // Extract the smallest element from the heap k-1 times
    int count = 0; // Variable to track how many elements have been removed from the heap
    while (count < k - 1) { // Continue until k-1 elements have been extracted
      int[] current = minHeap.poll(); // Remove the smallest combined return from the heap
      int i = current[1]; // Index in returns1
      int j = current[2]; // Index in returns2

      // If there are more elements in returns1 to pair with the current element in
      // returns2
      if (i + 1 < returns1.length) {
        int newProd = returns1[i + 1] * returns2[j]; // Compute the new product
        minHeap.offer(new int[] { newProd, i + 1, j }); // Add the new product to the heap
      }

      count++; // Increment the extraction counter
    }

    // The k-th smallest combined return is now at the root of the heap
    return minHeap.poll()[0];
  }

  public static void main(String[] args) {
    // Example 1
    int[] returns1 = { 2, 5 };
    int[] returns2 = { 3, 4 };
    int k = 2;
    System.out.println("Kth Lowest Return: " + findKthLowest(returns1, returns2, k)); // Expected output: 8

    // Example 2
    returns1 = new int[] { -4, -2, 0, 3 };
    returns2 = new int[] { 2, 4 };
    k = 6;
    System.out.println("Kth Lowest Return: " + findKthLowest(returns1, returns2, k)); // Expected output: 0
  }
}
