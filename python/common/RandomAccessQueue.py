import random

class RandomAccessQueue:
    def __init__(self, size):
        self.size = size
        self.data = []

    def get(self, size):
        return random.sample(self.data, size)

    def add(self, add_data):
        pop_size = len(add_data) + len(self.data) - self.size
        if pop_size <= 0:
            self.data = add_data + self.data
        else:
            self.data = add_data + self.data[:-pop_size]
