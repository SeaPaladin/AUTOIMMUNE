/**
 * class to load and get game images
*/
class Images {
  PImage _title; // title screen background image
  PImage _fin; // the end
  PImage _bgActor; // bg actor image
  PImage _gameOver; // game over screen
  PImage _banner; // left side banner for UI
  PImage _hearttrue;
  PImage _heartfalse;
  ArrayList<ArrayList<PImage>> _backgrounds;
  ArrayList<ArrayList<PImage>> _cells; // white blood cell sprites
  ArrayList<ArrayList<PImage>> _enemies;
  ArrayList<ArrayList<PImage>> _tiles;
  ArrayList<ArrayList<PImage>> _objects;

  Images() {
    _title = loadImage("images/title.png");
    _fin = loadImage("images/fin.png");
    _bgActor = loadImage("images/bgActor0a.png");
    _gameOver = loadImage("images/gameOver.png");
    _banner = loadImage("images/banner.png");
    _hearttrue = loadImage("images/hearttrue.png");
    _heartfalse = loadImage("images/heartfalse.png");

    _cells = new ArrayList<ArrayList<PImage>>();
    String fileName = "images/sprite0a.png";
    File file = new File(dataPath(fileName));
    int i = 0;
    char c = 'a';
    while (file.exists()) {
      _cells.add(new ArrayList<PImage>());
      while (file.exists()) {
        _cells.get(i).add(loadImage(fileName));
        c++;
        fileName = "images/sprite" + i + c + ".png";
        file = new File(dataPath(fileName));
      }
      c = 'a';
      i++;
      fileName = "images/sprite" + i + c + ".png";
      file = new File(dataPath(fileName));
    }

    _enemies = new ArrayList<ArrayList<PImage>>();
    fileName = "images/enemy0a.png";
    file = new File(dataPath(fileName));
    i = 0;
    c = 'a';
    while (file.exists()) {
      _enemies.add(new ArrayList<PImage>());
      while (file.exists()) {
        _enemies.get(i).add(loadImage(fileName));
        c++;
        fileName = "images/enemy" + i + c + ".png";
        file = new File(dataPath(fileName));
      }
      c = 'a';
      i++;
      fileName = "images/enemy" + i + c + ".png";
      file = new File(dataPath(fileName));
    }

    _backgrounds = new ArrayList<ArrayList<PImage>>();
    fileName = "images/bg0a.png";
    file = new File(dataPath(fileName));
    i = 0;
    c = 'a';
    while (file.exists()) {
      _backgrounds.add(new ArrayList<PImage>());
      while (file.exists()) {
        _backgrounds.get(i).add(loadImage(fileName));
        c++;
        fileName = "images/bg" + i + c + ".png";
        file = new File(dataPath(fileName));
      }
      c = 'a';
      i++;
      fileName = "images/bg" + i + c + ".png";
      file = new File(dataPath(fileName));
    }

    _tiles = new ArrayList<ArrayList<PImage>>();
    fileName = "images/tile0a.png";
    file = new File(dataPath(fileName));
    i = 0;
    c = 'a';
    while (file.exists()) {
      _tiles.add(new ArrayList<PImage>());
      while (file.exists()) {
        _tiles.get(i).add(loadImage(fileName));
        c++;
        fileName = "images/tile" + i + c + ".png";
        file = new File(dataPath(fileName));
      }
      c = 'a';
      i++;
      fileName = "images/tile" + i + c + ".png";
      file = new File(dataPath(fileName));
    }

    _objects = new ArrayList<ArrayList<PImage>>();
    fileName = "images/object0a.png";
    file = new File(dataPath(fileName));
    i = 0;
    c = 'a';
    while (file.exists()) {
      _objects.add(new ArrayList<PImage>());
      while (file.exists()) {
        _objects.get(i).add(loadImage(fileName));
        c++;
        fileName = "images/object" + i + c + ".png";
        file = new File(dataPath(fileName));
      }
      c = 'a';
      i++;
      fileName = "images/object" + i + c + ".png";
      file = new File(dataPath(fileName));
    }
  }

  PImage getTitle() {
    return _title;
  }

  PImage getFin() {
    return _fin;
  }

  PImage getBGActor() {
    return _bgActor;
  }

  PImage getGameOver() {
    return _gameOver;
  }

  PImage getBanner() {
    return _banner;
  }

  PImage getHeart(Boolean x) {
    if (x) {
      return _hearttrue;
    }
    return _heartfalse;
  }

  ArrayList<ArrayList<PImage>> getCellSprites() {
    return _cells;
  }

  ArrayList<ArrayList<PImage>> getEnemySprites() {
    return _enemies;
  }

  ArrayList<ArrayList<PImage>> getBG() {
    return _backgrounds;
  }

  ArrayList<PImage> getSprites(String type, char index) {
    int i;
    if (index >= 'A') {
      i = index - 65;
    } else {
      i = index - 48;
    }
    if (type == "tile") {
      return _tiles.get(i);
    }
    return _objects.get(i);
  }
}
