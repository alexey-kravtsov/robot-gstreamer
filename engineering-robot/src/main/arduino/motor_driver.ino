const int SLP = 4;

const int LF = 5;
const int LB = 3;
const int RF = 11;
const int RB = 6;

const int SPEED_STEP = 120;

const unsigned long MAX_ULONG = 4294967295;

unsigned long last_command_time = 0;
bool watchdog_enabled = false;

void init_driver() {
  pinMode(SLP, OUTPUT);

  pinMode(LF, OUTPUT);
  pinMode(LB, OUTPUT);
  pinMode(RF, OUTPUT);
  pinMode(RB, OUTPUT);

  stop();

  cli();

  TCCR1A = 0;// set entire TCCR1A register to 0
  TCCR1B = 0;// same for TCCR1B
  TCNT1  = 0;//initialize counter value to 0
  // set compare match register for 1hz increments
  OCR1A = 1563;// = (16*10^6) / (1*1024) - 1 (must be <65536)
  // turn on CTC mode
  TCCR1B |= (1 << WGM12);
  // Set CS10 and CS12 bits for 1024 prescaler
  TCCR1B |= (1 << CS12) | (1 << CS10);  
  // enable timer compare interrupt
  TIMSK1 |= (1 << OCIE1A);

  sei();
}

ISR(TIMER1_COMPA_vect) {
  if (!watchdog_enabled) {
    return;
  }
  
  unsigned long current_time = millis();
  unsigned long diff = 0;
  if (current_time < last_command_time) { //overflow
    diff += MAX_ULONG - last_command_time;
    diff += current_time;
  } else {
    diff = current_time - last_command_time;
  }

  if (diff >= 400) {
    stop();
  }
}

void move(byte data[], byte len) {
  if (len != 1 && len != 4) {
    return;
  }

  byte command = data[0];
  
  if (command == 'p') {
    stop();
    return;
  }

  char m_direction = data[1];
  char m_rotation = data[2];
  int m_speed = data[3];

  if (m_direction != 'f' && m_direction != 'b' && m_direction != 'n') {
    return;
  }

  if (m_rotation != 'l' && m_rotation != 'r' && m_rotation != 'n') {
    return;
  }

  if (m_speed < 0 || m_speed > 3) {
    return;
  }
      
  int speedL = 0;
  int speedR = 0;

  if (m_direction == 'n') {
    speedL = 0;
    speedR = 0;
  } else if (m_direction == 'f') {
    speedL = m_speed;
    speedR = m_speed;
  } else if (m_direction == 'b') {
    speedL = -m_speed;
    speedR = -m_speed;
  }

  if (m_rotation == 'l') {
    if (speedL == 0 && speedR == 0) {
      speedR = 2;
      speedL = -2;
    } else {
      int sign = speedL / abs(speedL);
      speedL = sign * (abs(speedL) - 1);
    }
  } else if (m_rotation == 'r') {
    if (speedL == 0 && speedR == 0) {
      speedR = -2;
      speedL = 2;
    } else {
      int sign = speedR / abs(speedR);
      speedR = sign * (abs(speedR) - 1);
    }
  }

  if (speedL == 0 && speedR == 0) {
    stop();
    return;
  }
  
  if (command == 's') {
    start_move(speedL, speedR);
    return;
  } else if (command == 'c') {
    continue_move(speedL, speedR);
    return;
  }
}

void start_move(int speedL, int speedR) {
  if (watchdog_enabled) {
    return;
  }
  
  last_command_time = millis();
  watchdog_enabled = true;
  digitalWrite(SLP, HIGH);
  
  continue_move(speedL, speedR);
}

void continue_move(int speedL, int speedR) {
  last_command_time = millis();
  if (!watchdog_enabled) {
    return;
  }

  set_speed(LF, LB, speedL);
  set_speed(RF, RB, speedR);
}

void set_speed(int F, int B, int sp) {
//  Serial.println(sp);
  if (sp >= 0) {
    analogWrite(F, SPEED_STEP * sp);
    analogWrite(B, 0);
  }

  if (sp < 0) {
    analogWrite(F, 0);
    analogWrite(B, SPEED_STEP * abs(sp));
  }
}

void stop() {
  last_command_time = millis();
  watchdog_enabled = false;
  digitalWrite(SLP, LOW);

  analogWrite(LF, 0);
  analogWrite(LB, 0);
  analogWrite(RF, 0);
  analogWrite(RB, 0);
}
