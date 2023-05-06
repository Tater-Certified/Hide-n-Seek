# Hide-n-Seek
### Server Side Hide and Seek mod

In this mod hiders are forced to hide away in a map and then seekers are released to find them!

## Features:
- /hide-n-seek command to control the game
- fully customizable events json (see examples after running the server)
- config for adjusting game values
- multi-seeker support
- compass hider tracking

## Custom Events Json Example:
```json
[
  {
    "time": 2000,
    "actionType": "announcement",
    "data": [
      "This is an example announcement at 2000 ticks!"
    ]
  },
  {
    "time": 2000,
    "actionType": "item-hider",
    "data": [
      "diamond_sword1",
      "golden_carrot64"
    ]
  },
  {
    "time": 2000,
    "actionType": "item-seeker",
    "data": [
      "diamond_sword1",
      "golden_carrot64"
    ]
  },
  {
    "time": 400,
    "actionType": "release"
  },
  {
    "time": 2000,
    "actionType": "compass"
  },
  {
    "time": 2000,
    "actionType": "hider-pvp"
  }
]
```
This example executes the following:
- Announcement at 2000 ticks saying "This is an example announcement at 2000 ticks!"
- Gives a diamond sword and 64 golden carrots to the hiders at 2000 ticks
- Gives a diamond sword and 64 golden carrots to the seekers at 2000 ticks
- Releases the seekers at 400 ticks
- Gives a tracking compass to the seeker at 2000 ticks
- Lets the hiders attack the seekers at 2000 ticks
