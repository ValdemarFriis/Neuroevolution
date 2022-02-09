class CarSystem {
  //CarSystem - 
  //Her kan man lave en generisk alogoritme, der skaber en optimal "hjerne" til de forhåndenværende betingelser

  DNA[] population;
  ArrayList<DNA> matingPool = new ArrayList<DNA>();
  float mutationRate = 0.001;
  float topFitness;

  CarSystem(int populationSize) {
    population = new DNA[populationSize];

    for (int i=0; i<populationSize; i++) { 
      population[i] = new DNA(2);
    }
  }

  void calcFitness() {
    for (int i=0; i<populationSize; i++) {
      population[i].fitness();
    }
  }

  void naturalSelection() {
    matingPool.clear();

    float maxFitness = 0;
    for (int i = 0; i < populationSize; i++) {
      if (population[i].fitness > maxFitness) {
        maxFitness = population[i].fitness;
        topFitness = maxFitness;
      }
    }

    for (int i = 0; i < populationSize; i++) {

      int n = int(population[i].fitness/maxFitness*100);
      for (int j = 0; j < n; j++) {   
        matingPool.add(population[i]);
      }
    }
  }  

  void generate() {
    for (int i=0; i<populationSize; i++) {
      DNA child = matingPool.get(int(random(matingPool.size()))).newDNA();
      child.mutate(mutationRate);
      population[i] = child;
    }
  }

  void run() {
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
