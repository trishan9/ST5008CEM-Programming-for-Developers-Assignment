/*
You have two points in a 2D plane, represented by the arrays x_coords and y_coords. The goal is to find
the lexicographically pair i.e. (i, j) of points (one from each array) that are closest to each other.

Goal: Determine the lexicographically pair of points with the smallest distance and smallest distance calculated using
| x_coords [i] - x_coords [j]| + | y_coords [i] - y_coords [j]|

Note that
|x| denotes the absolute value of x.
A pair of indices (i1, j1) is lexicographically smaller than (i2, j2) if i1 < i2 or i1 == i2 and j1 < j2.

Input:
x_coords: The array of x-coordinates of the points.
y_coords: The array of y-coordinates of the points.
Output: The indices of the closest pair of points.

Input: x_coords = [1, 2, 3, 2, 4], y_coords = [2, 3, 1, 2, 3]
Output: [0, 3]
Explanation: Consider index 0 and index 3. The value of | x_coords [i]- x_coords [j]| + | y_coords [i]-
y_coords [j]| is 1, which is the smallest value we can achieve.
*/

public class Q2B {
  // Method to find the indices of the closest pair of points based on Manhattan
  // distance.
  public static int[] closestPair(int[] x_coords, int[] y_coords) {
    // Initialize the minimum distance to a very large value,
    // and create an array to store the indices of the closest pair.
    int minDistance = Integer.MAX_VALUE;
    int[] result = new int[2]; // Array to store the indices of the closest pair

    // Iterate over all pairs of points to calculate their Manhattan distance.
    for (int i = 0; i < x_coords.length; i++) {
      for (int j = i + 1; j < x_coords.length; j++) {
        // Calculate the Manhattan distance between points (i, j)
        int distance = Math.abs(x_coords[i] - x_coords[j]) + Math.abs(y_coords[i] - y_coords[j]);

        // If a smaller distance is found, update the minimum distance and the result
        // array.
        if (distance < minDistance) {
          minDistance = distance;
          result[0] = i; // Store the index of the first point
          result[1] = j; // Store the index of the second point
        }
        // If the current distance is equal to the minimum,
        // check lexicographical order (compare indices to find the smallest pair).
        else if (distance == minDistance) {
          // If i < i1 or (i == i1 and j < j1), update the result with the new pair.
          if (i < result[0] || (i == result[0] && j < result[1])) {
            result[0] = i;
            result[1] = j;
          }
        }
      }
    }

    // return the indices of the closest pair of points.
    return result;
  }

  public static void main(String[] args) {
    // example Test Case 1: Given x and y coordinates of points
    int[] x_coords = { 1, 2, 3, 2, 4 };
    int[] y_coords = { 2, 3, 1, 2, 3 };

    // Find the closest pair of points
    int[] result = closestPair(x_coords, y_coords);

    // Print the indices of the closest pair
    System.out.println("Closest pair indices: [" + result[0] + ", " + result[1] + "]");
  }
}
