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
  void reset() {
    _done = false;
  }

  /**
   * starts the timer
  */
  Boolean start() {
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
  String end() {
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
  int getMillis() {
    if (_on) {
      return millis() - _start;
    }
    return _end - _start;
  }

  /**
   * makes time readable to humans
  */
  String readTime() {
    return getHour() + ":" + getMin() + ":" + getSec() + "." + getMil();
  }

  String getHour() {
    return "" + getMillis() / (60 * 60 * 1000);
  }

  String getMin() {
    int m = getMillis() / (60 * 1000) % 60;
    return ((m < 10) ? "0" : "") + m;
  }

  String getSec() {
    int s = getMillis() / 1000 % 60;
    return ((s < 10) ? "0" : "") + s;
  }

  String getMil() {
    int a = getMillis() % 1000;
    if (a < 10) {
      return "00" + a;
    } else if (a < 100) {
      return "0" + a;
    }
    return "" + a;
  }
  
  int totalSeconds() {
    return getMillis()/1000;
  }
}
