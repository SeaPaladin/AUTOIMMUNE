/**
 * class for the Enemy cell
*/
class Enemy {
  ArrayList<ArrayList<PImage>> _sprites; // sprites for the cell
  int _index; // sprite index
  int _width, _height; // width and height
  public float _x, _y; // x and y position
  float _speed; // current speed
  float _maxSpeed; // maximum speed
  float _accel; // acceleration
  Boolean _moving = false; // checks if cell is idle or moving
  int _last = 0; // last animated frame
  int _swapX = 1;
  int _swapY = 1;
  Boolean _exists = true;
  int _power;
  int _row, _col;
  int _health;

  Enemy(int x, int y, int w, int h, float speed, int power, int health, int row, int col, int index) {
    _index = index;
    _width = w;
    _height = h;
    _x = x;
    _y = y;
    _speed = 0;
    _maxSpeed = speed;
    _accel = (float) 0.1;
    _sprites = $images.getEnemySprites();
    _power = power;
    _health = health;
    _row = row;
    _col = col;
  }

  void updateSprite() {
    if (!_exists) {
      return;
    }
    image(_sprites.get(_index).get(animate(_index, 200)), _x, _y, _width, _height);
  }

  int animate(int sprite, int speed) {
    if (_moving) {
      _last = millis() / speed % _sprites.get(sprite).size();
    }
    return _last;
  }

  int getPower() {
    return _power;
  }

  int getIndex() {
    return _index;
  }

  void damage(int power) {
    _health -= power;
    $sounds[0].rewind();
    $sounds[0].play();
    if (_health <= 0) {
      delete();
    }
  }

  void delete() {
    if (_exists) {
      _exists = false;
      $enemyCount++;
      $enemyTotal++;
      if ($enemyCount >= $enemyCountMax) {
        $lg.setComplete($colMap,$rowMap);
      }
      if (_index == 0 || _index == 2) {
        $objMap.replace(_row, _col, '0');
      } else if (_index == 1) {
        finish(true);
      }
    }
  }

  void update() {
    if (_index == 0) {
      updateXY(1, 0);
      updateXY(0, 1);
    } else if (_index == 1) {
      updateXY(1, 0);
      updateXY(0, 1);
    } else if (_index == 2) {
      if (rand(2) == 0) {
        updateXY(1,0);
      } else {
        updateXY(0,1);
      }
    }
  }

  /**
   * move the enemy. account for if a move is legal as well
  */
  void updateXY(int xDir, int yDir) {
    if (!_exists) {
      return;
    }
    if (xDir == 0 && yDir == 0) {
      _moving = false;
      _speed = 0;
    } else {
      if (_speed < _maxSpeed) {
        _speed += _accel;
      }
      if (_speed > _maxSpeed) {
        _speed = _maxSpeed;
      }
      _moving = true;
    }
    float nextX = _x + (xDir * _speed * _swapX), nextY = _y
        + (yDir * _speed * _swapY);
    if (yDir != 0) {
      nextX = _x + xDir * sqrt(sq(_speed) / 2);
    }
    if (xDir != 0) {
      nextY = _y + yDir * sqrt(sq(_speed) / 2);
    }
    Tile[][] tBoundsX = $tileMap.getBounds(nextX, _y, _width, _height);
    boolean flag = false;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (tBoundsX[i][j].isBlock()) {
          nextX = _x;
          _swapX = _swapX * -1;
          flag = true;
          break;
        }
      }
      if (flag)
        break;
    }
    Tile[][] tBoundsY = $tileMap.getBounds(nextX, nextY, _width, _height);
    flag = false;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (tBoundsY[i][j].isBlock()) {
          nextY = _y;
          _swapY = _swapY * -1;
          flag = true;
          break;
        }
      }
      if (flag)
        break;
    }
    if (nextX < $bannerWidth) {
      _swapX = _swapX * -1;
      nextX = _x + (xDir * _speed * _swapX);
    } else if (nextX + _width - 1 >= width) {
      _swapX = _swapX * -1;
      nextX = _x + (xDir * _speed * _swapX);
    } else if (nextY < 0) {
      _swapY = _swapY * -1;
      nextY = _y + (yDir * _speed * _swapY);
    } else if (nextY + _height - 1 >= $rows * $tSize) {
      _swapY = _swapY * -1;
      nextY = _y + (yDir * _speed * _swapY);
    }
    _x = nextX;
    _y = nextY;
  }
}
