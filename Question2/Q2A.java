/*
You have a team of n employees, and each employee is assigned a performance rating given in the
integer array ratings. You want to assign rewards to these employees based on the following rules:
Every employee must receive at least one reward.
Employees with a higher rating must receive more rewards than their adjacent colleagues.

Goal:
Determine the minimum number of rewards you need to distribute to the employees.
Input:
ratings: The array of employee performance ratings.
Output:
The minimum number of rewards needed to distribute.

Example 1:
Input: ratings = [1, 0, 2]
Output: 5
Explanation: You can allocate to the first, second and third employee with 2, 1, 2 rewards respectively.

Example 2:
Input: ratings = [1, 2, 2]
Output: 4

Explanation: You can allocate to the first, second and third employee with 1, 2, 1 rewards respectively.
The third employee gets 1 rewards because it satisfies the above two conditions.
*/

public class Q2A {
  // Method to calculate the total rewards based on employee ratings.
  public static int calculateRewards(int[] ratings) {
    int n = ratings.length; // Number of employees

    // Initialize an array to store rewards for each employee.
    // Each employee must receive at least one reward, so we initialize the array
    // with 1 for every employee.
    int[] rewards = new int[n];
    for (int i = 0; i < n; i++) {
      rewards[i] = 1; // Every employee gets at least 1 reward by default
    }

    // First pass (Left to Right)
    // Traverse the ratings array from left to right, updating rewards for employees
    // whose ratings are higher than the previous employee.
    for (int i = 1; i < n; i++) {
      if (ratings[i] > ratings[i - 1]) {
        // If the current rating is higher than the previous one, increase the reward
        // for this employee.
        rewards[i] = rewards[i - 1] + 1;
      }
    }

    // Second pass (Right to Left)
    // Traverse the ratings array from right to left, ensuring that employees with
    // higher ratings than the next one receive the correct reward.
    for (int i = n - 2; i >= 0; i--) {
      if (ratings[i] > ratings[i + 1]) {
        // If the current rating is higher than the next one, adjust the reward.
        // Take the maximum reward needed to satisfy both the left and right conditions.
        rewards[i] = Math.max(rewards[i], rewards[i + 1] + 1);
      }
    }

    // Calculate the total rewards.
    // Sum up the rewards array to get the total number of rewards needed.
    int totalRewards = 0;
    for (int reward : rewards) {
      totalRewards += reward; // Add each employee's reward to the total
    }

    return totalRewards; // Return the total reward count
  }

  // Main method for testing the function with different rating arrays.
  public static void main(String[] args) {
    // Example 1: Ratings where the employee ratings are [1, 0, 2]
    // The expected output is 5 because the reward distribution would be [2, 1, 2],
    // with a total of 5.
    int[] ratings1 = { 1, 0, 2 };
    System.out.println("Minimum Rewards: " + calculateRewards(ratings1)); // Expected output: 5

    // Example 2: Ratings where the employee ratings are [1, 2, 2]
    // The expected output is 4 because the reward distribution would be [1, 2, 1],
    // with a total of 4.
    int[] ratings2 = { 1, 2, 2 };
    System.out.println("Minimum Rewards: " + calculateRewards(ratings2)); // Expected output: 4

    // Additional Example: Ratings where the employee ratings are [3, 2, 1, 4, 5]
    // The expected output is 11 because the reward distribution would be [3, 2, 1,
    // 2, 3], with a total of 11.
    int[] ratings3 = { 3, 2, 1, 4, 5 };
    System.out.println("Minimum Rewards: " + calculateRewards(ratings3)); // Expected output: 11
  }
}
