# OOPBoy
by Garrett Gu and Ryan Jacobs

![A screenshot of OOPBoy playing The Legend of Zelda: Link's Awakening](https://raw.githubusercontent.com/429ers/OOPBoy/master/screenshot.png)

OOPBoy is a working, fast, and cross-platform Nintendo Game Boy emulator written in Java. 

## Table of Contents
1. [Features](#features)
2. [Using the debugger](#using-the-debugger)
	1. [Some Debugger Commands](#some-debugger-commands)
3. [Autosaves](#autosaves)
4. [Tested Games](#tested-games)
5. [Compiling the emulator](#compiling-the-emulator)
6. [Running the emulator](#running-the-emulator)

## Features
- Full CPU emulation (passes cpu_instrs, instr_timing, mem_timing)
- Full Timer emulation
- Full DMG PPU support, GBC work-in-progress
- Object Oriented Design
- MBC1 and MBC3 support with battery-backed RAM
- Audio unit with stereo support
- Save states at the emulator level
- RAM-based auto-save support ("Rewind")
- Turbo mode
- Graphics modes including gray-scale, classic green, and psychedelic mode
- Debugger with break points, core dumps, memory access, instruction stepping, and instruction history

## Using the debugger
The debugger is enabled by using the -d flag:  
```java org.the429ers.gameboy.GameBoy -d```  

When this flag is supplied, the program will prompt for an initial breakpoint on the command line.  

When debugging the emulator itself, I often found it helpful to run the debugger within another Java debugger such as jdb.

### Some Debugger Commands
- `b nn`
	- Adds a breakpoint to the specified hex location
	- Ex: `b 2f`
- `d 00`
	- Deletes a breakpoint at the specified hex location
	- Ex: `d 2f`
- `c`
	- Continues execution
- `xc`
	- Prints a core dump, along with other pertinent information
- `xm nn n`
	- Prints `n` bytes starting at memory location `nn`
	- `n` is base-10, `nn` is base-16
	- Ex: `xm 48 4`
- `sm nn mm`
	- Sets the byte at memory location `nn` to the value `mm`
	- Both values are in base-16
	- Ex: `sm 48 ff`
- `n`
	- Executes one instruction and prints a core dump
- `nm m`
	- Executes `m` more instructions and breaks
	- Ex: `nm 20`
- `xh`
	- Prints the locations of the previous 100 instructions in base-16 format
	
## Autosaves
The autosave system is meant to mimic the "Rewind" feature on Nintendo Switch Online NES.  

If autosaves are enabled under the debug menu, a save file is generated in RAM every two seconds (120 frames), and is kept for a minute. It can be restored by using the option under the Load menu or by using the corresponding keyboard shortcut.  

Pressing the button multiple times in a row will roll back the corresponding number of autosaves. For example, pressing the load button once will cause the game to "rewind" two seconds, pressing it twice will make the game "rewind" four seconds, and so on.

## Tested Games
The following games are perfectly playable as far as we can tell:
- The Legend of Zelda: Link's Awakening
- Super Mario Land
- Super Mario Land 2
- Super Mario Land 3: Wario Land
- Mario's Picross
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
