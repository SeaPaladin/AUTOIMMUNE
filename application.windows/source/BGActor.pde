/**
 * class for floating things in background
*/
class BGActor {
  PImage _actor;
  int _w, _h;
  int _x, _y;
  int _dirX = 1;
  int _dirY = 1;

  BGActor(int x, int y, int w, int h) {
    _actor = $images.getBGActor();
    _x = x;
    _y = y;
    _w = w;
    _h = h;
    int a = rand(2);
    if (a < 1) {
      _dirX = -1;
    }
    a = rand(2);
    if (a < 1) {
      _dirY = -1;
    }
  }

  void place() {
    int a = rand(10);
    if (a == 0) {
      _dirX *= -1;
    }
    int b = rand(10);
    if (b == 0) {
      _dirY *= -1;
    }
    int c = rand(20);
    if (c == 0) {
      _w += 5;
    } else if (c == 1 && _w > 20) {
      _w -= 5;
    } else if (c == 2) {
      _h += 5;
    } else if (c == 3 && _h > 20) {
      _h -= 5;
    }
    _x += _dirX * rand(3);
    _y += _dirY * rand(3);
    if (_x - _w < $bannerWidth) {
      _x = width - _w;
    } else if (_x > width) {
      _x = $bannerWidth;
    }
    if (_y - _h < 0) {
      _y = height - _h;
    } else if (_y > height) {
      _y = 0;
    }
    image(_actor, _x, _y, _w, _h);
  }
}
