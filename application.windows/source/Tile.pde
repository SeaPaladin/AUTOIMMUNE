/**
 * class for each individual tile
 */
class Tile {
  String _type;
  char _index;
  int _tX, _tY;
  ArrayList<PImage> _frame;
  int _lim;
  int _speed = 250;

  Tile(String type, char index, int tX, int tY, int row, int col) {
    _type = type;
    _index = index;
    _tX = tX;
    _tY = tY;
    if (type == "object" && index == 'W') {
      $enemies.add(new Enemy(_tX, _tY, 45, 45, 3.0f, 1, 1, row, col, 2));
      $enemyCountMax++;
      _index = '0';
    }
    if (type == "object" && index == 'X') {
      $enemies.add(new Enemy(_tX, _tY, 45, 45, 1.0f, 2, 1, row, col, 0));
      $enemyCountMax++;
      _index = '0';
    }
    if (type == "object" && index == 'Y') {
      $enemies.add(new Enemy(_tX, _tY, 30, 30, 2.0f, 1, 1, row, col, 2*rand(2)));
      $enemyCountMax++;
      _index = '0';
    }
    if (type == "object" && index == 'Z') {
      $enemies.add(new Enemy(_tX, _tY, 48, 48, 2.0f, 10, 150, row, col, 1));
      _index = '0';
    }
    _frame = $images.getSprites(_type, _index);
  }

  char index() {
    return _index;
  }

  void paint(int x, int y) {
    int a = animate();
    image(_frame.get(a), x, y);
    noTint();
  }

  int animate() {
    return (millis() / _speed % _frame.size());
  }

  Boolean isBlock() {
    if (_type == "tile") {
      return _index == '1';
    }
    return false;
  }

  Boolean isTrans() {
    if (_type == "tile") {
      return _index == '2';
    }
    return false;
  }
}
