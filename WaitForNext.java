import java.io.File;
import java.awt.Image;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Arrays;

// How to fix setup:
// LEFT video stream = LOWER delay (zoom)
// RIGHT video stream = HIGHER delay (qTox)
// Start recording in 60fps when both of them are black, and then switch the source to white.
// Repeat the switching multiple times.
// Check that the amount of files (frames) is equal to the [video_length * 60]

// To analyze frames in folder data/exampleData, run "LeftAndRightIndividually exampleData" (after compiling).
public class WaitForNext {
    public static void main(String[] args) {

        // --- SETTINGS ---
        String folderWithFramesName = args[0]; // This folder must be located inside the data folder, and should contains frames from video
        boolean print = true;                // Set to true to print info about EVERY delay.

        // --- ACTUAL CODE STARTS ---
        String path = "data/" + folderWithFramesName;
        File folder = new File(path);  // Set this to the folder with frame files.
        File[] listOfFiles = folder.listFiles();
        ArrayList<Integer> whiteDelays = new ArrayList<Integer>();
        ArrayList<Integer> blackDelays = new ArrayList<Integer>();

        // Pixel coordinates that we analyze (leftx, lefty) and (rightx, righty)
        int leftx = 20;
        int lefty = 30;
        int rightx = 120;
        int righty = 30;
        
        // Needed to check delay
        int frameThatLeftBecameWhite = 0;
        int frameThatLeftBecameBlack = 0;

        // Waiting flags (only one true at a time)
        boolean waitingForLeftWhite = true;
        boolean waitingForLeftBlack = false;
        boolean waitingForRightWhite = false;
        boolean waitingForRightBlack = false;

        // counters
        int totalWhiteFrameDelay = 0;
        int totalBlackFrameDelay = 0;
        int blackToWhiteCounter = 0;
        int whiteToBlackCounter = 0;

        // Lowest/highest delay
        int lowestWhiteDelay = 1000000;
        int highestWhiteDelay = 0;
        int lowestBlackDelay = 1000000;
        int highestBlackDelay = 0;

        // Lowest/highest delay frame numbers. One per each of the above.
        int frame_lowestWhiteDelay = 0;
        int frame_highestWhiteDelay = 0;
        int frame_lowestBlackDelay = 0;
        int frame_highestBlackDelay = 0;

        // Each if-statement in the loop only waits for ONE pixel to be a specific color.
        // When that happens, a new pixel and color is chosen to wait for.
        for (int frame = 1; frame < listOfFiles.length + 1; frame++) {
            //println("frame: " + frame);
    
            // Load frame and display on canvas (too laggy to actually see though)
            File file = listOfFiles[frame-1];
            BufferedImage img = null;
            try {
                img = ImageIO.read(file);
            } catch (Exception e) { }
        
            // Extract colors of the two pixels
            Color colorLeft = getPixelColor(img, leftx, lefty);
            Color colorRight = getPixelColor(img, rightx, righty);
        
            // Left turns white
            if (waitingForLeftWhite && isWhite(colorLeft)) {
                frameThatLeftBecameWhite = frame;
                
                // Update flags
                waitingForLeftWhite = false;		
                waitingForRightWhite = true;

                if (print) {
                    System.out.println("");
                    System.out.println("frameThatLeftBecameWhite: " + frameThatLeftBecameWhite);
                }
            }
            
            // Right turns white, after left has turned white
            else if (waitingForRightWhite && isWhite(colorRight)) {
                int whiteDelay = frame - frameThatLeftBecameWhite;

                // Update list, total delay and counter
                whiteDelays.add(whiteDelay);
                totalWhiteFrameDelay += whiteDelay;
                blackToWhiteCounter++;
                
                // Update lowest/highest delays
                if (whiteDelay < lowestWhiteDelay) {
                    lowestWhiteDelay = whiteDelay;
                    frame_lowestWhiteDelay = frame;
                }
                if (whiteDelay > highestWhiteDelay) {
                    highestWhiteDelay = whiteDelay;
                    frame_highestWhiteDelay = frame;
                }
                
                // Update flags
                waitingForRightWhite = false;
                waitingForLeftBlack = true;

                if (print) {
                    System.out.println("frameThatRightBecameWhite: " + frame);
                    System.out.println("whiteDelay: " + whiteDelay);
                }
            }
        
            // Left turns black, after right has turned white
            else if (waitingForLeftBlack && isBlack(colorLeft)) {
                frameThatLeftBecameBlack = frame;
                
                // Update flags
                waitingForLeftBlack = false;
                waitingForRightBlack = true;

                if (print) {
                    System.out.println("");
                    System.out.println("frameThatLeftBecameBlack: " + frameThatLeftBecameBlack);
                }
            }
            
            // Right turns black, after left has turned black
            else if (waitingForRightBlack && isBlack(colorRight)) {
                int blackDelay = frame - frameThatLeftBecameBlack;
                
                // Update list, total delay and counter
                blackDelays.add(blackDelay);
                totalBlackFrameDelay += blackDelay;
                whiteToBlackCounter++;

                // Update lowest/highest delays
                if (blackDelay < lowestBlackDelay) {
                    lowestBlackDelay = blackDelay;
                    frame_lowestBlackDelay = frame;
                }
                if (blackDelay > highestBlackDelay) {
                    highestBlackDelay = blackDelay;
                    frame_highestBlackDelay = frame;
                }

                // Update flags
                waitingForRightBlack = false;
                waitingForLeftWhite = true;

                if (print) {
                    System.out.println("frameThatRightBecameBlack: " + frame);
                    System.out.println("blackDelay: " + blackDelay);
                }
            }
        }

        // Add white and black delays to allDelays
        ArrayList<Integer> allDelays = new ArrayList<Integer>(whiteDelays);
        allDelays.addAll(blackDelays);
        
        double averageWhiteFrameDelay = (double) totalWhiteFrameDelay / blackToWhiteCounter;
        int averageWhiteMilliSecDelay = (int) (averageWhiteFrameDelay * 16.6666); // Remember to record in 60fps.
        int lowestWhiteMilliSecDelay = (int) (lowestWhiteDelay * 16.6666);
        int highestWhiteMilliSecDelay = (int) (highestWhiteDelay * 16.6666);

        double averageBlackFrameDelay = (double) totalBlackFrameDelay / whiteToBlackCounter;
        int averageBlackMilliSecDelay = (int) (averageBlackFrameDelay * 16.6666);
        int lowestBlackMilliSecDelay = (int) (lowestBlackDelay * 16.6666);
        int highestBlackMilliSecDelay = (int) (highestBlackDelay * 16.6666);

        // All delays: median
        double medianFrameDelay = (double) getMedian(allDelays);
        int medianMilliSecDelay = (int) (medianFrameDelay * 16.6666);


        System.out.println ("");
        System.out.println ("### ALL DELAYS SORTED ###");
        System.out.println ("");
        Integer[] allDelaysArray = new Integer[allDelays.size()];
        allDelaysArray = allDelays.toArray(allDelaysArray);
        Arrays.sort(allDelaysArray);
        for (int i = 0; i < allDelaysArray.length; i++) {
            int delayInFrames = allDelaysArray[i];
            int delayInMilliSeconds = (int) (delayInFrames * 16.6666);
            System.out.println(delayInMilliSeconds);
        }

        System.out.println ("");
        System.out.println ("### ALL DELAYS, ORIGINAL ORDER ###");
        System.out.println ("");
        for (int i = 0; i < allDelays.size(); i++) {
            int delayInMilliSeconds = (int) (allDelays.get(i) * 16.6666);
            System.out.println(delayInMilliSeconds);
        }

        
        /*
        System.out.println("");
        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("--- TOTAL: white delay ---");
        System.out.println("Total frame delay: " + totalWhiteFrameDelay);
        System.out.println("Flips from black to white detected: " + blackToWhiteCounter);
        System.out.println("Lowest white delay: " + lowestWhiteMilliSecDelay + "ms = " + lowestWhiteDelay + " frames, ends at frame: " + frame_lowestWhiteDelay);
        System.out.println("Highest white delay: " + highestWhiteMilliSecDelay + "ms = " + highestWhiteDelay + " frames, ends at frame: " + frame_highestWhiteDelay);

        System.out.println("");
        System.out.println("--- TOTAL: black delay ---");
        System.out.println("Total frame delay: " + totalBlackFrameDelay);
        System.out.println("Flips from black to white detected: " + whiteToBlackCounter);
        System.out.println("Lowest black delay: " + lowestBlackMilliSecDelay + "ms = " + lowestBlackDelay + " frames, ends at frame: " + frame_lowestBlackDelay);
        System.out.println("Highest black delay: " + highestBlackMilliSecDelay + "ms = " + highestBlackDelay + " frames, ends at frame: " + frame_highestBlackDelay);

        System.out.println("");
        System.out.println("--- AVERAGE: white delay ---");
        System.out.println("Average frame delay: " + averageWhiteFrameDelay);
        System.out.println("Average ms delay: " + averageWhiteMilliSecDelay);
        System.out.println(pctWithinPctRangeOfAverage(whiteDelays, 0.75, 1.25) + "% of delays were within 25% of average.");
        System.out.println(pctWithinPctRangeOfAverage(whiteDelays, 0.5, 1.5) + "% of delays were within 50% of average.");
        System.out.println(pctWithinPctRangeOfAverage(whiteDelays, 0.2, 1.8) + "% of delays were within 80% of average.");

        System.out.println("");
        System.out.println("--- AVERAGE: black delay ---");
        System.out.println("Average frame delay: " + averageBlackFrameDelay);
        System.out.println("Average ms delay: " + averageBlackMilliSecDelay);
        System.out.println(pctWithinPctRangeOfAverage(blackDelays, 0.75, 1.25) + "% of delays were within 25% of average.");
        System.out.println(pctWithinPctRangeOfAverage(blackDelays, 0.5, 1.5) + "% of delays were within 50% of average.");
        System.out.println(pctWithinPctRangeOfAverage(blackDelays, 0.2, 1.8) + "% of delays were within 80% of average.");
        */
        
        System.out.println("--- MEDIAN: all delays ---");
        System.out.println(medianMilliSecDelay + " ms");
    }

    // Returns the percentage amount (an int between 0 and 100) of the list values that are within 
    // a certain percentage of the list average.
    // Example: pctWithinPctRangeOfAverage(list, 0.5, 1.5) gives percentage amount of 
    // values in list that are between 50% and 150% of the average value.
    static int pctWithinPctRangeOfAverage(ArrayList<Integer> list, double lower, double upper) {
        int countWithinRange = 0;
        double average = getAverage(list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) >= lower * average && list.get(i) <= upper * average) {
                countWithinRange++;
            }
        }
        double pctWithinRange = (double) countWithinRange / (double) list.size();
        return (int) (((double) 100) * pctWithinRange);
    }

    // Could speed things up by only checking red (less reliable, but probably fine).
    static boolean isBlack(Color c) {
        return c.getRed() < 40 && c.getGreen() < 40 && c.getBlue() < 40;
    }
    
    static boolean isWhite(Color c) {
        return c.getRed() > 230 && c.getGreen() > 230 && c.getBlue() > 230;
    }

    static double getAverage(ArrayList<Integer> list) {
        return (double) getSum(list) / list.size();
    }  

    static int getSum(ArrayList<Integer> list) {
        int sum = 0; 
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }    

    // Part of this function was made by stackoverflow user Mike B.
    // Function copied from https://stackoverflow.com/questions/11955728/how-to-calculate-the-median-of-an-array/28822243
    static double getMedian(ArrayList<Integer> list) {
        Integer[] numArray = new Integer[list.size()];
        numArray = list.toArray(numArray);
        Arrays.sort(numArray);

        double median;
        if (numArray.length % 2 == 0)
            median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
        else
            median = (double) numArray[numArray.length/2];
        return median;
    }

    // Basic function copied from stackoverflow: 
    // https://stackoverflow.com/questions/47500296/how-to-get-the-color-of-a-specific-pixel-of-an-image-java
    private static Color getPixelColor(Image image, int x, int y) {
        if (image instanceof BufferedImage) {
            return new Color(((BufferedImage) image).getRGB(x, y));
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        int[] pixels = new int[width * height];
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
        try {
            grabber.grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int c = pixels[x * width + y];
        int  red = (c & 0x00ff0000) >> 16;
        int  green = (c & 0x0000ff00) >> 8;
        int  blue = c & 0x000000ff;
        return new Color(red, green, blue);
    }
}