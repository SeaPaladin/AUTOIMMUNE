/**
 * class to create level layouts
*/
class LevelGenerator {
  int[][] _mapGrid = new int[5][5];
  int _maps = 11;

  LevelGenerator() {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        _mapGrid[i][j] = 0;
      }
    }
    _mapGrid[2][2] = 2;
    createGrid();
  }

  void createGrid() {
    int a = rand(4);
    int x, y;
    if (a == 0) {
      x = 0;
      y = rand(4);
    } else if (a == 1) {
      x = 4;
      y = 1 + rand(4);
    } else if (a == 2) {
      y = 0;
      x = 1 + rand(4);
    } else {
      y = 4;
      x = rand(4);
    }
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        String[] room = loadStrings("maps/map" + rand(_maps) + ".map");
        String[] obj = new String[$rows];
        for (int m = 0; m < $rows; m++) {
          obj[m] = "";
          for (int n = 0; n < $cols; n++) {
            if (room[m].charAt(n) == '0' && rand(10) == 1 && m > 1 && m < $rows - 2 && n > 1 && n < $cols - 2) {
              if (rand(3) == 0) {
                obj[m] += 'W';
              } else if (rand(3) == 1) {
                obj[m] += 'X';
              } else {
                obj[m] += 'Y';
              }
            } else {
              obj[m] += '0';
            }
          }
        }
        if (i == x && j == y) {
          room = loadStrings("maps/transMap.map");
          obj = loadStrings("maps/objectMapError.map");
        }
        if (i == 0) {
          String line = room[0].substring(0, $cols / 2 - 1);
          line += "11" + room[0].substring($cols / 2 + 1);
          room[0] = line;
        }
        if (j == 0) {
          String col = "1" + room[$rows / 2 - 1].substring(1);
          room[$rows / 2 - 1] = col;
          col = "1" + room[$rows / 2].substring(1);
          room[$rows / 2] = col;
        }
        if (i == 4) {
          String line = room[$rows - 1].substring(0,
              $cols / 2 - 1);
          line += "11" + room[$rows - 1].substring($cols / 2 + 1);
          room[$rows - 1] = line;
        }
        if (j == 4) {
          String col = room[$rows / 2 - 1].substring(0, room[$rows / 2 - 1].length() - 1) + "1";
          room[$rows / 2 - 1] = col;
          col = room[$rows / 2].substring(0, room[$rows / 2].length() - 1) + "1";
          room[$rows / 2] = col;
        }
        if (i == 2 && j == 2) {
          room = loadStrings("maps/tileMapError.map");
          obj = loadStrings("maps/objectMapError.map");
        }
        saveStrings("data/maps/tileMap" + i + "," + j + ".map", room);
        saveStrings("data/maps/objectMap" + i + "," + j + ".map", obj);
      }
    }
  }

  void displayGrid() {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (_mapGrid[i][j] > 0) {
          if ($rowMap == j && $colMap == i) {
            fill(255);
          } else {
            fill(127);
          }
          rect(53 + i % 5 * 30, 240 + j * 20, 30, 20);
          if (_mapGrid[i][j] == 2) {
            stroke(255,0,0);
            strokeWeight(5);
            noFill();
            ellipse(68 + i % 5 * 30, 250 + j * 20, 14, 14);
            stroke(0);
            strokeWeight(1);
          }
        }
      }
    }
  }

  void setExplored(int i, int j) {
    if (_mapGrid[i][j] == 0) {
      _mapGrid[i][j] = 1;
    }
  }
  
  void setComplete(int i, int j) {
    if (i < 5 && j < 5 && _mapGrid[i][j] != 2) { 
      _mapGrid[i][j] = 2;
      $sounds[2].rewind();
      $sounds[2].play();
      $roomTotal++;
      $score += 200;
    }
  }
}
