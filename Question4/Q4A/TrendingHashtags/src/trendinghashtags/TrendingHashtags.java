/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package trendinghashtags;

/**
 *
 * @author trishan9
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrendingHashtags {
    public static void main(String[] args) {
        // Database connection details
        String url = "jdbc:mysql://localhost:3306/TrendingHashtags";
        String user = "root";
        String password = "password";

        // SQL query that finds the top 3 trending hashtags in February 2024.
        // It uses a recursive CTE to split the tweet into words and then filters for
        // words starting with '#'.
        String sqlQuery = "WITH RECURSIVE\n"
                + "  FebruaryTweets AS (\n"
                + "    SELECT *\n"
                + "    FROM Tweets\n"
                + "    WHERE YEAR(tweet_date) = 2024 AND MONTH(tweet_date) = 2\n"
                + "  ),\n"
                + "  HashtagToTweet AS (\n"
                + "    SELECT\n"
                + "      REGEXP_SUBSTR(tweet, '#[^\\\\s]+') AS hashtag,\n"
                + "      REGEXP_REPLACE(tweet, '#[^\\\\s]+', '', 1, 1) AS tweet\n"
                + "    FROM FebruaryTweets\n"
                + "    UNION ALL\n"
                + "    SELECT\n"
                + "      REGEXP_SUBSTR(tweet, '#[^\\\\s]+') AS hashtag,\n"
                + "      REGEXP_REPLACE(tweet, '#[^\\\\s]+', '', 1, 1) AS tweet\n"
                + "    FROM HashtagToTweet\n"
                + "    WHERE POSITION('#' IN tweet) > 0\n"
                + "  )\n"
                + "SELECT hashtag, COUNT(*) AS count\n"
                + "FROM HashtagToTweet\n"
                + "GROUP BY hashtag\n"
                + "ORDER BY count DESC, hashtag DESC\n"
                + "LIMIT 3;";

        // Try-with block to ensure that resources are properly closed after use
        try (
            Connection conn = DriverManager.getConnection(url, user, password); 
            PreparedStatement stmt = conn.prepareStatement(sqlQuery); 
            ResultSet rs = stmt.executeQuery()
        ) {

            // Print header for the output
            System.out.println("+-----------+-------+");
            System.out.println("| hashtag   | count |");
            System.out.println("+-----------+-------+");

            // Iterate through the result set and print each hashtag and its count
            while (rs.next()) {
                // Retrieve the hashtag string from the current row
                String hashtag = rs.getString("hashtag");
                // Retrieve the count of occurrences from the current row
                int count = rs.getInt("count");

                // Print the hashtag and its count.
                System.out.println("| " + hashtag + " | " + count + "     |");
            }
            // Print table footer
            System.out.println("+-----------+-------+");
        } catch (SQLException e) {
            // If a SQL error occurs, print the error so that debugging is easy
            e.printStackTrace();
        }
    }
}
