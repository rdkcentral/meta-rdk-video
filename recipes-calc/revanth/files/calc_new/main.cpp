#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

using namespace std;

#include "calc.h"

int main()
{
    ifstream file("/etc/Calculator/instructions.txt");

    if (!file.is_open()) {
        cout << "Failed to open instructions file" << endl;
        return 1;
    }

    string line;
    while (getline(file, line)) {

        string option;
        int a, b;

        // Split line: operation operand1 operand2
        istringstream iss(line);
        if (!(iss >> option >> a >> b)) {
            cout << "Invalid line format: " << line << endl;
            continue;
        }

        // Perform operation
        if (option == "add") {
            cout << "Addition: " << add(a, b) << endl;
        }
        else if (option == "subtract") {
            cout << "Subtraction: " << sub(a, b) << endl;
        }
        else if (option == "multiply") {
            cout << "Multiplication: " << mul(a, b) << endl;
        }
        else if (option == "divide") {
            cout << "Division: " << division(a, b) << endl;
        }
        else if (option == "modulus") {
            cout << "Modulus: " << mod(a, b) << endl;
        }
        else {
            cout << "Invalid option: " << option << endl;
        }
    }

    file.close();
    return 0;
}
