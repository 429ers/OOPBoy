# OOPBoy
by Garrett Gu and Ryan Jacobs

![A screenshot of OOPBoy playing The Legend of Zelda: Link's Awakening](https://raw.githubusercontent.com/429ers/OOPBoy/master/screenshot.png)

OOPBoy is a working, fast, and cross-platform Nintendo Game Boy emulator written in Java. 

## Features
- Full CPU emulation (passes cpu_instrs, instr_timing, mem_timing)
- Full Timer emulation
- Full DMG PPU support, GBC work-in-progress
- Object Oriented Design
- MBC1 and MBC3 support with battery-backed RAM
- Audio unit with stereo support
- Save states at the emulator level
- Turbo mode
- Graphics modes including gray-scale, classic green, and psychedelic mode
- Debugger with break points, core dumps, memory access, instruction stepping, and instruction history

## Tested Games
The following games are perfectly playable as far as we can tell:
- The Legend of Zelda: Link's Awakening
- Super Mario Land
- Super Mario Land 2
- Super Mario Land 3: Wario Land
- Tetris
- Tetris DX
- Pokemon - Red Version
- Pokemon - Blue Version
- Dr. Mario
- Pac-Man
- Serpent
- Kirby's Dream Land
- Tennis

The following games play with major graphical glitches:
- Donkey Kong

The following games do not play at all:
- Please wait for me to test more games

## Compiling the emulator
Go into the source folder and run ```javac *.java```. There are no external dependencies.

## Running the emulator
The ```main()``` function is located in ```GameBoy.java```.
```
cd src
javac org/the429ers/gameboy/*.java
java org.the429ers.gameboy.Gameboy
```

There is also an executable jar file available for each release.

## Running the emulator in debug mode
```java org.the429ers.gameboy.GameBoy -d```
