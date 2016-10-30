#include <iostream>
#include <fstream>
#include <string>
#include <stdio.h>
#include <vector>
#include <sstream>

using namespace std;
int main()
{
	std::ifstream ifs;
	ifs.open("inp.txt", std::ifstream::in);
	std::string temp;
 	std::getline(ifs, temp);


	vector<int> vect;

	std::stringstream ss(temp);

	int i;

	while (ss >> i)
	{
		vect.push_back(i);

		if (ss.peek() == ',' || ss.peek() == ' ' )
			ss.ignore();
	}
	
	ifs.close();
	return 0;	
}
