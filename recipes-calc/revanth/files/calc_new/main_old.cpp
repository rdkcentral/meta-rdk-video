#include<iostream>
using namespace std;

#include "calc.h"

int main()
{
    int a,b;
    cout<<"Enter two numbers: ";
    cin>>a>>b;
    int option ;
    cout<<"Select operation: 1.Add 2.Subtract 3.Multiply 4.Divide 5.Modulus : ";
    cin>>option;
    switch(option){
        case 1:
            cout<<"Addition: "<<add(a,b)<<endl;
            break;
        case 2:
            cout<<"Subtraction: "<<sub(a,b)<<endl;
            break;
        case 3:
            cout<<"Multiplication: "<<mul(a,b)<<endl;
            break;
        case 4:
            cout<<"Division: "<<division(a,b)<<endl;
            break;
        case 5:
            cout<<"Modulus: "<<mod(a,b)<<endl;
            break;
        default:
            cout<<"Invalid option"<<endl;
    }
}
