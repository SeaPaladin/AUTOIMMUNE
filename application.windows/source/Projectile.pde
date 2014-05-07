/**
 * class for projectile shots
 */
class Projectile {
  int _power;
  float _pX, _pY;
  int _width, _height;
  int _dirX, _dirY;
  float _speed;
  Boolean _exists = true;

  Projectile(int power, float x, float y, int w, int h, int dirX, int dirY, float speed) {
    _power = power;
    _pX = x;
    _pY = y;
    _width = w;
    _height = h;
    _dirX = dirX;
    _dirY = dirY;
    _speed = speed;
    place();
    $bulletNo++;
    $sounds[1].rewind();
    $sounds[1].play();
  }

  void update() {
    if (!_exists) {
      return;
    }
    _pX += _dirX * _speed;
    _pY += _dirY * _speed;
    if (_pX < $bannerWidth || _pX > width || _pY < 0 || _pY > height) {
      delete();
    }
    Tile[][] tBounds = $tileMap.getBounds(_pX, _pY, _width, _height);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (tBounds[i][j].isBlock()) {
          delete();
        }
      }
    }
    place();
    for (Enemy e : $enemies) {
      if (e._exists && ((_pX + (_width)) > e._x && (_pX) < e._x + (e._width)) && ((_pY + (_height)) > e._y && (_pY) < e._y + (e._height))) {
        delete();
        e.damage(_power);
        $score += 10;
        return;
      }
    }
  }

  void delete() {
    if (_exists) {
      _exists = false;
      $bulletNo--;
      $bullets.remove(this);
    }
  }

  void place() {
    fill(255, 0, 0);
    ellipse(_pX, _pY, _width, _height);
  }
}
