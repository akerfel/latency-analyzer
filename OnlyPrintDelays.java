import java.io.File;
import java.awt.Image;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

// This file only prints the individual delays, without calculating the median and other relevant data.

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
public class OnlyPrintDelays {
    public static void main(String[] args) {

        // --- SETTINGS, TO BE CHANGED BY USER ---
        boolean printSwapFrames = true; // If true, will print info about EVERY swap. Adds about 20% runtime

        // First command line option
        String folderWithFramesName = args[0]; // This folder must be located inside the data folder, and should contains frames from video

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
            }
            else if (isWhite(lastColorLeft) && isBlack(colorLeft)) {
                whiteToBlackFramesLeft.add(frame);
            }

            // Save the current frame is black switches to white or vice versa in right video.
            if (isBlack(lastColorRight) && isWhite(colorRight)) {
                blackToWhiteFramesRight.add(frame);
            }
            else if (isWhite(lastColorRight) && isBlack(colorRight)) {
                whiteToBlackFramesRight.add(frame);
            }

            // Save current colors 
            lastColorLeft = colorLeft;
            lastColorRight = colorRight;
        }

        ArrayList<Integer> whiteDelays = new ArrayList<Integer>();
        ArrayList<Integer> blackDelays = new ArrayList<Integer>();

        int leastAmountOfSwapsBlackToWhite = Math.min(blackToWhiteFramesLeft.size(), blackToWhiteFramesRight.size());
        int leastAmountOfSwapsWhiteToBlack = Math.min(whiteToBlackFramesLeft.size(), whiteToBlackFramesRight.size());

        // Loop through all black to white swaps, and calculate
        // the difference of frame numbers between left and right, which is the delay in frames.
        for (int swapNum = 0; swapNum < leastAmountOfSwapsBlackToWhite; swapNum++) {
            int frameThatLeftBecameWhite = blackToWhiteFramesLeft.get(swapNum);
            int frameThatRightBecameWhite = blackToWhiteFramesRight.get(swapNum);

            int whiteDelay = frameThatRightBecameWhite - frameThatLeftBecameWhite;
            whiteDelays.add(whiteDelay);
        }

        // Do the same for white to black switches.
        for (int swapNum = 0; swapNum < leastAmountOfSwapsWhiteToBlack; swapNum++) {
            int frameThatLeftBecameBlack = whiteToBlackFramesLeft.get(swapNum);
            int frameThatRightBecameBlack = whiteToBlackFramesRight.get(swapNum);
            int blackDelay = frameThatRightBecameBlack - frameThatLeftBecameBlack;
            blackDelays.add(blackDelay);
        }

        // Add white and black delays to allDelays
        ArrayList<Integer> allDelays = new ArrayList<Integer>(whiteDelays);
        printAllDelays(allDelays);
    }

    static void printAllDelays(ArrayList<Integer> allDelays) {
        allDelays.addAll(blackDelays);

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

    static boolean oneIsBlackOneIsWhite(Color c1, Color c2) {
        return (isBlack(c1) && isWhite(c2)) || (isWhite(c1) && isBlack(c2));
    }

    static boolean isBlack(Color c) {
        return c.getRed() < 40 && c.getGreen() < 40 && c.getBlue() < 40;
    }
    
    static boolean isWhite(Color c) {
        return c.getRed() > 230 && c.getGreen() > 230 && c.getBlue() > 230;
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