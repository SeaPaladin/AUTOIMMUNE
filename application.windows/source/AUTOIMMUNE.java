import processing.core.*; 
import processing.xml.*; 

import ddf.minim.*; 
import ddf.minim.signals.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class AUTOIMMUNE extends PApplet {






/**
 * global variables -can be used by any class -please prefix with $ for
 * consistency
*/
Minim[] $minim;
AudioPlayer[] $music; // plays bg music
Minim[] $minim2;
AudioPlayer[] $sounds; // plays sound effects
Boolean[] $dir = new Boolean[4]; // array that tracks which arrow keys are hit
Boolean[] $mov = new Boolean[4]; // array that tracks movement
Boolean[] $action = new Boolean[2]; // array that tracks which of the other keys are hit
Images $images; // holds all game images
String $mode; // controls flow of the game
int $xPriority, $yPriority; // assigns priority if left+right or up+down is held
Cell $cell; // main character
TileMap $tileMap; // background map
TileMap $objMap; // objects on the map
int $rows, $cols; // amount of rows and columns per screen
int $rowMap, $colMap; // map index
int $rowMapInit, $colMapInit; // initial index of map you're on
int $tSize; // size of an individual tile
int $lim; // controlls scrolling speed on screen transitions
int $frame; // frame of scrolling
int $bannerWidth; // size of left banner
ArrayList<Enemy> $enemies;
ArrayList<Projectile> $bullets;
int $heartWidth;
LevelGenerator $lg;
int $bulletNo; // amount of bullets on screen
ArrayList<ArrayList<PImage>> $backgrounds;
ArrayList<BGActor> $bgActors;
Timer $gameTimer;
Timer $transTimer;
int $score;
int $topScore;
String $time;
Boolean $win;
int[] $bg = new int[3];
int[] $c;
int $rGrowth = 1;
int $gGrowth = 1;
int $bGrowth = -1;
int $cWidth = 150;
int $cHeight = 150;
int $enemyCountMax;
int $enemyCount;
int $enemyTotal;
int $roomTotal;
int $timeBonus;
int $compBonus;

/**
 * first thing ran before going to draw() -only initiate things that need to
 * be done once
*/
public void setup() {
  size(1024, 576); // aspect ratio
  $minim = new Minim[2];
  $music = new AudioPlayer[2];
  for (int i = 0; i < $minim.length; i++) {
    $minim[i] = new Minim(this);
    $music[i] = $minim[i].loadFile("sounds/music" + i + ".mp3");
  }
  $minim2 = new Minim[3];
  $sounds = new AudioPlayer[3];
  for (int i = 0; i < $minim2.length; i++) {
    $minim2[i] = new Minim(this);
    $sounds[i] = $minim2[i].loadFile("sounds/sound" + i + ".wav");
  }
  $images = new Images(); // initiate class to load all game images
  // change these if you want
  $rows = 12;
  $cols = 16;
  $tSize = 48;
  $bannerWidth = 256;
  $heartWidth = 40;
  $backgrounds = $images.getBG();
  textSize(30);
  for (int i = 0; i < 4; i++) {
    $mov[i] = false;
    $dir[i] = false;
  }
  $score = 0;
  $topScore = 0;
  reset();
}

/**
 * function that allows for resetting the game to be convenient set initial
 * values here
*/
public void reset() {
  $win = false;
  for (int i = 0; i < $music.length; i++) {
    $music[i].setGain(-7.0f);
    $music[i].pause();
  }
  $sounds[1].setGain(-7.0f);
  $frame = 0;
  $bulletNo = 0;
  $lg = new LevelGenerator();
  $enemies = new ArrayList<Enemy>();
  $bullets = new ArrayList<Projectile>();
  $rowMap = $colMap = 2;
  $rowMapInit = $colMapInit = 2;
  $cell = new Cell(617.5f, 265.5f, 34, 34, 6.0f, 10); // main character. if additional character in future, change the arguments for different stats
  $tileMap = new TileMap("tile");
  $objMap = new TileMap("object");
  $mode = "reset";
  for (int i = 0; i < 2; i++) {
    $action[i] = false;
  }
  for (int i = 0; i < 3; i++) {
    $bg[i] = rand(256);
  }
  $bgActors = new ArrayList<BGActor>();
  int actorNo = 10 + rand(20);
  for (int i = 0; i < actorNo; i++) {
    $bgActors.add(new BGActor($bannerWidth + rand(768), rand(height), 10 + rand(50), 10 + rand(50)));
  }
  if ($score > $topScore) {
    String[] input = new String[1];
    input[0] = $score + "";
    saveStrings("data/record.TXT", input);
  }
  $score = 0;
  String record = "record.TXT";
  File recordFile = new File(dataPath(record));
  if (recordFile.exists()) {
    String[] input = loadStrings(record);
    $topScore = Integer.parseInt(input[0]);
  } else {
    $topScore = 0;
  }
  $time = "";
  $gameTimer = new Timer(); // timer to time game completion
  $transTimer = new Timer(1); // timer used for transitions
  $c = new int[3];
  $c[0] = 0;
  $c[1] = 127;
  $c[2] = 255;
  $enemyCountMax = 0;
  $enemyCount = 0;
  $enemyTotal = 0;
  $roomTotal = 1;
  $timeBonus = 0;
  $compBonus = 0;
}

/**
 * main loop of game
*/
boolean flag = true;
public void draw() {
  if ($mode.equals("reset")) { // start at title screen
    flag = true;
    image($images.getTitle(), 0, 0);
    if ($action[0]) {
      $music[1].rewind();
      $music[1].loop();
      $mode = "play";
      $gameTimer.start();
      text("", 0, 0); // gives time to load text
    }
  } else if ($mode.equals("play") || $mode.equals("gameover")) {
    paintBG();
    if (flag) {
      $tileMap.updateBG();
      $objMap.updateBG();
    }
    flag = false;
    $tileMap.updateBG();
    if ($mode.equals("play")) {
      $cell.updateXY(xVal(), yVal());
      $cell.updateSprite();
    }
    for (int i = 0; i < $bullets.size(); i++) {
      if ($bullets.get(i)._exists) {
        $bullets.get(i).update();
      }
    }
    for (int i = 0; i < $enemies.size(); i++) {
      if ($enemies.get(i)._exists) {
        $enemies.get(i).update();
        $enemies.get(i).updateSprite();
      }
    }
  } else if ($mode.equals("scroll") && millis() / 40 % 2 != $lim) {
    $lim = millis() / 40 % 2;
    $cell.scroll();
    paintBG();
    $tileMap.scroll();
    $objMap.scroll();
    $cell.updateSprite();
    $frame++;
    flag = true;
  } else if ($mode.equals("trans")) {
    background(0);
    if (!$transTimer.start()) {
      $music[0].rewind();
      $music[0].loop();
      $rowMap = 5;
      $colMap = 5;
      $tileMap = new TileMap("tile");
      $objMap = new TileMap("object");
      $mode = "play";
    }
  }
  if (!$mode.equals("reset")) { // things to handle when the game isnt on the title screen
    if ($action[1]) {
      reset();
    }
    image($images.getBanner(), 0, 0);
    for (int i = 0; i < $cell.get_health(); i++) {
      image($images.getHeart(true), 12 + i % 5 * ($heartWidth + 8), 8 + i / 5 * ($heartWidth + 4));
    }
    for (int i = $cell.get_health(); i < $cell.getMaxHealth(); i++) {
      image($images.getHeart(false), 12 + i % 5 * ($heartWidth + 8), 8 + i / 5 * ($heartWidth + 4));
    }
    $lg.displayGrid();
    fill(127);
    if ($mov[0]) {
      fill(255);
    }
    text("W", 68, 440);
    fill(127);
    if ($mov[1]) {
      fill(255);
    }
    text("A", 40, 480);
    fill(127);
    if ($mov[2]) {
      fill(255);
    }
    text("S", 72, 480);
    fill(127);
    if ($mov[3]) {
      fill(255);
    }
    text("D", 98, 480);
    fill(127);
    if ($dir[0]) {
      fill(255);
    }
    text("\u2191", 180, 440);
    fill(127);
    if ($dir[1]) {
      fill(255);
    }
    text("\u2190", 150, 458);
    fill(127);
    if ($dir[2]) {
      fill(255);
    }
    text("\u2193", 180, 480);
    fill(127);
    if ($dir[3]) {
      fill(255);
    }
    text("\u2192", 198, 458);
    if (!$mode.equals("gameover")) {
      fill(255);
      text("Score: "+$score, 10, 120);
      if ($score > $topScore) {
        text("Top: "+$score, 10, 150);
      } else {
        text("Top: "+$topScore, 10, 150);
      }
      text($gameTimer.readTime(), 40, 540);
    } else {
      if (!$win) {
        image($images.getGameOver(), 0, 0);
        fill(255, 0, 0);
      } else {
        $c[0] += $rGrowth;
        if ($c[0] == 255 || $c[0] == 0) {
          $rGrowth *= -1;
        }
        $c[1] += $gGrowth;
        if ($c[1] == 255 || $c[1] == 0) {
          $gGrowth *= -1;
        }
        $c[2] += $bGrowth;
        if ($c[2] == 255 || $c[2] == 0) {
          $bGrowth *= -1;
        }
        tint($c[0], $c[1], $c[2]);
        image($images.getFin(), $bannerWidth, 0);
        image($images._cells.get(0).get(0), $bannerWidth+366, 132);
        textAlign(RIGHT);
        fill($c[0], $c[1], $c[2]);
        text($enemyTotal+"",$bannerWidth+724,244);
        text($roomTotal+"",$bannerWidth+724,289);
        text($timeBonus+"",$bannerWidth+724,335);
        text($compBonus+"",$bannerWidth+724,382);
        text("x"+$cell.get_health(),$bannerWidth+724,427);
        text($score+"",$bannerWidth+724,519);
        fill(0, 255, 0);
      }
      textAlign(LEFT);
      text("Score: "+$score, 10, 120);
      if ($score > $topScore) {
        text("Top: "+$score, 10, 150);
      } else {
        text("Top: "+$topScore, 10, 150);
      }
      text($time, 40, 540);
    }
  }
}

/**
 * handles key inputs
*/
public void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) {
      $dir[0] = true;
    }
    else if (keyCode == LEFT) {
      $dir[1] = true;
    }
    else if (keyCode == DOWN) {
      $dir[2] = true;
    }
    else if (keyCode == RIGHT) {
      $dir[3] = true;
    }
  } else {
    Character k = key;
    k = Character.toLowerCase(k);
    switch (k) {
    case 'w':
      $mov[0] = true;
      $yPriority = 1;
      break;
    case 'a':
      $mov[1] = true;
      $xPriority = -1;
      break;
    case 's':
      $mov[2] = true;
      $yPriority = -1;
      break;
    case 'd':
      $mov[3] = true;
      $xPriority = 1;
      break;
    case ' ':
      $action[0] = true;
      break;
    case '\\':
      $action[1] = true;
      break;
    }
  }
}

public void keyReleased() {
  if (key == CODED) {
    if (keyCode == UP) {
      $dir[0] = false;
      if ($bulletNo < 3 && $mode.equals("play")) {
        $bullets.add(new Projectile(2, $cell.getX() + $cell.getW() / 2, $cell.getY(), 10, 10, 0, -1, 12.0f));
      }
    } else if (keyCode == LEFT) {
      $dir[1] = false;
      if ($bulletNo < 3 && $mode.equals("play")) {
        $bullets.add(new Projectile(2, $cell.getX(), $cell.getY() + $cell.getH() / 2, 10, 10, -1, 0, 12.0f));
      }
    } else if (keyCode == DOWN) {
      $dir[2] = false;
      if ($bulletNo < 3 && $mode.equals("play")) {
        $bullets.add(new Projectile(2, $cell.getX() + $cell.getW() / 2, $cell.getY() + $cell.getH(), 10, 10, 0, 1, 12.0f));
      }
    } else if (keyCode == RIGHT) {
      $dir[3] = false;
      if ($bulletNo < 3 && $mode.equals("play")) {
        $bullets.add(new Projectile(2, $cell.getX() + $cell.getW(), $cell.getY() + $cell.getH() / 2, 10, 10, 1, 0, 12.0f));
      }
    }
  } else {
    Character k = key;
    k = Character.toLowerCase(k);
    switch (k) {
    case 'w':
      $mov[0] = false;
      break;
    case 'a':
      $mov[1] = false;
      break;
    case 's':
      $mov[2] = false;
      break;
    case 'd':
      $mov[3] = false;
      break;
    case ' ':
      $action[0] = false;
      break;
    case '\\':
      $action[1] = false;
      break;
    }
  }
}

/**
 * returns direction to move in, in instance of conflicting keys
*/
public int xVal() {
  if ($mov[1] && $mov[3]) {
    return $xPriority;
  }
  return $mov[1] ? -1 : ($mov[3] ? 1 : 0);
}

public int yVal() {
  if ($mov[0] && $mov[2]) {
    return $yPriority;
  }
  return $mov[0] ? -1 : ($mov[2] ? 1 : 0);
}

public int rand(int x) {
  return floor(random(x));
}

public void paintBG() {
  tint($c[0], $c[1], $c[2]);
  $c[0] += $rGrowth;
  if ($c[0] == 255 || $c[0] == 0) {
    $rGrowth *= -1;
  }
  $c[1] += $gGrowth;
  if ($c[1] == 255 || $c[1] == 0) {
    $gGrowth *= -1;
  }
  $c[2] += $bGrowth;
  if ($c[2] == 255 || $c[2] == 0) {
    $bGrowth *= -1;
  }
  image($backgrounds.get(0).get(
      millis() / 200 % $backgrounds.get(0).size()), 0, 0);
  for (int i = 0; i < $bgActors.size(); i++) {
    $bgActors.get(i).place();
  }
  noTint();
}

public void finish(Boolean win) {
  $win = win;
  if (win) {
    $timeBonus = 10*(600-$gameTimer.totalSeconds());
    if ($timeBonus < 0) {
      $timeBonus = 0;
    }
    $score += $timeBonus;
    if ($roomTotal == 25) {
      $compBonus = 10000;
      $score += 10000;
    }
    $score *= $cell.get_health();
  }
  $music[0].pause();
  $music[1].pause();
  if ($time == "") {
    $time = $gameTimer.readTime();
  }
  if ($score > $topScore) {
    String[] input = new String[1];
    input[0] = $score + "";
    saveStrings("data/record.TXT", input);
  }
  $mode = "gameover";
}

public void stop() {
  for (int i = 0; i < $minim.length; i++) {
    $minim[i].stop();
    $music[i].close();
  }
  for (int i = 0; i < $minim2.length; i++) {
    $minim2[i].stop();
    $sounds[i].close();
  }
  super.stop();
}
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

  public void place() {
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

  public void updateSprite() {
    image(_sprites.get(_index).get(animate(_index, 200)), _x, _y);
  }

  public int animate(int sprite, int speed) {
    if (_moving) {
      _last = millis() / speed % _sprites.get(sprite).size();
    }
    return _last;
  }

  /**
   * move the cell. account for if a move is legal as well
   */
  public void updateXY(int xDir, int yDir) {
    if (xDir == 0 && yDir == 0) {
      _moving = false;
      _speed = 0;
      for (Enemy e : $enemies) {
        if (e._exists
            && ((_x + (_width)) > e._x && (_x) < e._x + (e._width))
            && ((_y + (_height)) > e._y && (_y) < e._y
                + (e._height))) {
          _health -= e.getPower();
          if (_health <= 0) {
            finish(false);
          }
          if (e.getIndex() == 0) {
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

  public void erase() {
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
  public void scroll() {
    int deltaX = $colMap - $colMapInit;
    int deltaY = $rowMap - $rowMapInit;
    if ($frame == 0) {
      _x += deltaX * $tSize;
      _y += deltaY * $tSize;
    }
    _x -= deltaX * $tSize;
    _y -= deltaY * $tSize;
  }

  public float getX() {
    return _x;
  }

  public float getY() {
    return _y;
  }

  public int getW() {
    return _width;
  }

  public int getH() {
    return _height;
  }

  public int get_health() {
    return _health;
  }

  public int getMaxHealth() {
    return _maxHealth;
  }
}
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
    _accel = (float) 0.1f;
    _sprites = $images.getEnemySprites();
    _power = power;
    _health = health;
    _row = row;
    _col = col;
  }

  public void updateSprite() {
    if (!_exists) {
      return;
    }
    image(_sprites.get(_index).get(animate(_index, 200)), _x, _y, _width, _height);
  }

  public int animate(int sprite, int speed) {
    if (_moving) {
      _last = millis() / speed % _sprites.get(sprite).size();
    }
    return _last;
  }

  public int getPower() {
    return _power;
  }

  public int getIndex() {
    return _index;
  }

  public void damage(int power) {
    _health -= power;
    $sounds[0].rewind();
    $sounds[0].play();
    if (_health <= 0) {
      delete();
    }
  }

  public void delete() {
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

  public void update() {
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
  public void updateXY(int xDir, int yDir) {
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

  public PImage getTitle() {
    return _title;
  }

  public PImage getFin() {
    return _fin;
  }

  public PImage getBGActor() {
    return _bgActor;
  }

  public PImage getGameOver() {
    return _gameOver;
  }

  public PImage getBanner() {
    return _banner;
  }

  public PImage getHeart(Boolean x) {
    if (x) {
      return _hearttrue;
    }
    return _heartfalse;
  }

  public ArrayList<ArrayList<PImage>> getCellSprites() {
    return _cells;
  }

  public ArrayList<ArrayList<PImage>> getEnemySprites() {
    return _enemies;
  }

  public ArrayList<ArrayList<PImage>> getBG() {
    return _backgrounds;
  }

  public ArrayList<PImage> getSprites(String type, char index) {
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

  public void createGrid() {
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

  public void displayGrid() {
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

  public void setExplored(int i, int j) {
    if (_mapGrid[i][j] == 0) {
      _mapGrid[i][j] = 1;
    }
  }
  
  public void setComplete(int i, int j) {
    if (i < 5 && j < 5 && _mapGrid[i][j] != 2) { 
      _mapGrid[i][j] = 2;
      $sounds[2].rewind();
      $sounds[2].play();
      $roomTotal++;
      $score += 200;
    }
  }
}
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

  public void update() {
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

  public void delete() {
    if (_exists) {
      _exists = false;
      $bulletNo--;
      $bullets.remove(this);
    }
  }

  public void place() {
    fill(255, 0, 0);
    ellipse(_pX, _pY, _width, _height);
  }
}
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

  public char index() {
    return _index;
  }

  public void paint(int x, int y) {
    int a = animate();
    image(_frame.get(a), x, y);
    noTint();
  }

  public int animate() {
    return (millis() / _speed % _frame.size());
  }

  public Boolean isBlock() {
    if (_type == "tile") {
      return _index == '1';
    }
    return false;
  }

  public Boolean isTrans() {
    if (_type == "tile") {
      return _index == '2';
    }
    return false;
  }
}
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
  public Tile[][] getTiles() {
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

  public void updateBG() {
    for (int i = 0; i < $rows; i++) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j].paint(j * $tSize + $bannerWidth, i * $tSize);
      }
    }
  }

  public void replace(int row, int col, char replace) {
    String line = _tileMap[row].substring(0, col);
    line += replace + _tileMap[row].substring(col + 1);
    _tileMap[row] = line;
    saveStrings("data/maps/" + _type + "Map" + $rowMap + "," + $colMap + ".map", _tileMap);
  }

  public void scroll() {
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

  public void scrollLeft() {
    for (int i = $cols - 1; i > 0; i--) {
      for (int j = 0; j < $rows; j++) {
        _tiles[j][i] = _tiles[j][i - 1];
      }
    }
    for (int i = 0; i < $rows; i++) {
      _tiles[i][0] = _tiles2[i][$cols - 1 - $frame];
    }
  }

  public void scrollRight() {
    for (int i = 0; i < $cols - 1; i++) {
      for (int j = 0; j < $rows; j++) {
        _tiles[j][i] = _tiles[j][i + 1];
      }
    }
    for (int i = 0; i < $rows; i++) {
      _tiles[i][$cols - 1] = _tiles2[i][$frame];
    }
  }

  public void scrollUp() {
    for (int i = $rows - 1; i > 0; i--) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j] = _tiles[i - 1][j];
      }
    }
    for (int i = 0; i < $cols; i++) {
      _tiles[0][i] = _tiles2[$rows - 1 - $frame][i];
    }
  }

  public void scrollDown() {
    for (int i = 0; i < $rows - 1; i++) {
      for (int j = 0; j < $cols; j++) {
        _tiles[i][j] = _tiles[i + 1][j];
      }
    }
    for (int i = 0; i < $cols; i++) {
      _tiles[$rows - 1][i] = _tiles2[$frame][i];
    }
  }

  public Tile[][] getBounds(float x, float y, float w, float h) {
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
/**
 * class for timing things
*/
class Timer {
  int _start, _end;
  float _stop;
  Boolean _on = false;
  Boolean _done = false;

  Timer() {
  }

  Timer(float stop) {
    _stop = stop;
  }

  /**
   * gets state of timer
  */
  public void reset() {
    _done = false;
  }

  /**
   * starts the timer
  */
  public Boolean start() {
    if (!_on) {
      _start = millis();
      _on = true;
    } else if ((millis() - _start) / 1000 >= _stop) {
      _end = millis();
      _on = false;
      _done = true;
    }
    return !_done;
  }

  /**
   * ends the timer and returns time passed
  */
  public String end() {
    if (!_done) {
      _end = getMillis();
      _on = false;
      _done = true;
    }
    return readTime();
  }

  /**
   * time passed in milliseconds
  */
  public int getMillis() {
    if (_on) {
      return millis() - _start;
    }
    return _end - _start;
  }

  /**
   * makes time readable to humans
  */
  public String readTime() {
    return getHour() + ":" + getMin() + ":" + getSec() + "." + getMil();
  }

  public String getHour() {
    return "" + getMillis() / (60 * 60 * 1000);
  }

  public String getMin() {
    int m = getMillis() / (60 * 1000) % 60;
    return ((m < 10) ? "0" : "") + m;
  }

  public String getSec() {
    int s = getMillis() / 1000 % 60;
    return ((s < 10) ? "0" : "") + s;
  }

  public String getMil() {
    int a = getMillis() % 1000;
    if (a < 10) {
      return "00" + a;
    } else if (a < 100) {
      return "0" + a;
    }
    return "" + a;
  }
  
  public int totalSeconds() {
    return getMillis()/1000;
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "AUTOIMMUNE" });
  }
}
