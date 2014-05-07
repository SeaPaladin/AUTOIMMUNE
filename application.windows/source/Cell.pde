/**
 * class for the white blood cell
*/
class Cell {
  ArrayList<ArrayList<PImage>> _sprites; // sprites for the cell
  int _index; // sprite index
  int _width, _height; // width and height
  float _x, _y; // x and y position
  float _speed; // current speed
  float _maxSpeed; // maximum speed
  float _accel; // acceleration
  Boolean _moving = false; // checks if cell is idle or moving
  int _last = 0; // last animated frame
  int _maxHealth; // max amount of health cell has
  int _health; // current amount of health cell has

  Cell(float x, float y, int w, int h, float speed, int maxHealth) {
    _index = 0;
    _width = w;
    _height = h;
    _x = x;
    _y = y;
    _speed = 0;
    _maxSpeed = speed;
    _accel = 0.2f;
    _health = maxHealth;
    _maxHealth = maxHealth;
    _sprites = $images.getCellSprites();
  }

  void updateSprite() {
    image(_sprites.get(_index).get(animate(_index, 200)), _x, _y);
  }

  int animate(int sprite, int speed) {
    if (_moving) {
      _last = millis() / speed % _sprites.get(sprite).size();
    }
    return _last;
  }

  /**
   * move the cell. account for if a move is legal as well
   */
  void updateXY(int xDir, int yDir) {
    if (xDir == 0 && yDir == 0) {
      _moving = false;
      _speed = 0;
      for (Enemy e : $enemies) {
        if (e._exists && ((_x + (_width)) > e._x && (_x) < e._x + (e._width)) && ((_y + (_height)) > e._y && (_y) < e._y + (e._height))) {
          _health -= e.getPower();
          if (_health <= 0) {
            finish(false);
          }
          if (e.getIndex() != 1) {
            e.delete();
            $sounds[0].rewind();
            $sounds[0].play();
          }
          return;
        }
      }
      return;
      
    } else {
      if (_speed < _maxSpeed) {
        _speed += _accel;
      }
      if (_speed > _maxSpeed) {
        _speed = _maxSpeed;
      }
      _moving = true;
    }
    float nextX = _x + xDir * _speed, nextY = _y + yDir * _speed;
    if (yDir != 0) {
      nextX = _x + xDir * sqrt(sq(_speed) / 2);
    }
    if (xDir != 0) {
      nextY = _y + yDir * sqrt(sq(_speed) / 2);
    }
    Tile[][] tBoundsX = $tileMap.getBounds(nextX, _y, _width, _height);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (tBoundsX[i][j].isBlock()) {
          nextX = _x;
        }
      }
    }
    Tile[][] tBounds = $tileMap.getBounds(nextX, nextY, _width, _height);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (tBounds[i][j].isBlock()) {
          nextY = _y;
        } else if (tBounds[i][j].isTrans()) {
          $music[1].pause();
          $bullets.clear();
          $bulletNo = 0;
          $mode = "trans";
        }
      }
    }
    if (nextX < $bannerWidth) {
      $colMap--;
      erase();
    } else if (nextX + _width - 1 >= width) {
      $colMap++;
      erase();
    } else if (nextY < 0) {
      $rowMap--;
      erase();
    } else if (nextY + _height - 1 >= $rows * $tSize) {
      $rowMap++;
      erase();
    }
    _x = nextX;
    _y = nextY;
    for (Enemy e : $enemies) {
      if (e._exists && ((_x + (_width)) > e._x && (_x) < e._x + (e._width)) && ((_y + (_height)) > e._y && (_y) < e._y + (e._height))) {
        _health -= e.getPower();
        if (_health <= 0) {
          finish(false);
        }
        if (e.getIndex() != 1) {
          e.delete();
          $sounds[0].rewind();
          $sounds[0].play();
        }
        return;
      }
    }
  }

  void erase() {
    $enemyCount = 0;
    $enemyCountMax = 0;
    $enemies.clear();
    $bullets.clear();
    $bulletNo = 0;
    $mode = "scroll";
  }

  /**
   * scroll the cell with the maps
  */
  void scroll() {
    int deltaX = $colMap - $colMapInit;
    int deltaY = $rowMap - $rowMapInit;
    if ($frame == 0) {
      _x += deltaX * $tSize;
      _y += deltaY * $tSize;
    }
    _x -= deltaX * $tSize;
    _y -= deltaY * $tSize;
  }

  float getX() {
    return _x;
  }

  float getY() {
    return _y;
  }

  int getW() {
    return _width;
  }

  int getH() {
    return _height;
  }

  public int get_health() {
    return _health;
  }

  int getMaxHealth() {
    return _maxHealth;
  }
}
