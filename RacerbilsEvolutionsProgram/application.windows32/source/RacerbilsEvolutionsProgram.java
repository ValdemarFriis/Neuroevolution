import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class RacerbilsEvolutionsProgram extends PApplet {

//populationSize: Hvor mange "controllere" der genereres, controller = bil & hjerne & sensorer
int       populationSize  = 1000;     
int timer = 0;
float simulationTimer = 10;
int bestFitness;
int generations = 1;

boolean showCensors = false;

//CarSystem: Indholder en population af "controllere" 
CarSystem carSystem       = new CarSystem(populationSize);

//trackImage: RacerBanen , Vejen=sort, Udenfor=hvid, Målstreg= 100%grøn 
PImage    trackImage;

public void setup() {
  
  trackImage = loadImage("track.png");
}

public void draw() {
  clear();
  background(255);
  fill(0);
  rect(0, 0, 1000, 80);
  image(trackImage, 0, 80);  

  carSystem.calcFitness();
  carSystem.naturalSelection();
  carSystem.run();
  simulate();

  textSize(24);
  fill(255);
  text("Time Elapsed: " + PApplet.parseInt(millis()/1000), 0, 24);
  text("Top Fitness: " + PApplet.parseInt(carSystem.topFitness), 220, 24);
  text("Max Fitness: " + bestFitness, 0, 50);
  text("generation: " + generations, 220, 50);
  fill(0);
  text("Press 'enter' to toggle censors", 80, height-24);
}

public void simulate() {
  if (millis() > timer+simulationTimer*1000) {
    timer = millis();

    if (bestFitness < carSystem.topFitness) bestFitness = PApplet.parseInt(carSystem.topFitness);

    carSystem.generate();
    for (int i = 0; i < populationSize; i++) {
      carSystem.population[i].reset();            
    }
    generations++;
  }
}

public void keyPressed(){
  if (keyCode == (int)ENTER){
    showCensors = !showCensors;
  }
}
class Car {  
  //Bil - indeholder position & hastighed & "tegning"
  PVector pos = new PVector(60, 232);
  PVector vel = new PVector(0, 5);
  
  public void turnCar(float turnAngle){
    vel.rotate(turnAngle);
  }

  public void displayCar() {
    stroke(100);
    fill(100);
    ellipse(pos.x, pos.y, 10, 10);
  }
  
  public void update() {
    pos.add(vel);
  }
  
}
class CarSystem {
  //CarSystem - 
  //Her kan man lave en generisk alogoritme, der skaber en optimal "hjerne" til de forhåndenværende betingelser

  DNA[] population;
  ArrayList<DNA> matingPool;
  float mutationRate = 0.001f;
  float topFitness;

  CarSystem(int populationSize) {
    population = new DNA[populationSize];

    for (int i=0; i<populationSize; i++) { 
      population[i] = new DNA(2);
    }
  }

  public void calcFitness() {
    for (int i=0; i<populationSize; i++) {
      population[i].fitness();
    }
  }

  public void naturalSelection() {
    /*for (int i = matingPool.size()-1; i >= 0; i--){
      if(matingPool.get(i).fitness < bestFitness){
        matingPool.remove(i);
      }
    }*/
    matingPool = new ArrayList<DNA>();

    float maxFitness = 0;
    for (int i = 0; i < populationSize; i++) {
      if (population[i].fitness > maxFitness) {
        maxFitness = population[i].fitness;
        topFitness = maxFitness;
      }
    }

    for (int i = 0; i < populationSize; i++) {
      int n = PApplet.parseInt(population[i].fitness/maxFitness*100);
      for (int j = 0; j < n; j++) {   
        matingPool.add(population[i]);
      }
    }
  }  

  public void generate() {
    for (int i=0; i<populationSize; i++) {
      DNA child = matingPool.get(PApplet.parseInt(random(matingPool.size()))).newDNA();
      child.mutate(mutationRate);
      population[i] = child;
    }
  }

  public void run() {
    //1.) Opdaterer sensorer og bilpositioner
    for (DNA dna : population) {
      if (dna.sensorSystem.whiteSensorFrameCount<=0) dna.update();
    }

    //2.) Tegner tilsidst - så sensorer kun ser banen og ikke andre biler!
    for (DNA dna : population) {
      if (dna.sensorSystem.whiteSensorFrameCount<=0) dna.display();
    }
  }
}
class DNA {


  float[] genes = new float[11];
  float fitness;
  float mutationVarians = 0.05f;

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

  public void fitness() {
    fitness = sensorSystem.clockWiseRotationFrameCounter;
    if(sensorSystem.whiteSensorFrameCount > 0) fitness = PApplet.parseInt(fitness/2)-50;
    if (fitness<0) fitness = 0;
  }

  public void mutate(float mutationRate) {
    for (int i=0; i<genes.length; i++) {
      if (random(1)<mutationRate) {
        genes[i] += random(-mutationVarians, mutationVarians);
      }
    }
  }

  public DNA newDNA() {
    DNA child  = new DNA(varians);
    child.hjerne.weights = hjerne.returnWeights();
    return child;
  }

  public void reset() {
    bil = new Car();
    sensorSystem = new SensorSystem();
  }

  public void update() {
    //1.)opdtarer bil 
    bil.update();
    //2.)opdaterer sensorer    
    sensorSystem.updateSensorsignals(bil.pos, bil.vel);
    //3.)hjernen beregner hvor meget der skal drejes
    float turnAngle = 0;
    float x1 = PApplet.parseInt(sensorSystem.leftSensorSignal);
    float x2 = PApplet.parseInt(sensorSystem.frontSensorSignal);
    float x3 = PApplet.parseInt(sensorSystem.rightSensorSignal);    
    turnAngle = hjerne.getOutput(x1, x2, x3);    
    //4.)bilen drejes
    bil.turnCar(turnAngle);
  }

  public void display() {
    bil.displayCar();
    if (showCensors) sensorSystem.displaySensors();
  }
}
class NeuralNetwork {
  //All weights
  float[] weights = new float[8];
  
    //Naming convention w{layer number}_{from neuron number}_{to neuron number}
    // layer 1, 2 hidden neurons: w0_11=w[0], w0_21=w[1], w0_31=w[2] 
    //                            w0_12=w[3], w0_22=w[4], w0_32=w[5]
    // layer 2, 1 output neuron : w1_11=w[6], w1_21=w[7] 
  
  //All biases
  float[] biases = {0,0,0};//new float[3];
    //Naming convention b{layer number}_{neuron number}
    // layer 1, 2 hidden neurons: b2_1=b[0], b2_2=b[1]
    // layer 2, 1 output neuron : b3_1=b[2]
  
  NeuralNetwork(float varians){
    for(int i=0; i < weights.length -1; i++){
      weights[i] = random(-varians,varians);
    }
    for(int i=0; i < biases.length -1; i++){
      biases[i] = random(-varians,varians);
    }
  }
  
  public float[] returnWeights(){
    return weights;
  }

  public float getOutput(float x1, float x2, float x3) {
    //layer1
    float o11 = weights[0]*x1+ weights[1]*x2 + weights[2]*x3 + biases[0];
    float o12 = weights[3]*x1+ weights[4]*x2 + weights[5]*x3 + biases[1];
    //layer2
    return o11*weights[6] + o12*weights[7] + biases[2];
  }
}
class SensorSystem {
  //SensorSystem - alle bilens sensorer - ogå dem der ikke bruges af "hjernen"
  
  //tjekker hvor lang tid den er på højre side af banen
  float rightSide;
  
  //wall detectors
  float sensorMag = 50;
  float sensorAngle = PI*2/8;
  
  PVector anchorPos           = new PVector();
  
  PVector sensorVectorFront   = new PVector(0, sensorMag);
  PVector sensorVectorLeft    = new PVector(0, sensorMag);
  PVector sensorVectorRight   = new PVector(0, sensorMag);

  boolean frontSensorSignal   = false;
  boolean leftSensorSignal    = false;
  boolean rightSensorSignal   = false;

  //crash detection
  int whiteSensorFrameCount    = 0; //udenfor banen

  //clockwise rotation detection
  PVector centerToCarVector     = new PVector();
  float   lastRotationAngle   = -1;
  float   clockWiseRotationFrameCounter  = 0;

  //lapTime calculation
  boolean lastGreenDetection;
  int     lastTimeInFrames      = 0;
  int     lapTimeInFrames       = 10000;

  public void displaySensors() {
    strokeWeight(0.5f);
    if (frontSensorSignal) { 
      fill(255, 0, 0);
      ellipse(anchorPos.x+sensorVectorFront.x, anchorPos.y+sensorVectorFront.y, 8, 8);
    }
    if (leftSensorSignal) { 
      fill(255, 0, 0);
      ellipse( anchorPos.x+sensorVectorLeft.x, anchorPos.y+sensorVectorLeft.y, 8, 8);
    }
    if (rightSensorSignal) { 
      fill(255, 0, 0);
      ellipse( anchorPos.x+sensorVectorRight.x, anchorPos.y+sensorVectorRight.y, 8, 8);
    }
    line(anchorPos.x, anchorPos.y, anchorPos.x+sensorVectorFront.x, anchorPos.y+sensorVectorFront.y);
    line(anchorPos.x, anchorPos.y, anchorPos.x+sensorVectorLeft.x, anchorPos.y+sensorVectorLeft.y);
    line(anchorPos.x, anchorPos.y, anchorPos.x+sensorVectorRight.x, anchorPos.y+sensorVectorRight.y);

    strokeWeight(2);
    if (whiteSensorFrameCount>0) {
      fill(whiteSensorFrameCount*10, 0, 0);
    } else {
      fill(0, clockWiseRotationFrameCounter, 0);
    }
    ellipse(anchorPos.x, anchorPos.y, 10, 10);
  }

  public void updateSensorsignals(PVector pos, PVector vel) {
    //Collision detectors
    frontSensorSignal = get(PApplet.parseInt(pos.x+sensorVectorFront.x), PApplet.parseInt(pos.y+sensorVectorFront.y))==-1?true:false;
    leftSensorSignal = get(PApplet.parseInt(pos.x+sensorVectorLeft.x), PApplet.parseInt(pos.y+sensorVectorLeft.y))==-1?true:false;
    rightSensorSignal = get(PApplet.parseInt(pos.x+sensorVectorRight.x), PApplet.parseInt(pos.y+sensorVectorRight.y))==-1?true:false;  
    //Crash detector
    int color_car_position = get(PApplet.parseInt(pos.x), PApplet.parseInt(pos.y));
    if (color_car_position ==-1) {
      whiteSensorFrameCount = whiteSensorFrameCount+1;
    }
    //Laptime calculation
    boolean currentGreenDetection =false;
    if (red(color_car_position)==0 && blue(color_car_position)==0 && green(color_car_position)!=0) {//den grønne målstreg er detekteret
      currentGreenDetection = true;
    }
    if (lastGreenDetection && !currentGreenDetection) {  //sidst grønt - nu ikke -vi har passeret målstregen 
      lapTimeInFrames = frameCount - lastTimeInFrames; //LAPTIME BEREGNES - frames nu - frames sidst
      lastTimeInFrames = frameCount;
    }   
    lastGreenDetection = currentGreenDetection; //Husker om der var grønt sidst
    //count clockWiseRotationFrameCounter
    centerToCarVector.set((height/2)-pos.x, (width/2)-pos.y);    
    float currentRotationAngle =  centerToCarVector.heading();
    float deltaHeading   =  lastRotationAngle - centerToCarVector.heading();
    clockWiseRotationFrameCounter  =  deltaHeading>0 ? clockWiseRotationFrameCounter + 1 : clockWiseRotationFrameCounter -1;
    lastRotationAngle = currentRotationAngle;
    
    updateSensorVectors(vel);
    
    anchorPos.set(pos.x,pos.y);
    
    if (pos.x > width/2) rightSide++;
  }

  public void updateSensorVectors(PVector vel) {
    if (vel.mag()!=0) {
      sensorVectorFront.set(vel);
      sensorVectorFront.normalize();
      sensorVectorFront.mult(sensorMag);
    }
    sensorVectorLeft.set(sensorVectorFront);
    sensorVectorLeft.rotate(-sensorAngle);
    sensorVectorRight.set(sensorVectorFront);
    sensorVectorRight.rotate(sensorAngle);
  }
}
  public void settings() {  size(500, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "RacerbilsEvolutionsProgram" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
