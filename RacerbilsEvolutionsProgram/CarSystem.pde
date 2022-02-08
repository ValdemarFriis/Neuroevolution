class CarSystem {
  //CarSystem - 
  //Her kan man lave en generisk alogoritme, der skaber en optimal "hjerne" til de forhåndenværende betingelser

  DNA[] population;
  ArrayList<DNA> matingPool = new ArrayList<DNA>();
  float mutationRate = 0.00001;

  CarSystem(int populationSize) {
    population = new DNA[populationSize];

    for (int i=0; i<populationSize; i++) { 
      population[i] = new DNA();
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
      }
    }

    for (int i = 0; i < populationSize; i++) {

      float fitness = map(population[i].fitness, 0, maxFitness, 0, 1);
      int n = int(fitness*100);
      for (int j = 0; j < n; j++) {   
        if (population[i].fitness > 0) matingPool.add(population[i]);
      }
    }
    for (DNA d : matingPool) println(d.fitness);
  }  

  void generate() {
    for (int i=0; i<populationSize; i++) {
      int a = int(random(matingPool.size()));
      int b = int(random(matingPool.size()));
      DNA partnerA = matingPool.get(a);
      DNA partnerB = matingPool.get(b);
      DNA child = partnerA.crossover(partnerB);
      child.mutate(mutationRate);
      population[i]=child;
    }
  }

  void run() {
    //1.) Opdaterer sensorer og bilpositioner
    for (DNA dna : population) {
      dna.update();
    }

    //2.) Tegner tilsidst - så sensorer kun ser banen og ikke andre biler!
    for (DNA dna : population) {
      dna.display();
    }
  }
}
