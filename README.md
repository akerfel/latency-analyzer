# latency-analyzer
A Java program used for analyzing the delay between two video streams which are switching from black and white continuously. This program was specifically made for a thesis, and thus we have not provided detailed instructions for how to use it (although we have provided some general instructions)

## Thesis
[You can find the thesis LaTeX source code and other resources here.](https://github.com/akerfel/Tor-video-latency-thesis) All of the measured latencies can also be found there.

## How to run with example data
1. Compile with `javac Main.java`
2. Run `java Main exampleData`

The `Main.java` file will not only print the individual delays, but also some extra statistics. If you only want to print the individual delays, the file `OnlyPrintDelays.java` can be used instead. The delays will be printed both in sorted order, and in original order. `OnlyPrintDelays.java` can be compiled and run similarly to `Main.java`.

## Different versions

The `Main.java` file simply takes note of which frames each color swap happens for left and right, and then compares the frame difference for each swap for left and right.

The file `WaitForNext.java` uses a different method for measuring the individual delays. After the left part of the video has turned black/white, the program will *wait* for the right part to do so as well. This method can be useful for certain videos where at least *one* delay is longer than three seconds (assuming the color switch occurs every third second). This is because the `Main.java` version could *miss* one color swap during that delay, resulting in inaccurate measuring results. All delays following the *missed color swap* would inaccurately be measured as longer than they actually are. `WaitForNext.java` can be compiled and run similarly to `Main.java`.

## Example data and output
There is a folder with example data in the data folder. That folder contains a small sample size of 1908 files/frames, i.e. about 32 seconds of video (60 FPS). Ten delays were detected and measured, which is resonable since a color swap happened every third second. In our project, each test was five minutes long, and each scenario was tested six times. 

Example output can be found in the file `examepleOutput.txt`.

## License
Shield: [![CC BY-SA 4.0][cc-by-sa-shield]][cc-by-sa]

This work is licensed under a
[Creative Commons Attribution-ShareAlike 4.0 International License][cc-by-sa].

[![CC BY-SA 4.0][cc-by-sa-image]][cc-by-sa]

[cc-by-sa]: http://creativecommons.org/licenses/by-sa/4.0/
[cc-by-sa-image]: https://licensebuttons.net/l/by-sa/4.0/88x31.png
[cc-by-sa-shield]: https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg
