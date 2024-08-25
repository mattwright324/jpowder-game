# <img src='./src/main/resources/icon.png' width=42> JPowder

![Github All Releases](https://img.shields.io/github/downloads/mattwright324/jpowder-game/total.svg?style=flat-square)
![GitHub release](https://img.shields.io/github/release/mattwright324/jpowder-game.svg?style=flat-square)
![Github Releases](https://img.shields.io/github/downloads/mattwright324/jpowder-game/latest/total.svg?style=flat-square)

A java cellular automata based on The Powder Toy.

Thread located at [powdertoy.co.uk](http://powdertoy.co.uk/Discussions/Thread/View.html?Thread=19989&PageNum=0)

![Preview](./README_preview.png)

Keybinding help, the same info can also be enabled on the hud in-game.

| Control | Action                                         |
|---------|------------------------------------------------|
| T       | Toggle drawing shape as Circle or Square.      |
| F       | Update by a single frame.                      |
| H       | Toggle HUD                                     |
| S       | Toggle window size.                            |
| SPACE   | Pause & Play                                   |
| [ ]     | Change drawing size +- 1                       |
| 1 to 4  | View type: Default, Temp, Life Gradient, Fancy |

## Download

[![GitHub Releases](https://img.shields.io/badge/downloads-releases-brightgreen.svg?maxAge=60&style=flat-square)](https://github.com/mattwright324/jpowder-game/releases)

Be sure to have at least Java 11 installed.

Extract the latest release zip file and run `jpowder-yyyyMMdd.HHmmss.jar`.

## Build

Use the clean build commands to test a build. Use the run command to build and run.

```sh
$ ./gradlew clean build
$ ./gradlew run
```

## Package

Run the package command then zip up the `build/package` folder contents for a release.

```sh
$ ./gradlew packageJar
```