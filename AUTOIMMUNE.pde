import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;

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
void reset() {
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
    text("↑", 180, 440);
    fill(127);
    if ($dir[1]) {
      fill(255);
    }
    text("←", 150, 458);
    fill(127);
    if ($dir[2]) {
      fill(255);
    }
    text("↓", 180, 480);
    fill(127);
    if ($dir[3]) {
      fill(255);
    }
    text("→", 198, 458);
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
int xVal() {
  if ($mov[1] && $mov[3]) {
    return $xPriority;
  }
  return $mov[1] ? -1 : ($mov[3] ? 1 : 0);
}

int yVal() {
  if ($mov[0] && $mov[2]) {
    return $yPriority;
  }
  return $mov[0] ? -1 : ($mov[2] ? 1 : 0);
}

int rand(int x) {
  return floor(random(x));
}

void paintBG() {
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

void finish(Boolean win) {
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
