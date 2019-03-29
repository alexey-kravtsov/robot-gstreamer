const int MESSAGE_HEADER_SIZE_BYTES = 3;
const int MAX_MESSAGE_SIZE_BYTES = 30 - MESSAGE_HEADER_SIZE_BYTES;
const byte PROTOCOL_VERSION = 1;

byte headerBuffer[MESSAGE_HEADER_SIZE_BYTES];
byte payloadBuffer[MAX_MESSAGE_SIZE_BYTES];

void setup() {
  init_driver();

  pinMode(13, OUTPUT);
  Serial.begin(115200);
}

void loop() {
  if (Serial.available() > 0) {
    int marker = Serial.read();
    while (marker != 0) {
      marker = Serial.read();
    }

    Serial.readBytes(headerBuffer, MESSAGE_HEADER_SIZE_BYTES);
    
    byte len = headerBuffer[0];

    if (len > MAX_MESSAGE_SIZE_BYTES) {
      return;
    }
    
    byte ver = headerBuffer[1];
    byte commandName = headerBuffer[2];

    if (ver != PROTOCOL_VERSION) {
      return;
    }

    len -= (MESSAGE_HEADER_SIZE_BYTES + 1);
    Serial.readBytes(payloadBuffer, len);

    if (commandName == 'm') {
      move(payloadBuffer, len);
    }
  }
}
