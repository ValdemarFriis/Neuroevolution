class DNA {


  float[] genes = new float[11];
  float fitness;
  float mutationVarians = 2;

  //Forbinder - Sensorer & Hjerne & Bil
  float varians             = 2; //hvor stor er variansen på de tilfældige vægte og bias
  Car bil;//                    = new Car();
  NeuralNetwork hjerne;//       = new NeuralNetwork(varians); 
  SensorSystem  sensorSystem;// = new SensorSystem();

  DNA(float v) {
    varians = v;
    bil = new Car();
    hjerne = new NeuralNetwork(varians);
    sensorSystem = new SensorSystem();
    
    for (int i=0; i<11; i++) {
      if (i<8) {
        genes[i]=hjerne.weights[i];
      } else {
        genes[i]=hjerne.biases[i-8];
      }
    }
  }

  void fitness() {

    if (sensorSystem.clockWiseRotationFrameCounter>0) {
      if (sensorSystem.whiteSensorFrameCount > 0) {
        fitness = 0;
      } else {
        fitness = sensorSystem.clockWiseRotationFrameCounter;
      }
    } else {
      fitness = 0;
    }
  }

  void mutate(float mutationRate) {
    for (int i=0; i<genes.length; i++) {
      if (random(1)<mutationRate) {
        genes[i] += random(-mutationVarians, mutationVarians);
      }
    }
  }

  DNA newDNA() {
    DNA child  = new DNA(varians);
    child.hjerne.weights = genes;
    return child;
  }
  
  void reset(){
    bil = new Car();
    sensorSystem = new SensorSystem();
  }

  void update() {
    //1.)opdtarer bil 
    bil.update();
    //2.)opdaterer sensorer    
    sensorSystem.updateSensorsignals(bil.pos, bil.vel);
    //3.)hjernen beregner hvor meget der skal drejes
    float turnAngle = 0;
    float x1 = int(sensorSystem.leftSensorSignal);
    float x2 = int(sensorSystem.frontSensorSignal);
    float x3 = int(sensorSystem.rightSensorSignal);    
    turnAngle = hjerne.getOutput(x1, x2, x3);    
    //4.)bilen drejes
    bil.turnCar(turnAngle);
  }

  void display() {
    bil.displayCar();
    sensorSystem.displaySensors();
  }
}
