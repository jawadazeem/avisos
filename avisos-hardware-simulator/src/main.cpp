#include <iostream>
#include "Simulator.h"

int main(int argc, char* argv[]) {
    std::cout << "Avisos Hardware Simulator starting...\nVersion: 0.1.0";

    Simulator sim;

    sim.start();
    sim.stop();

    return 0;
}