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

void setup() {
  size(500, 600);
  trackImage = loadImage("track.png");
}

void draw() {
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
  text("Time Elapsed: " + int(millis()/1000), 0, 24);
  text("Top Fitness: " + int(carSystem.topFitness), 220, 24);
  text("Max Fitness: " + bestFitness, 0, 50);
  text("generation: " + generations, 220, 50);
  fill(0);
  text("Press 'enter' to toggle censors", 80, height-24);
}

void simulate() {
  if (millis() > timer+simulationTimer*1000) {
    timer = millis();

    if (bestFitness < carSystem.topFitness) bestFitness = int(carSystem.topFitness);

    carSystem.generate();
    for (int i = 0; i < populationSize; i++) {
      carSystem.population[i].reset();            
    }
    generations++;
  }
}

void keyPressed(){
  if (keyCode == (int)ENTER){
    showCensors = !showCensors;
  }
}
