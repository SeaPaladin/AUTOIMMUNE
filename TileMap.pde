/**
 * class for background and object maps. not random at the moment
*/
class TileMap {
  Tile[][] _tiles, _tiles2; // map and its copy
  String _type; // type of map
  String[] _tileMap;

  TileMap(String type) {
    _type = type;
    _tiles = new Tile[$rows][$cols];
    _tiles = getTiles();
  }

  /**
   * load the tilemap
  */
  Tile[][] getTiles() {
    String fileName = "maps/" + _type + "Map" + $rowMap + "," + $colMap + ".map";
    File file = new File(dataPath(fileName));
    if (file.exists()) {
      _tileMap = loadStrings(fileName);
    } else {
      _tileMap = loadStrings("maps/" + _type + "MapError.map");
    }
    Tile[][] tiles = new Tile[$rows][$cols];
    for (int i = 0; i < $rows; i++) {
      for (int j = 0; j < $cols; j++) {
        tiles[i][j] = new Tile(_type, _tileMap[i].charAt(j), j * $tSize + $bannerWidth, i * $tSize, i, j);
      }
    }
    return tiles;
  }

  void updateBG() {
    for (int i = 0; i < $rows; i++) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j].paint(j * $tSize + $bannerWidth, i * $tSize);
      }
    }
  }

  void replace(int row, int col, char replace) {
    String line = _tileMap[row].substring(0, col);
    line += replace + _tileMap[row].substring(col + 1);
    _tileMap[row] = line;
    saveStrings("data/maps/" + _type + "Map" + $rowMap + "," + $colMap + ".map", _tileMap);
  }

  void scroll() {
    int deltaX = $colMap - $colMapInit;
    int deltaY = $rowMap - $rowMapInit;
    if ($frame == 0) {
      _tiles2 = getTiles();
    }
    if (deltaX == -1) {
      scrollLeft();
    } else if (deltaX == 1) {
      scrollRight();
    } else if (deltaY == 1) {
      scrollDown();
    } else if (deltaY == -1) {
      scrollUp();
    }
    updateBG();
    if ($frame + 1 >= abs(deltaY * $rows) + abs(deltaX * $cols) && _type == "object") {
      $lg.setExplored($colMap, $rowMap);
      $frame = -1;
      $mode = "play";
      $rowMapInit = $rowMap;
      $colMapInit = $colMap;
      if ($enemyCountMax == 0) {
        $lg.setComplete($colMap, $rowMap);
      }
    }
  }

  void scrollLeft() {
    for (int i = $cols - 1; i > 0; i--) {
      for (int j = 0; j < $rows; j++) {
        _tiles[j][i] = _tiles[j][i - 1];
      }
    }
    for (int i = 0; i < $rows; i++) {
      _tiles[i][0] = _tiles2[i][$cols - 1 - $frame];
    }
  }

  void scrollRight() {
    for (int i = 0; i < $cols - 1; i++) {
      for (int j = 0; j < $rows; j++) {
        _tiles[j][i] = _tiles[j][i + 1];
      }
    }
    for (int i = 0; i < $rows; i++) {
      _tiles[i][$cols - 1] = _tiles2[i][$frame];
    }
  }

  void scrollUp() {
    for (int i = $rows - 1; i > 0; i--) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j] = _tiles[i - 1][j];
      }
    }
    for (int i = 0; i < $cols; i++) {
      _tiles[0][i] = _tiles2[$rows - 1 - $frame][i];
    }
  }

  void scrollDown() {
    for (int i = 0; i < $rows - 1; i++) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j] = _tiles[i + 1][j];
      }
    }
    for (int i = 0; i < $cols; i++) {
      _tiles[$rows - 1][i] = _tiles2[$frame][i];
    }
  }

  Tile[][] getBounds(float x, float y, float w, float h) {
    Tile[][] bounds = new Tile[2][2];
    int x1 = floor((x - $bannerWidth) / $tSize);
    int x2 = floor((x - $bannerWidth + w - 1) / $tSize);
    int y1 = floor(y / $tSize);
    int y2 = floor((y + h - 1) / $tSize);
    if (x1 < 0) {
      x1 = 0;
      x2 = 0;
    }
    if (x2 >= $cols) {
      x2 = $cols - 1;
      x1 = $cols - 1;
    }
    if (y1 < 0) {
      y1 = y2 = 0;
    }
    if (y2 >= $rows) {
      y2 = y1 = $rows - 1;
    }
    bounds[0][0] = _tiles[y1][x1];
    bounds[0][1] = _tiles[y1][x2];
    bounds[1][0] = _tiles[y2][x1];
    bounds[1][1] = _tiles[y2][x2];
    return bounds;
  }
}
