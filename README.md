# GridWars
Grid Wars is a Chess-Like Turn-Based game written in Java using libgdx libraries. The game features two teams of creatures that compete to either defeat all members of the other team, or land on the other team's "Zone". The game has 3 main modes: Death Match, Zone Match, and Survival.

I made the project to work on when I have free time, so I could familiarize myself more with the Java and programming in general while working towards a cool end product.

## Running the Game
Head over to the Releases sections, then download the jar file of the most recent version. Make sure you have at least Java 8 installed.

## How to Play
After selecting teams in the Team Select menu, you will be placed right into the Battle Screen. Your goal is to defeat the other team by defeating all members of the opposing team (or place one of your pieces onto the opponent's Zone if it is a Zone Match).
### The Battle Screen UI
The checker board print in the center is the game board, where the core gameplay happens. The box at the top is the Infobar, which says different messages based on what is happening in the game. The box to the top-right is the Status Box, which lists all the information about a game piece.
* HP represents the amount of damage a piece can take before being defeated.
* SP represents a skill points. Certain attacks consume skill points to work, and you regain 1 skill point at the beginning of your turn. 
* Attack represents attack power. The amount of damage a game-piece can inflict is based off this value.
* Defense represents defensive power. The amount of damage a game-piece receive is based off this value.
* Speed determines how many tiles a piece can move each turn.
* The last message tells what status effects a game-piece has. Healthy (or ---) means that they have no status effects active. Status effects can be beneficial or harmful, and can augment stats (the stat will be colored red or green). Some statuses even have end-of-turn effects like poison, which inflicts 1 point of damage at the start of each turn.
The box on the bottom right is the Move-List, which tells all the attacks a game-piece can use. The number in parenthesis next to the name tells how much SP must be consumed to use the move. The box on the bottom is the Team Bar, which shows all the members on a team. This also has the End Turn Button, which is used to end your turn and begin the opponent’s turn.
### Movement
To move your team members, click on them. If that piece is on your team, all tiles it can move to will change color. Clicking on any of these colored tiles will move the piece to the selected space. You can only move each piece once per turn! Note that spaces you can move is determined using taxicab distance, so other game-pieces can heavily affect available spaces.
### Attacking
Click on a team member to select them, then head over to the Move List. Hovering over any of the attacks will highlight effected spaces in red. To change the direction of the attack, use the scroll wheel. To use an attack, click any of the buttons. You can only attack once per turn, and you must have enough SP to use the move.
### How to Win
In death match, the only way to win is to reduce each enemy-piece's HP to 0.
Zone match adds an additional win objective, which is to land on the enemy team's zone. Zones spaces change based on their owner team's color. 
### Shortcut Keys
* A & D - shift through game-pieces
* SHIFT(left) - End turn
* MINUS or EQUAL - change game speed

## Game Modes
* Death Match allows you to choose 2 teams and a board to start a Death Match in.
* Zone Match allows you to choose 2 teams and a board to start a Zone Match in.
* In Survival, you choose a team and fight through 50 consecutive levels. You have a limited number of power ups.

## Credits
Music from the various games in the Bomberman series.
* Super Bomberman 1
* Super Bomberman 2
* Super Bomberman 3
* Super Bomberman 4
* Bomberman Wars
* Super Bomberman Panic Bomber W
* The Baku Bomb!!! Baku Bomberman Original Soundtrack (Bomberman 64)
* A few others as well
