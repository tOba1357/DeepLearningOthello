service LearningServer {
    void load(1:string file_name)
    void save(1:string file_name)
    void learning(1:list<list<double>> result, 2:list<list<i16>> board)
    list<list<double>> get(1:list<list<i16>> board)
    list<list<list<double>>> getWeight()
    list<list<double>> getBiase()
    void learningPhase2(1:list<list<double>> result, 2:list<list<i16>> board)
    void initial()
}

