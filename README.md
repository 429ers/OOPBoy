# GheithBoy
by Garrett Gu and Ryan Jacobs

GheithBoy is a working, fast, and cross-platform Nintendo Game Boy emulator written in Java. It was created for CS 429h, the Honors Computer Architecture class taught by Professor Ahmed Gheith at the University of Texas at Austin.

## Features
- Full CPU emulation (passes cpu_instrs, instr_timing)
- Full Timer emulation
- MBC1 and MBC3 support with battery-backed RAM
- Audio unit with stereo support
- Save states at the emulator level
- Turbo mode
- Debugger with break points, core dumps, memory access, instruction stepping, and instruction history

## Tested Games
The following games are perfectly playable as far as we can tell:
- The Legend of Zelda: Link's Awakening
- Super Mario Land
- Tetris
- Pokemon - Red Version
- Pokemon - Blue Version
- Dr. Mario
- Pac-Man
- Serpent

The following games play with major graphical glitches:
- Kirby's Dream Land

The following games do not play at all:
- Donkey Kong
- Super Mario Land 2
- Super Mario Land 3: Wario Land
- Tennis

## Compiling the emulator
Go into the source folder and run ```javac *.java```. There are no external dependencies.

## Running the emulator
The ```main()``` function is located in ```GameBoy.java```.

## Running the emulator in debug mode
```java GameBoy -d```