package Q6B;

/*
Problem:
You need to crawl a large number of web pages to gather data or index content. Crawling each page sequentially can be time-consuming and inefficient.

Goal: Create a web crawler application that can crawl multiple web pages concurrently using multithreading to
improve performance.

Tasks:
Design the application: 
Create a data structure to store the URLs to be crawled.
Implement a mechanism to fetch web pages asynchronously.
Design a data storage mechanism to save the crawled data.

Create a thread pool: 
Use the ExecutorService class to create a thread pool for managing multiple threads.

Submit tasks: 
For each URL to be crawled, create a task (e.g., a Runnable or Callable object) that fetches the web page and processes the content.
Submit these tasks to the thread pool for execution.

Handle responses: 
Process the fetched web pages, extracting relevant data or indexing the content.
Handle errors or exceptions that may occur during the crawling process.

Manage the crawling queue: 
Implement a mechanism to manage the queue of URLs to be crawled, such as a priority queue or a breadth-first search algorithm.

By completing these tasks, you will create a multithreaded web crawler that can efficiently crawl large
numbers of web page
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Class to store URL and its crawling depth
class UrlEntry implements Comparable<UrlEntry> {
  String url; // url to be crawled
  int depth; // depth of url in the crawl (level in BFS)

  UrlEntry(String url, int depth) {
    this.url = url;
    this.depth = depth;
  }

  // Compare method for sorting URLs by depth
  @Override
  public int compareTo(UrlEntry other) {
    return Integer.compare(this.depth, other.depth); // Prioritize URLs with lower depth (breadth-first)
  }
}

public class MultiThreadedWebCrawler {
  private static final int MAX_THREADS = 5; // Maximum number of threads in the thread pool
  private static final int MAX_DEPTH = 2; // Maximum depth for crawling (limit to avoid infinite crawling)

  // Thread pool to manage concurrent tasks
  private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
  private static final Queue<UrlEntry> urlQueue = new PriorityQueue<>(); // Queue to manage URLs for crawling

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter URLs to crawl (type 'done' to finish):");

    // Continuously take URLs from the user until "done" is entered
    while (true) {
      System.out.print("Enter URL: ");
      String input = scanner.nextLine(); // Read user input
      if (input.equalsIgnoreCase("done")) { // Stop if the user enters "done"
        break;
      }
      // Validate if the URL starts with 'http://' or 'https://'
      if (isValidUrl(input)) {
        urlQueue.add(new UrlEntry(input, 0)); // Add valid URLs to the queue, starting at depth 0
      } else {
        System.out.println("Invalid URL. Please enter a valid URL starting with 'http' or 'https'.");
      }
    }
    scanner.close(); // close scanner after use

    startCrawling(); // start crawling process
    shutdownExecutor(); // close executor service after crawling finishes
  }

  // check if url is valid
  private static boolean isValidUrl(String url) {
    return url.startsWith("http://") || url.startsWith("https://");
  }

  // begin the crawling process by submitting tasks to the thread pool
  private static void startCrawling() {
    // keep crawling until there are no URLs left in the queue
    while (!urlQueue.isEmpty()) {
      UrlEntry entry = urlQueue.poll(); // Get the next URL to crawl
      if (entry.depth <= MAX_DEPTH) { // Only crawl if the depth is within the limit
        // Submit a crawling task for the URL to the executor service
        executorService.submit(() -> crawl(entry.url, entry.depth));
      }
    }
  }

  // Method to crawl a specific URL
  private static void crawl(String url, int depth) {
    try {
      System.out.println("Crawling: " + url + " [Depth: " + depth + "]");
      String content = fetchUrlContent(url); // Fetch the content of the URL
      processContent(url, content); // Process the content

      // If required, new URLs can be added dynamically here
    } catch (Exception e) {
      System.out.println("Error crawling " + url + ": " + e.getMessage()); // handles exceptionss
    }
  }

  // Method to fetch the content of a URL
  private static String fetchUrlContent(String urlString) throws Exception {
    // Convert the URL string into a URL object
    URI uri = new URI(urlString); // uri to url as direct url is deprecated
    URL url = uri.toURL();

    HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Open a connection to the URL
    connection.setRequestMethod("GET"); // Set the request method to GET

    // Read the response from the URL
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      StringBuilder content = new StringBuilder(); // StringBuilder to store the content of the page
      String line;
      // Read each line from the response and append it to the content
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      return content.toString(); // returns the crawled content
    }
  }

  // Method to process the crawled content
  private static void processContent(String url, String content) {
    System.out.println("Crawled: " + url + " [Content Length: " + content.length() + " chars]");
    // we can also add further content processing here, such as parsing or saving
    // the data, for now just printing content length
  }

  // properly shut down the executor service after crawling is done
  private static void shutdownExecutor() {
    executorService.shutdown(); // shutdown of the thread pool
    try {
      // wait up to 30 seconds for tasks to finish; if they don't, force shutdown, for
      // error handling
      if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
        executorService.shutdownNow(); // Force shutdown if tasks are not completed in time
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow(); // Handle interruptions and force shutdown
    }
  }
}
