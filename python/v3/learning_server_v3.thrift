service LearningServer {
    void train(1:list<list<list<double>>> data)
    list<list<list<double>>> getWeight()
    list<list<double>> getBiase()
}

