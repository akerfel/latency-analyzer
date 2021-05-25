import java.io.File;
import java.awt.Image;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

// How to fix setup:
// LEFT video stream = LOWER delay (zoom)
// RIGHT video stream = HIGHER delay (qTox)
// Start recording in 60fps when both of them are black, and then switch the source to white.
// Repeat the switching multiple times.
// Check that the amount of files (frames) is equal to the [video_length * 60]
// This version can start on either black or white screen, it does not matter.

// A 16 minute video results in about 58.000 frame images. 
// If each frame image file is 4KB (low res), it will take about 28 seconds to process with prints turned on,
// and about 23 seconds with prints turned off.

// To analyze frames in folder data/exampleData, run "Main exampleData" (after compiling).
// To print info about white and black delays individually, run "Main exampleData print"
public class Main {
    public static void main(String[] args) {

        // --- SETTINGS, TO BE CHANGED BY USER ---
        boolean printSwapFrames = true; // If true, will print info about EVERY swap. Adds about 20% runtime
        boolean printDelaysSorted = true; 

        // First command line option
        String folderWithFramesName = args[0]; // This folder must be located inside the data folder, and should contains frames from video

        // Set by command line's optional second argument.
        boolean printBlackAndWhiteInfo = false; // If false (recommended), only prints info about ALL delays (not black/white delays)
        if (args.length == 2) {
            printBlackAndWhiteInfo = args[1].equals("print") || args[1].equals("p");
        }

        // --- ACTUAL CODE STARTS ---
        String path = "data/" + folderWithFramesName;
        File folder = new File(path);  // Set this to the folder with frame files.
        File[] listOfFiles = folder.listFiles();

        if (printSwapFrames) {
            System.out.println("Processing " + listOfFiles.length + " files...");
        }

        
        // Frame numbers where black switches to white and vice versa, both for left and right.
        // For example, blackToWhiteFramesLeft.get(11) = 4800 means that the eleventh swap from black to
        // white happens at frame 4800.
        ArrayList<Integer> blackToWhiteFramesLeft = new ArrayList<Integer>();
        ArrayList<Integer> blackToWhiteFramesRight = new ArrayList<Integer>();
        ArrayList<Integer> whiteToBlackFramesLeft = new ArrayList<Integer>();
        ArrayList<Integer> whiteToBlackFramesRight = new ArrayList<Integer>();

        // Pixel coordinates that we analyze (leftx, lefty) and (rightx, righty)
        int leftx = 20;
        int lefty = 30;
        int rightx = 120;
        int righty = 30;

        // Open first frame file and extract colors of specified pixels.
        File firstFile = listOfFiles[0];
        BufferedImage firstImg = null;
        try {
            firstImg = ImageIO.read(firstFile);
        } catch (Exception e) { }

        
        Color lastColorLeft = getPixelColor(firstImg, leftx, lefty);
        Color lastColorRight = getPixelColor(firstImg, rightx, righty);
        
        // Loop through all the frames, and save the specific frame numbers where
        // a switch from black to white or white to black happens.
        // (For both the left and right video).
        for (int frame = 2; frame < listOfFiles.length + 1; frame++) {
            //println("frame: " + frame);
    
            // Open current frame file and extract colors of specified pixels.
            File file = listOfFiles[frame - 1];
            BufferedImage img = null;
            try {
                img = ImageIO.read(file);
            } catch (Exception e) { }
            Color colorLeft = getPixelColor(img, leftx, lefty);
            Color colorRight = getPixelColor(img, rightx, righty);
        
            // Save the current frame is black switches to white or vice versa in left video.
            if (isBlack(lastColorLeft) && isWhite(colorLeft)) {
                blackToWhiteFramesLeft.add(frame);
                if (printSwapFrames) System.out.println("Left  became white # " + blackToWhiteFramesLeft.size() + " at frame: " + frame);
            }
            else if (isWhite(lastColorLeft) && isBlack(colorLeft)) {
                whiteToBlackFramesLeft.add(frame);
                if (printSwapFrames) System.out.println("Left  became black # " + whiteToBlackFramesLeft.size() + " at frame: " + frame);
            }

            // Save the current frame is black switches to white or vice versa in right video.
            if (isBlack(lastColorRight) && isWhite(colorRight)) {
                blackToWhiteFramesRight.add(frame);
                if (printSwapFrames) System.out.println("Right became white # " + blackToWhiteFramesRight.size() + " at frame: " + frame);
            }
            else if (isWhite(lastColorRight) && isBlack(colorRight)) {
                whiteToBlackFramesRight.add(frame);
                if (printSwapFrames) System.out.println("Right became black # " + whiteToBlackFramesRight.size() + " at frame: " + frame);
            }

            // Save current colors 
            lastColorLeft = colorLeft;
            lastColorRight = colorRight;
        }

        if (printSwapFrames) {
            System.out.println("Processing complete!");
        }

        ArrayList<Integer> whiteDelays = new ArrayList<Integer>();
        ArrayList<Integer> blackDelays = new ArrayList<Integer>();

        int leastAmountOfSwapsBlackToWhite = Math.min(blackToWhiteFramesLeft.size(), blackToWhiteFramesRight.size());
        int leastAmountOfSwapsWhiteToBlack = Math.min(whiteToBlackFramesLeft.size(), whiteToBlackFramesRight.size());

        // Lowest/highest delay
        int lowestWhiteDelay = 1000000;
        int highestWhiteDelay = 0;
        int lowestBlackDelay = 1000000;
        int highestBlackDelay = 0;
        int lowestDelay = 0;
        int highestDelay = 0;
 
        // Lowest/highest delay frame numbers. One per each of the above.
        int firstFrame_lowestWhiteDelay = 0;
        int firstFrame_highestWhiteDelay = 0;
        int firstFrame_lowestBlackDelay = 0;
        int firstFrame_highestBlackDelay = 0;
        int firstFrame_lowestDelay = 0;
        int firstFrame_highestDelay = 0;

        // Lowest/highest delay frame numbers. One per each of the above.
        int lastFrame_lowestWhiteDelay = 0;
        int lastFrame_highestWhiteDelay = 0;
        int lastFrame_lowestBlackDelay = 0;
        int lastFrame_highestBlackDelay = 0;
        int lastFrame_lowestDelay = 0;
        int lastFrame_highestDelay = 0;

        // Loop through all black to white swaps, and calculate
        // the difference of frame numbers between left and right, which is the delay in frames.
        for (int swapNum = 0; swapNum < leastAmountOfSwapsBlackToWhite; swapNum++) {
            int frameThatLeftBecameWhite = blackToWhiteFramesLeft.get(swapNum);
            int frameThatRightBecameWhite = blackToWhiteFramesRight.get(swapNum);

            int whiteDelay = frameThatRightBecameWhite - frameThatLeftBecameWhite;
            whiteDelays.add(whiteDelay);

            // Update lowest and highest white delays
            if (whiteDelay < lowestWhiteDelay) {
                lowestWhiteDelay = whiteDelay;
                firstFrame_lowestWhiteDelay = frameThatLeftBecameWhite;
                lastFrame_lowestWhiteDelay = frameThatRightBecameWhite;
            }
            if (whiteDelay > highestWhiteDelay) {
                highestWhiteDelay = whiteDelay;
                firstFrame_highestWhiteDelay = frameThatLeftBecameWhite;
                lastFrame_highestWhiteDelay = frameThatRightBecameWhite;
            }
        }

        // Do the same for white to black switches.
        for (int swapNum = 0; swapNum < leastAmountOfSwapsWhiteToBlack; swapNum++) {
            int frameThatLeftBecameBlack = whiteToBlackFramesLeft.get(swapNum);
            int frameThatRightBecameBlack = whiteToBlackFramesRight.get(swapNum);

            int blackDelay = frameThatRightBecameBlack - frameThatLeftBecameBlack;
            blackDelays.add(blackDelay);

            // Update lowest and highest black delays
            if (blackDelay < lowestBlackDelay) {
                lowestBlackDelay = blackDelay;
                firstFrame_lowestBlackDelay = frameThatLeftBecameBlack;
                lastFrame_lowestBlackDelay = frameThatRightBecameBlack;
            }
            if (blackDelay > highestBlackDelay) {
                highestBlackDelay = blackDelay;
                firstFrame_highestBlackDelay = frameThatLeftBecameBlack;
                lastFrame_highestBlackDelay = frameThatRightBecameBlack;
            }
        }

        // Add white and black delays to allDelays
        ArrayList<Integer> allDelays = new ArrayList<Integer>(whiteDelays);
        allDelays.addAll(blackDelays);

        // Save global lowest and highest delay 
        if (lowestWhiteDelay < lowestBlackDelay) {
            lowestDelay = lowestWhiteDelay;
            firstFrame_lowestDelay = firstFrame_lowestWhiteDelay;
            lastFrame_lowestDelay = lastFrame_lowestWhiteDelay;
        }
        else {
            lowestDelay = lowestBlackDelay;
            firstFrame_lowestDelay = firstFrame_lowestBlackDelay;
            lastFrame_lowestDelay = lastFrame_lowestBlackDelay;
        }
        if (highestWhiteDelay > highestBlackDelay) {
            highestDelay = highestWhiteDelay;
            firstFrame_highestDelay = firstFrame_highestWhiteDelay;
            lastFrame_highestDelay = lastFrame_highestWhiteDelay;
        }
        else {
            highestDelay = highestBlackDelay;
            firstFrame_highestDelay = firstFrame_highestBlackDelay;
            lastFrame_highestDelay = lastFrame_highestBlackDelay;
        }
        
        // Lowest and highest frame delays in milliseconds
        int lowestWhiteMilliSecDelay = (int) (lowestWhiteDelay * 16.6666);
        int highestWhiteMilliSecDelay = (int) (highestWhiteDelay * 16.6666);
        int lowestBlackMilliSecDelay = (int) (lowestBlackDelay * 16.6666);
        int highestBlackMilliSecDelay = (int) (highestBlackDelay * 16.6666);
        int lowestMilliSecDelay = (int) (lowestDelay * 16.6666);
        int highestMilliSecDelay = (int) (highestDelay * 16.6666);

        // Average delays in frames and milliseconds
        double averageWhiteFrameDelay = (double) getAverage(whiteDelays);
        double averageBlackFrameDelay = (double) getAverage(blackDelays);
        double averageFrameDelay = (averageWhiteFrameDelay + averageBlackFrameDelay) / 2;
        int averageWhiteMilliSecDelay = (int) (averageWhiteFrameDelay * 16.6666);
        int averageBlackMilliSecDelay = (int) (averageBlackFrameDelay * 16.6666);
        int averageMilliSecDelay = (int) (averageFrameDelay * 16.6666);

        // Median delays in frames and milliseconds
        double medianWhiteFrameDelay = (double) getMedian(whiteDelays);
        double medianBlackFrameDelay = (double) getMedian(blackDelays);
        double medianFrameDelay = (double) getMedian(allDelays);
        int medianWhiteMilliSecDelay = (int) (medianWhiteFrameDelay * 16.6666); 
        int medianBlackMilliSecDelay = (int) (medianBlackFrameDelay * 16.6666);
        int medianMilliSecDelay = (int) (medianFrameDelay * 16.6666);

        if (printDelaysSorted) {
            if (printBlackAndWhiteInfo) {
                System.out.println ("");
                System.out.println ("### ALL WHITE DELAYS SORTED ###");
                System.out.println ("");
    
                Integer[] whiteDelaysArray = new Integer[whiteDelays.size()];
                whiteDelaysArray = whiteDelays.toArray(whiteDelaysArray);
                Arrays.sort(whiteDelaysArray);
                for (int i = 0; i < whiteDelaysArray.length; i++) {
                    int whiteDelayInFrames = whiteDelaysArray[i];
                    int whiteDelayInMilliSeconds = (int) (whiteDelayInFrames * 16.6666);
                    System.out.print("white delay size-order # " + (i+1) + ":\t " + whiteDelayInFrames + " frames\t= " + whiteDelayInMilliSeconds + "ms");
                    if (i > 0 && whiteDelaysArray[i] > whiteDelaysArray[i-1] * 2) {
                        System.out.println("\t(doubled)");
                    }
                    else {
                        System.out.println("");
                    }
                }
    
                System.out.println ("");
                System.out.println ("### ALL BLACK DELAYS SORTED ###");
                System.out.println ("");
    
                Integer[] blackDelaysArray = new Integer[blackDelays.size()];
                blackDelaysArray = whiteDelays.toArray(blackDelaysArray);
                Arrays.sort(blackDelaysArray);
                for (int i = 0; i < blackDelaysArray.length; i++) {
                    int blackDelayInFrames = blackDelaysArray[i];
                    int blackDelayInMilliSeconds = (int) (blackDelayInFrames * 16.6666);
                    System.out.print("black delay size-order # " + (i+1) + ":\t " + blackDelayInFrames + " frames\t= " + blackDelayInMilliSeconds + "ms");
                    if (i > 0 && blackDelaysArray[i] > blackDelaysArray[i-1] * 2) {
                        System.out.println("\t(doubled)");
                    }
                    else {
                        System.out.println("");
                    }
                }
            }

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
        }

        if (printBlackAndWhiteInfo) {
            System.out.println("");
            System.out.println("--------------------------");
            System.out.println("@@@ SUMMARY WHITE AND BLACK DELAYS @@@");
            System.out.println("");

            System.out.println("File name: " + folderWithFramesName);
            System.out.println("Total frame files analyzed: " + listOfFiles.length);
            System.out.println("--- TOTAL: white delay ---");
            System.out.println("Flips from black to white left: " + blackToWhiteFramesLeft.size() + ", right: " + blackToWhiteFramesRight.size());
            System.out.println("Lowest white delay: " + lowestWhiteMilliSecDelay + "ms = " + lowestWhiteDelay + " frames, left became white at " + firstFrame_lowestWhiteDelay + ", right at " + lastFrame_lowestWhiteDelay);
            System.out.println("Highest white delay: " + highestWhiteMilliSecDelay + "ms = " + highestWhiteDelay + " frames, left became white at " + firstFrame_highestWhiteDelay + ", right at " + lastFrame_highestWhiteDelay);

            System.out.println("");
            System.out.println("--- TOTAL: black delay ---");
            System.out.println("Flips from white to black left: " + whiteToBlackFramesLeft.size() + ", right: " + whiteToBlackFramesRight.size());
            System.out.println("Lowest black delay: " + lowestBlackMilliSecDelay + "ms = " + lowestBlackDelay + " frames, left became black at " + firstFrame_lowestBlackDelay + ", right at " + lastFrame_lowestBlackDelay);
            System.out.println("Highest black delay: " + highestBlackMilliSecDelay + "ms = " + highestBlackDelay + " frames, left became black at " + firstFrame_highestBlackDelay + ", right at " + lastFrame_highestBlackDelay);

            System.out.println("");
            System.out.println("--- AVERAGE: white delay ---");
            System.out.println("Average delay: " + (int) averageWhiteFrameDelay + " frames = " + averageWhiteMilliSecDelay + " ms");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, averageWhiteFrameDelay, 0.75, 1.25) + "% of delays were within 25% of average.");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, averageWhiteFrameDelay, 0.5, 1.5) + "% of delays were within 50% of average.");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, averageWhiteFrameDelay, 0.2, 1.8) + "% of delays were within 80% of average.");

            System.out.println("");
            System.out.println("--- AVERAGE: black delay ---");
            System.out.println("Average delay: " + (int) averageBlackFrameDelay + " frames = " + averageBlackMilliSecDelay + " ms");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, averageBlackFrameDelay, 0.75, 1.25) + "% of delays were within 25% of average.");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, averageBlackFrameDelay, 0.5, 1.5) + "% of delays were within 50% of average.");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, averageBlackFrameDelay, 0.2, 1.8) + "% of delays were within 80% of average.");

            System.out.println("");
            System.out.println("--- MEDIAN: white delay ---");
            System.out.println("Median delay: " + (int) medianWhiteFrameDelay + " frames = " + medianWhiteMilliSecDelay + " ms");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, medianWhiteFrameDelay, 0.75, 1.25) + "% of delays were within 25% of median.");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, medianWhiteFrameDelay, 0.5, 1.5) + "% of delays were within 50% of median.");
            System.out.println(pctWithinPctRangeOfValue(whiteDelays, medianWhiteFrameDelay, 0.2, 1.8) + "% of delays were within 80% of median.");

            System.out.println("");
            System.out.println("--- MEDIAN: black delay ---");
            System.out.println("Median delay: " + (int) medianBlackFrameDelay + " frames = " + medianBlackMilliSecDelay + " ms");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, medianBlackFrameDelay, 0.75, 1.25) + "% of delays were within 25% of median.");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, medianBlackFrameDelay, 0.5, 1.5) + "% of delays were within 50% of median.");
            System.out.println(pctWithinPctRangeOfValue(blackDelays, medianBlackFrameDelay, 0.2, 1.8) + "% of delays were within 80% of median.");
        }
        
        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("@@@ SUMMARY ALL DELAYS @@@");
        System.out.println("");

        System.out.println("File name: " + folderWithFramesName);
        System.out.println("Total frame files analyzed: " + listOfFiles.length);
        System.out.println("--- TOTAL ---");
        System.out.println("Flips from black to white left: " + blackToWhiteFramesLeft.size() + ", right: " + blackToWhiteFramesRight.size());
        System.out.println("Flips from white to black left: " + whiteToBlackFramesLeft.size() + ", right: " + whiteToBlackFramesRight.size());
        System.out.println("Lowest delay: " + lowestMilliSecDelay + "ms = " + lowestDelay + " frames, left flipped at " + firstFrame_lowestDelay + ", right at " + lastFrame_lowestDelay);
        System.out.println("Highest delay: " + highestMilliSecDelay + "ms = " + highestDelay + " frames, left flipped at " + firstFrame_highestDelay + ", right at " + lastFrame_highestDelay);

        System.out.println("");
        System.out.println("--- AVERAGE ---");
        System.out.println("Average delay: " + (int) averageFrameDelay + " frames = " + averageMilliSecDelay + " ms");
        System.out.println(pctWithinPctRangeOfValue(allDelays, averageFrameDelay, 0.75, 1.25) + "% of delays were within 25% of average.");
        System.out.println(pctWithinPctRangeOfValue(allDelays, averageFrameDelay, 0.5, 1.5) + "% of delays were within 50% of average.");
        System.out.println(pctWithinPctRangeOfValue(allDelays, averageFrameDelay, 0.2, 1.8) + "% of delays were within 80% of average.");

        System.out.println("");
        System.out.println("--- MEDIAN ---");
        System.out.println("Median delay: " + (int) medianFrameDelay + " frames = " + medianMilliSecDelay + " ms");
        System.out.println(pctWithinPctRangeOfValue(allDelays, medianFrameDelay, 0.75, 1.25) + "% of delays were within 25% of median.");
        System.out.println(pctWithinPctRangeOfValue(allDelays, medianFrameDelay, 0.5, 1.5) + "% of delays were within 50% of median.");
        System.out.println(pctWithinPctRangeOfValue(allDelays, medianFrameDelay, 0.2, 1.8) + "% of delays were within 80% of median.");
    }

    // Returns the percentage amount (an int between 0 and 100) of the list values that are within 
    // a certain percentage of the list average.
    // Example: pctWithinPctRangeOfAverage(list, 0.5, 1.5) gives percentage amount of 
    // values in list that are between 50% and 150% of the average value.
    static int pctWithinPctRangeOfValue(ArrayList<Integer> list, double comparisonValue, double lower, double upper) {
        int countWithinRange = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) >= lower * comparisonValue && list.get(i) <= upper * comparisonValue) {
                countWithinRange++;
            }
        }
        double pctWithinRange = (double) countWithinRange / (double) list.size();
        return (int) (((double) 100) * pctWithinRange);
    }

    static boolean oneIsBlackOneIsWhite(Color c1, Color c2) {
        return (isBlack(c1) && isWhite(c2)) || (isWhite(c1) && isBlack(c2));
    }

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

    // Function made by stackoverflow user Jared Rummler.
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