# latency-analyzer
A java program used for analyzing the delay between two video streams that are switching from black and white continuously.

## How to run with example data
1. Compile with `javac Main.java`
2. Run `java Main exampleData`

The `Main.java` file will not only print the individual delays, but also some extra statistics.

If you only want to print the individual delays, the file `OnlyPrintDelays.java` can be used instead. The delays will be printed both in sorted order, and in original order. `OnlyPrintDelays.java` can be compiled and run similarly to `Main.java`.

## Different versions

The `Main.java` file simply takes note of which frames each color swap happens for left and right, and then compares the frame difference for each swap for left and right.

The file `WaitForNext.java` uses a different method for measuring the individual delays. After the left part of the video has turned black/white, the program will *wait* for the right part to do so as well. This method can be useful for certain videos where at least *one* delay is longer than three seconds. This is because the `Main.java` version could *miss* one color swap during that delay, resulting in inaccurate measuring results. All delays following the *missed color swap* would inaccurately be measured as longer than they actually are. `WaitForNext.java` can be compiled and run similarly to `Main.java`.

## Example output
Example output can be found in the file `examepleOutput.txt`.
