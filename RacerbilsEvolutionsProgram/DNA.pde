class DNA {


  float[] genes = new float[11];
  float fitness;
  float mutationVarians = 2;

  //Forbinder - Sensorer & Hjerne & Bil
  float varians             = 2; //hvor stor er variansen på de tilfældige vægte og bias
  Car bil                    = new Car();
  NeuralNetwork hjerne       = new NeuralNetwork(varians); 
  SensorSystem  sensorSystem = new SensorSystem();

  DNA() {
    for (int i=0; i<11; i++) {
      if (i<8) {
        genes[i]=hjerne.weights[i];
      } else {
        genes[i]=hjerne.biases[i-8];
      }
    }
  }

  void fitness() {
    println(sensorSystem.clockWiseRotationFrameCounter);

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
        genes[i] = random(-mutationVarians, mutationVarians);
      }
    }
  }

  DNA crossover(DNA partner) {
    DNA child = new DNA();

    int midpoint = int(random(genes.length));

    for (int i=0; i < genes.length; i++) {
      if (i > midpoint) child.genes[i] = genes[i];
      else child.genes[i] = partner.genes[i];
    }
    return child;
  }

  DNA newDNA() {
    DNA child  = new DNA();
    hjerne.weights = genes;
    return child;
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
